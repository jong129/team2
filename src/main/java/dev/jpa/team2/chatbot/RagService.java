package dev.jpa.team2.chatbot;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RagService {

    private final FastApiLlmService llmService;
    private final EmbeddingSimilarityService similarityService;

    // ✅ 메시지 저장/조회는 "메시지 단위" 서비스로 통일
    private final ChatHistoryService chatHistoryService;

    // ✅ 세션 소유권 체크/최근대화 시간 갱신
    private final ChatSessionService chatSessionService;

    // ✅ RAG 근거 저장
    private final ChatDataRefRepository chatDataRefRepository;

    private static final int TOP_K = 5;

    /**
     * (옵션) 세션 대화 조회 (디버그)
     */
    @Transactional(readOnly = true)
    public ChatMessagesResponseDto getHistory(Long memberId, Long sessionId) {
        return chatHistoryService.loadSessionMessages(memberId, sessionId);
    }

    /**
     * ✅ 핵심: RAG ask
     * 1) 내 세션인지 확인
     * 2) USER 메시지 저장
     * 3) RAG 검색 + LLM 답변 생성
     * 4) ASSISTANT 메시지 저장
     * 5) refs를 ASSISTANT messageId 기준으로 저장
     */
    @Transactional
    public RagDto ask(RagDto dto, Long memberId) {

        Long sessionId = dto.getSessionId();
        String question = dto.getQuestion();

        // 0️⃣ 세션 소유권 체크 (남의 세션에 쓰기/조회 방지)
        chatSessionService.requireOwnedSession(memberId, sessionId);

        // 1️⃣ USER 메시지 저장
        chatHistoryService.saveMessage(memberId, sessionId, "USER", question);

        // 2️⃣ 질문 임베딩
        List<Double> queryVector = llmService.embedding(question);

        // 3️⃣ Top-K 검색
        List<EmbeddingSearchResultDto> topChunks =
            similarityService.searchTopK(queryVector, TOP_K);

        // 4️⃣ Context 생성
        String context = topChunks.stream()
            .map(EmbeddingSearchResultDto::getChunkText)
            .collect(Collectors.joining("\n\n"));

        // 5️⃣ LLM 호출
        String answer = llmService.chat(context, question);

        // 6️⃣ ASSISTANT 메시지 저장 (여기서 messageId 확보)
        ChatHistory aiMsg =
            chatHistoryService.saveMessage(memberId, sessionId, "ASSISTANT", answer);

        // 7️⃣ ChatDataRef 저장 (출처) - ✅ aiMsg.getChatId() 기준으로 저장
        for (EmbeddingSearchResultDto chunk : topChunks) {
            chatDataRefRepository.save(
                new ChatDataRef(
                    aiMsg.getChatId(),              // ✅ ASSISTANT messageId
                    chunk.getChunkId(),
                    chunk.getSimilarityScore()
                )
            );
        }

        // 8️⃣ 응답 DTO 구성
        dto.setAnswer(answer);
        dto.setReferences(topChunks);

        return dto;
    }
}
