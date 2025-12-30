package dev.jpa.team2.chatbot.rag;

import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.chatbot.FastApiLlmService;
import dev.jpa.team2.chatbot.dataref.ChatDataRefRepository;
import dev.jpa.team2.chatbot.embeddingchunk.EmbeddingChunkDto;
import dev.jpa.team2.chatbot.embeddingchunk.EmbeddingSimilarityService;
import dev.jpa.team2.chatbot.message.ChatMessage;
import dev.jpa.team2.chatbot.message.ChatMessageService;
import dev.jpa.team2.chatbot.message.ChatMessagesResponseDto;
import dev.jpa.team2.chatbot.messageref.ChatMessageRef;
import dev.jpa.team2.chatbot.messageref.ChatMessageRefRepository;
import dev.jpa.team2.chatbot.session.ChatSessionService;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;
    private final ChatMessageRefRepository chatMessageRefRepository;
    private final ChatDataRefRepository chatDataRefRepository;
    private final RagRepository ragResultRepository;

    private final FastApiLlmService llmService;
    private final EmbeddingSimilarityService similarityService;

    private static final int TOP_K = 5;

    @Transactional
    public RagDto ask(RagDto dto, Long memberId) {

        Long sessionId = dto.getSessionId();
        String question = dto.getQuestion();

        // 세션 소유권 체크
        chatSessionService.requireOwnedSession(memberId, sessionId);

        // USER 메시지 저장
        ChatMessage userMsg = chatMessageService.saveMessage(memberId, sessionId, "USER", question);

        // 세션에 붙은 문서/체크리스트 context 가져오기
        String sessionContext = chatDataRefRepository
            .findByMemberIdAndSessionIdOrderByCreatedAtDesc(memberId, sessionId)
            .stream()
            .map(ref -> String.format(
                "[%s] %s\n%s",
                ref.getRefType(), ref.getTitle(), ref.getSummary()
            ))
            .collect(Collectors.joining("\n\n"));

        // 질문 임베딩
        List<Double> queryVector = llmService.embedding(question);

        // Top-K 검색
        List<EmbeddingChunkDto.SearchResult> topChunks =
            similarityService.searchTopK(queryVector, TOP_K);

        // RAG Context 생성
        String ragContext = topChunks.stream()
            .map(EmbeddingChunkDto.SearchResult::getChunkText)  // ✅ 변경
            .collect(Collectors.joining("\n\n"));

        // 최종 context
        String finalContext = ""
            + "=== [세션 참고자료: 업로드/분석 결과 요약] ===\n"
            + (sessionContext == null || sessionContext.isBlank() ? "(없음)" : sessionContext)
            + "\n\n=== [RAG 검색 참고자료] ===\n"
            + (ragContext == null || ragContext.isBlank() ? "(없음)" : ragContext);

        log.info("SESSION_CONTEXT_LEN={} HEAD={}",
            sessionContext == null ? -1 : sessionContext.length(),
            (sessionContext == null ? "null" : sessionContext.substring(0, Math.min(200, sessionContext.length())))
        );

        // LLM 호출
        String answer = llmService.chat(finalContext, question);

        // ASSISTANT 메시지 저장
        ChatMessage assistantMsg = chatMessageService.saveMessage(memberId, sessionId, "ASSISTANT", answer);

        Long assistantChatId = assistantMsg.getChatId();

        // RagResult 저장(선택)
        Rag ragResult = ragResultRepository.save(new Rag(sessionId, question, answer));
        dto.setRagId(ragResult.getRagId());

        // refs 저장
        for (EmbeddingChunkDto.SearchResult chunk : topChunks) {
            chatMessageRefRepository.save(
                ChatMessageRef.builder()
                    .chatId(assistantChatId)
                    .chunkId(chunk.getChunkId())
                    .score(chunk.getSimilarityScore())
                    .build()
            );
        }

        // 응답 DTO 구성
        dto.setAnswer(answer);
        dto.setReferences(topChunks);

        return dto;
    }

    @Transactional(readOnly = true)
    public ChatMessagesResponseDto getHistory(Long memberId, Long sessionId) {
        return chatMessageService.loadSessionMessages(memberId, sessionId);
    }
}
