package dev.jpa.team2.chatbot.rag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.chatbot.FastApiLlmService;
import dev.jpa.team2.chatbot.dataref.ChatDataRefRepository;
import dev.jpa.team2.chatbot.message.ChatMessage;
import dev.jpa.team2.chatbot.message.ChatMessageService;
import dev.jpa.team2.chatbot.session.ChatSession;
import dev.jpa.team2.chatbot.session.ChatSessionService;
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

        // 0) 기본 검증
        if (sessionId == null) throw new IllegalArgumentException("sessionId is required");
        if (question == null || question.isBlank()) throw new IllegalArgumentException("question is required");

        // 1) 세션 소유권 체크
        chatSessionService.requireOwnedSession(memberId, sessionId);

        // 2) 사용자 메시지 저장
        chatMessageService.saveMessage(memberId, sessionId, "USER", question);

        // 3) 세션 컨텍스트(ChatDataRef) 구성
        String sessionContext = chatDataRefRepository
            .findByMemberIdAndSessionIdOrderByCreatedAtDesc(memberId, sessionId)
            .stream()
            .map(ref -> String.format("[%s] %s\n%s", ref.getRefType(), ref.getTitle(), ref.getSummary()))
            .collect(Collectors.joining("\n\n"));

        // 4) ✅ Python /ask 호출 (answer + references + followUpQuestions)
        Map<String, Object> py = llmService.ask(
            question,
            sessionContext,
            DEFAULT_TOP_K,
            null, // docType 필요하면 dto에 필드 추가해서 넘기면 됨
            null  // stage 필요하면 dto에 필드 추가해서 넘기면 됨
        );

        // 5) 응답 파싱
        String answer = safeString(py, "answer");
        if (answer == null || answer.isBlank()) answer = "(답변 없음)";

        List<RagReferenceDto> references = parseReferences(py);
        List<String> followUps = parseFollowUps(py);

        // 6) (옵션) RAG 결과 테이블 저장
        try {
            ragRepository.save(new ChatRag(sessionId, question, answer));
        } catch (Exception e) {
            // 저장 실패가 전체 응답을 죽이면 UX가 너무 나빠서 방어
            log.warn("[ChatRagService] ragRepository.save failed (ignored) sessionId={}", sessionId, e);
        }

        // 7) assistant 메시지 저장
        ChatMessage assistantMsg = chatMessageService.saveAssistantMessageWithFollowUps(
            memberId, sessionId, answer, followUps
        );
        Long assistantChatId = assistantMsg.getChatId();

        // 8) ✅ (핵심) 세션 제목 자동 생성/업데이트 (1~2턴 기준은 ensureTitleUpdated 내부에서 처리)
        // - title이 "새 대화"일 때만, 메시지 수가 충분할 때만, 앞부분 1~2턴으로 /title 호출하도록 구현하는 게 베스트
        try {
            chatSessionService.ensureTitleUpdated(memberId, sessionId);
        } catch (Exception e) {
            // 제목 생성 실패가 응답을 깨면 UX가 나빠서 방어
            log.warn("[ChatRagService] ensureTitleUpdated failed (ignored) sessionId={}", sessionId, e);
        }

        // 9) ✅ 최신 세션 title 조회해서 응답에 포함 (프론트에서 즉시 반영 가능)
        String sessionTitle = null;
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

    // -------------------------
    // parsing helpers
    // -------------------------
    private String safeString(Map<String, Object> py, String key) {
        if (py == null) return null;
        Object v = py.get(key);
        return v == null ? null : String.valueOf(v);
    }

    private List<RagReferenceDto> parseReferences(Map<String, Object> py) {
        if (py == null) return Collections.emptyList();

        Object refObj = py.get("references");
        if (!(refObj instanceof List<?> list)) return Collections.emptyList();

        List<RagReferenceDto> refs = new ArrayList<>();
        for (Object o : list) {
            if (!(o instanceof Map<?, ?> m)) continue;

            RagReferenceDto r = new RagReferenceDto();
            Object cid = m.get("chunkId");
            r.setChunkId(cid == null ? null : String.valueOf(cid));

            Object title = m.get("title");
            r.setTitle(title == null ? null : String.valueOf(title));

            Object snippet = m.get("snippet");
            r.setSnippet(snippet == null ? null : String.valueOf(snippet));

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
}
