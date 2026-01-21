package dev.jpa.team2.chatbot.domain.rag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.chatbot.FastApiLlmService;
import dev.jpa.team2.chatbot.api.rag.RagReferenceDto;
import dev.jpa.team2.chatbot.domain.dataref.ChatDataRefRepository;
import dev.jpa.team2.chatbot.domain.message.ChatMessage;
import dev.jpa.team2.chatbot.domain.message.ChatMessageService;
import dev.jpa.team2.chatbot.domain.session.ChatSession;
import dev.jpa.team2.chatbot.domain.session.ChatSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 핵심 서비스 (파이프라인)

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRagService {

    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;
    private final ChatDataRefRepository chatDataRefRepository;
    private final ChatRagRepository ragRepository;
    private final FastApiLlmService llmService;

    private static final int DEFAULT_TOP_K = 5;

    @Transactional
    public ChatRagDto ask(ChatRagDto dto, Long memberId) {

        Long sessionId = dto.getSessionId();
        String question = dto.getQuestion();

        // 0) 기본 입력 검증
        if (sessionId == null) throw new IllegalArgumentException("sessionId is required"); // sessionId 필수
        if (question == null || question.isBlank()) throw new IllegalArgumentException("question is required"); // question 필수 (Blank 금지)

        // 1) 세션 소유권 체크 : 내 세션이 아니면 질문 자체를 막음 (권한/보안 핵심)
        chatSessionService.requireOwnedSession(memberId, sessionId);

        // 2) USER 메시지 저장 : 질문이 들어오면 먼저 CHAT_MESSAGE에 기록
        chatMessageService.saveMessage(memberId, sessionId, "USER", question);

        // 3) 세션 컨텍스트(ChatDataRef) 구성 : 문서 분석 결과 / 체크리스트 결과 등 같은 걸 최신순으로 가져와 컨텍스트에 합침
        String sessionContext = chatDataRefRepository
            .findByMemberIdAndSessionIdOrderByCreatedAtDesc(memberId, sessionId)
            .stream()
            .map(ref -> String.format("[%s] %s\n%s", ref.getRefType(), ref.getTitle(), ref.getSummary()))
            .collect(Collectors.joining("\n\n"));

        // 4) Python /ask 호출 (answer + references + followUpQuestions + model/tokensIn/tokensOut)
        Map<String, Object> py = llmService.ask(
            question,
            sessionContext,
            DEFAULT_TOP_K,
            null,
            null
        );

        // 디버그 로그: 응답 구조 확인 (latency 측정 없음)
        if (py != null) {
            log.info("[ChatRagService] /ask response keys={}", py.keySet());
            Object usageRaw = py.get("usage");
            log.info("[ChatRagService] /ask usageRawType={} usageRaw={}",
                usageRaw == null ? "null" : usageRaw.getClass().getName(),
                usageRaw
            );
            log.info("[ChatRagService] /ask root fields | model={} tokensIn={} tokens_in={} tokensOut={} tokens_out={} tokensTotal={} tokens_total={} latencyMs={} latency_ms={}",
                py.get("model"),
                py.get("tokensIn"), py.get("tokens_in"),
                py.get("tokensOut"), py.get("tokens_out"),
                py.get("tokensTotal"), py.get("tokens_total"),
                py.get("latencyMs"), py.get("latency_ms")
            );
        }

        // 5) 응답 파싱
        // answer 파싱
        String answer = safeString(py, "answer");
        if (answer == null || answer.isBlank()) answer = "(답변 없음)";
        
        // references 파싱 : Python이 내려주는 근거를 그대로 프론트에 전달 가능한 형태로 변환
        List<RagReferenceDto> references = parseReferences(py);
        
        // followUpQuestions 파싱 : followUpQuestions 리스트에서 null/blank 제거
        List<String> followUps = parseFollowUps(py);

        // usage 파싱: usage 객체 우선 → 없으면 루트(py)에서 fallback으로 찾음
        Map<String, Object> usageMap = safeMap(py, "usage");

        // model
        String model = firstString(usageMap, "model");
        if (model == null) model = firstString(py, "model");

        // tokens in/out
        Integer tokensIn = firstInt(usageMap, "tokensIn", "tokens_in");
        if (tokensIn == null) tokensIn = firstInt(py, "tokensIn", "tokens_in");

        Integer tokensOut = firstInt(usageMap, "tokensOut", "tokens_out");
        if (tokensOut == null) tokensOut = firstInt(py, "tokensOut", "tokens_out");
  
        // tokens total
        Integer tokensTotal = firstInt(usageMap, "tokensTotal", "tokens_total");
        if (tokensTotal == null) tokensTotal = firstInt(py, "tokensTotal", "tokens_total");
  
        // latency (FastAPI가 내려주면 저장, 아니면 null 유지)
        Integer latencyMs = firstInt(usageMap, "latencyMs", "latency_ms");
        if (latencyMs == null) latencyMs = firstInt(py, "latencyMs", "latency_ms");
  
        // tokensTotal이 안 오면 in+out으로 계산
        if (tokensTotal == null && (tokensIn != null || tokensOut != null)) {
           tokensTotal = (tokensIn == null ? 0 : tokensIn) + (tokensOut == null ? 0 : tokensOut);
        }

        // dto.usage 세팅
        if (model != null || tokensIn != null || tokensOut != null || tokensTotal != null || latencyMs != null) {
            ChatRagDto.UsageDto u = new ChatRagDto.UsageDto();
            u.setModel(model);
            u.setTokensIn(tokensIn);
            u.setTokensOut(tokensOut);
            u.setTokensTotal(tokensTotal);
            u.setLatencyMs(latencyMs);
            dto.setUsage(u);
        }
  
        // usage 파싱 결과 로그
        log.info("[ChatRagService] usage parsed | model={} tokensIn={} tokensOut={} tokensTotal={} latencyMs={}",
             model, tokensIn, tokensOut, tokensTotal, latencyMs
        );

        // 6) RAG 결과 테이블 저장 : 실패해도 전체 흐름은 계속 진행 (로그만 남김)
        try {
            ragRepository.save(new ChatRag(sessionId, question, answer));
        } catch (Exception e) {
            log.warn("[ChatRagService] ragRepository.save failed (ignored) sessionId={}", sessionId, e);
        }

        // 7) ASSISTANT 메시지 저장 (followUps + usage 포함)
        // 답변을 CHAT_MESSAGE에 저장하면서 followUps 3개를 suggest에/ usage를 message 컬럼에 넣고 저장된 assistantChatId를 응답에 넣어줌
        ChatMessage assistantMsg = chatMessageService.saveAssistantMessageWithFollowUpsAndUsage(
            memberId, sessionId, answer, followUps, model, tokensIn, tokensOut, tokensTotal, latencyMs
        );
        Long assistantChatId = assistantMsg.getChatId();

        // 8) 세션 제목 자동 생성/업데이트 : 대화가 쌓이면 제목을 자동으로 만드는 기능과 연결 (실패해도 무시)
        try {
            chatSessionService.ensureTitleUpdated(memberId, sessionId);
        } catch (Exception e) {
            log.warn("[ChatRagService] ensureTitleUpdated failed (ignored) sessionId={}", sessionId, e);
        }

        // 9) 최신 sessionTitle 조회 후 응답에 포함 : 프론트에서 세션 목록/탭 제목 등을 최신으로 업데이트하기 좋게 응답에 실어줌
        String sessionTitle = null;
        try {
            ChatSession s = chatSessionService.requireOwnedSession(memberId, sessionId);
            sessionTitle = (s.getTitle() == null || s.getTitle().isBlank()) ? "새 대화" : s.getTitle().trim();
        } catch (Exception e) {
            log.warn("[ChatRagService] load session title failed (ignored) sessionId={}", sessionId, e);
            sessionTitle = "새 대화";
        }

        // 10) 최종 응답 세팅
        dto.setAnswer(answer);
        dto.setReferences(references);
        dto.setFollowUpQuestions(followUps);
        dto.setAssistantChatId(assistantChatId);
        dto.setSessionTitle(sessionTitle);

        // (선택) 응답에도 usage를 실어주고 싶다면 ChatRagDto에 필드 추가 후 세팅
        // dto.setModel(model);
        // dto.setTokensIn(tokensIn);
        // dto.setTokensOut(tokensOut);
        
        return dto;
    }

    // ===== 파싱 헬퍼 ===== //
    /** Python 쪽에서 answer 같은 값이 항상 String으로 오지 않을걸 대비해 안전하게 처리하려고 만든 기본 유틸 */
    private String safeString(Map<String, Object> py, String key) {
        if (py == null) return null;  // py가 null이면 null
        Object v = py.get(key); // key가 없거나 값이 null이면 null
        return v == null ? null : String.valueOf(v);  // 값이 있으면 어떤 타입이든 String.valueOf(v)로 문자열화
    }
    
    /** 정수로 바꿀 수 있으면 바꾸고, 아니면 조용히 null */
    private Integer safeInt(Map<String, Object> py, String key) {
        if (py == null) return null;  
        Object v = py.get(key);
        if (v == null) return null;

        if (v instanceof Number n) return n.intValue(); // 값이 Number면 바로 intValue()

        try {
            String s = String.valueOf(v).trim();  // 문자열로 바꿔서 trim 후 Integer.parseInt. 실패하면 null 반환
            if (s.isBlank()) return null;
            return Integer.parseInt(s); 
        } catch (Exception e) {
            return null;
        }
    }
    
    /** references 리스트 파싱 */
    private List<RagReferenceDto> parseReferences(Map<String, Object> py) {
      if (py == null) return Collections.emptyList(); // references가 List가 아니면 빈 리스트

      Object refObj = py.get("references");
      if (!(refObj instanceof List<?> list)) return Collections.emptyList();

      List<RagReferenceDto> refs = new ArrayList<>();
      for (Object o : list) {
          if (!(o instanceof Map<?, ?> m)) continue;  // 리스트 원소가 Map이 아니면 스킵

          RagReferenceDto r = new RagReferenceDto();
          
          // Map에서 chunkId/title/snippet은 문자열로 강제 변환
          r.setChunkId(m.get("chunkId") == null ? null : String.valueOf(m.get("chunkId")));
          r.setTitle(m.get("title") == null ? null : String.valueOf(m.get("title")));
          r.setSnippet(m.get("snippet") == null ? null : String.valueOf(m.get("snippet")));
          
          // score, rankNo는 Number일 때만 값 세팅
          Object score = m.get("score");
          if (score instanceof Number n) {
              r.setScore(n.doubleValue());
          }

          Object rankNo = m.get("rankNo");
          if (rankNo instanceof Number n) {
              r.setRankNo(n.intValue());
          }

          refs.add(r);
      }
      return refs;
    }
    
    /** 후속 질문 3개만 추출 */
    private List<String> parseFollowUps(Map<String, Object> py) {
        if (py == null) return Collections.emptyList();

        Object fuObj = py.get("followUpQuestions");
        if (!(fuObj instanceof List<?> list)) return Collections.emptyList();
        
        // null/공백 제거
        List<String> out = new ArrayList<>();
        for (Object o : list) {
            if (o == null) continue;
            String s = String.valueOf(o).trim();
            if (!s.isBlank()) out.add(s);
            if (out.size() >= 3) break; // 최대 3개까지만 담고 종료
        }
        return out;
    }
      
    /** Map 하위 객체 안전 캐스팅 */
    @SuppressWarnings("unchecked")
    private Map<String, Object> safeMap(Map<String, Object> py, String key) {
        if (py == null) return null;
        Object v = py.get(key);
        if (v instanceof Map<?, ?> m) { // py.get(key)가 Map이면 Map<String,Object>로 캐스팅해서 반환
            return (Map<String, Object>) m;
        }
        return null;  // 아니면 null
    }
    
    /** 여러 키 중 처음으로 유효한 문자열 찾기 : Python 응답 키가 버전/작성자에 따라 달라질 수 있음. */
    private String firstString(Map<String, Object> map, String... keys) {
      if (map == null || keys == null) return null;
      
      // keys 후보를 순서대로 돌면서 값이 있고 공백이 아니면 첫번째 값 반환
      for (String k : keys) { 
          Object v = map.get(k);
          if (v == null) continue;
          String s = String.valueOf(v).trim();
          if (!s.isBlank()) return s;
      }
      return null;
    }
    
    /** 여러 키 중 처음으로 파싱 가능한 정수 찾기 : safeInt가 못 막는 특이 케이스를 대비한 이중 안전장치 */
    private Integer firstInt(Map<String, Object> map, String... keys) {
      if (map == null || keys == null) return null;
      for (String k : keys) {
          Object v = map.get(k);
          Integer parsed = safeInt(map, k);
          if (parsed != null) return parsed;
          // safeInt가 못 파싱하는 케이스 대비
          if (v != null) {
              try {
                  String s = String.valueOf(v).trim();
                  if (!s.isBlank()) return Integer.parseInt(s);
              } catch (Exception ignore) {}
          }
      }
      return null;
    }
}
