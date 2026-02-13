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

        if (sessionId == null) throw new IllegalArgumentException("sessionId is required");
        if (question == null || question.isBlank()) throw new IllegalArgumentException("question is required");

        // ✅ doc filter 파라미터
        String docId = dto.getDocId();
        String docType = dto.getDocType();
        String stage = dto.getStage();   // 예: "analysis"
        Integer topK = dto.getTopK() != null ? dto.getTopK() : DEFAULT_TOP_K;

        // 디버그: doc filter가 실제로 들어오는지 확인
        log.info("[ChatRagService] incoming dto | sessionId={} docId={} docType={} stage={} topK={}",
            sessionId, docId, docType, stage, topK
        );

        // 1) 소유권 체크
        chatSessionService.requireOwnedSession(memberId, sessionId);

        // 2) USER 메시지 저장
        chatMessageService.saveMessage(memberId, sessionId, "USER", question);

        // 3) 세션 컨텍스트 구성
        String sessionContext = chatDataRefRepository
            .findByMemberIdAndSessionIdOrderByCreatedAtDesc(memberId, sessionId)
            .stream()
            .map(ref -> String.format("[%s] %s\n%s", ref.getRefType(), ref.getTitle(), ref.getSummary()))
            .collect(Collectors.joining("\n\n"));

        // 4) ✅ FastAPI /ask 호출 (doc 필터 + user_id 포함)
        Map<String, Object> py = llmService.ask(
            question,
            sessionContext,
            topK,
            docType,
            docId,
            stage,
            String.valueOf(memberId) // ✅ user_id는 로그인 사용자로 확정
        );

        if (py != null) {
            log.info("[ChatRagService] /ask response keys={}", py.keySet());
        }

        // 5) 응답 파싱
        String answer = safeString(py, "answer");
        if (answer == null || answer.isBlank()) answer = "(답변 없음)";

        List<RagReferenceDto> references = parseReferences(py);
        List<String> followUps = parseFollowUps(py);

        Map<String, Object> usageMap = safeMap(py, "usage");

        String model = firstString(usageMap, "model");
        if (model == null) model = firstString(py, "model");

        Integer tokensIn = firstInt(usageMap, "tokensIn", "tokens_in");
        if (tokensIn == null) tokensIn = firstInt(py, "tokensIn", "tokens_in");

        Integer tokensOut = firstInt(usageMap, "tokensOut", "tokens_out");
        if (tokensOut == null) tokensOut = firstInt(py, "tokensOut", "tokens_out");

        Integer tokensTotal = firstInt(usageMap, "tokensTotal", "tokens_total");
        if (tokensTotal == null) tokensTotal = firstInt(py, "tokensTotal", "tokens_total");

        Integer latencyMs = firstInt(usageMap, "latencyMs", "latency_ms");
        if (latencyMs == null) latencyMs = firstInt(py, "latencyMs", "latency_ms");

        if (tokensTotal == null && (tokensIn != null || tokensOut != null)) {
           tokensTotal = (tokensIn == null ? 0 : tokensIn) + (tokensOut == null ? 0 : tokensOut);
        }

        if (model != null || tokensIn != null || tokensOut != null || tokensTotal != null || latencyMs != null) {
            ChatRagDto.UsageDto u = new ChatRagDto.UsageDto();
            u.setModel(model);
            u.setTokensIn(tokensIn);
            u.setTokensOut(tokensOut);
            u.setTokensTotal(tokensTotal);
            u.setLatencyMs(latencyMs);
            dto.setUsage(u);
        }

        // 6) RAG 로그 저장(실패 무시)
        try {
            ragRepository.save(new ChatRag(sessionId, question, answer));
        } catch (Exception e) {
            log.warn("[ChatRagService] ragRepository.save failed (ignored) sessionId={}", sessionId, e);
        }

        // 7) ASSISTANT 메시지 저장
        ChatMessage assistantMsg = chatMessageService.saveAssistantMessageWithFollowUpsAndUsage(
            memberId, sessionId, answer, followUps, model, tokensIn, tokensOut, tokensTotal, latencyMs
        );
        Long assistantChatId = assistantMsg.getChatId();

        // 8) 세션 제목 업데이트(실패 무시)
        try {
            chatSessionService.ensureTitleUpdated(memberId, sessionId);
        } catch (Exception e) {
            log.warn("[ChatRagService] ensureTitleUpdated failed (ignored) sessionId={}", sessionId, e);
        }

        // 9) sessionTitle 조회
        String sessionTitle;
        try {
            ChatSession s = chatSessionService.requireOwnedSession(memberId, sessionId);
            sessionTitle = (s.getTitle() == null || s.getTitle().isBlank()) ? "새 대화" : s.getTitle().trim();
        } catch (Exception e) {
            log.warn("[ChatRagService] load session title failed (ignored) sessionId={}", sessionId, e);
            sessionTitle = "새 대화";
        }

        // 10) 응답 세팅
        dto.setAnswer(answer);
        dto.setReferences(references);
        dto.setFollowUpQuestions(followUps);
        dto.setAssistantChatId(assistantChatId);
        dto.setSessionTitle(sessionTitle);

        return dto;
    }

    // ===== 파싱 헬퍼 =====
    private String safeString(Map<String, Object> py, String key) {
        if (py == null) return null;
        Object v = py.get(key);
        return v == null ? null : String.valueOf(v);
    }

    private Integer safeInt(Map<String, Object> py, String key) {
        if (py == null) return null;
        Object v = py.get(key);
        if (v == null) return null;

        if (v instanceof Number n) return n.intValue();

        try {
            String s = String.valueOf(v).trim();
            if (s.isBlank()) return null;
            return Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }

    private List<RagReferenceDto> parseReferences(Map<String, Object> py) {
      if (py == null) return Collections.emptyList();

      Object refObj = py.get("references");
      if (!(refObj instanceof List<?> list)) return Collections.emptyList();

      List<RagReferenceDto> refs = new ArrayList<>();
      for (Object o : list) {
          if (!(o instanceof Map<?, ?> m)) continue;

          RagReferenceDto r = new RagReferenceDto();
          r.setChunkId(m.get("chunkId") == null ? null : String.valueOf(m.get("chunkId")));
          r.setTitle(m.get("title") == null ? null : String.valueOf(m.get("title")));
          r.setSnippet(m.get("snippet") == null ? null : String.valueOf(m.get("snippet")));

          Object score = m.get("score");
          if (score instanceof Number n) r.setScore(n.doubleValue());

          Object rankNo = m.get("rankNo");
          if (rankNo instanceof Number n) r.setRankNo(n.intValue());

          refs.add(r);
      }
      return refs;
    }

    private List<String> parseFollowUps(Map<String, Object> py) {
        if (py == null) return Collections.emptyList();

        Object fuObj = py.get("followUpQuestions");
        if (!(fuObj instanceof List<?> list)) return Collections.emptyList();

        List<String> out = new ArrayList<>();
        for (Object o : list) {
            if (o == null) continue;
            String s = String.valueOf(o).trim();
            if (!s.isBlank()) out.add(s);
            if (out.size() >= 3) break;
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> safeMap(Map<String, Object> py, String key) {
        if (py == null) return null;
        Object v = py.get(key);
        if (v instanceof Map<?, ?> m) {
            return (Map<String, Object>) m;
        }
        return null;
    }

    private String firstString(Map<String, Object> map, String... keys) {
      if (map == null || keys == null) return null;
      for (String k : keys) {
          Object v = map.get(k);
          if (v == null) continue;
          String s = String.valueOf(v).trim();
          if (!s.isBlank()) return s;
      }
      return null;
    }

    private Integer firstInt(Map<String, Object> map, String... keys) {
      if (map == null || keys == null) return null;
      for (String k : keys) {
          Integer parsed = safeInt(map, k);
          if (parsed != null) return parsed;
      }
      return null;
    }
}
