package dev.jpa.team2.chatbot;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RagService {

    private final OpenAiService openAiService;
    private final EmbeddingSimilarityService similarityService;
    private final ChatHistoryRepository chatHistoryRepository;
    private final ChatDataRefRepository chatDataRefRepository;

    private static final int TOP_K = 5;

    public List<ChatHistory> getHistory(Long sessionId) {
      return chatHistoryRepository.findBySessionId(sessionId);
  }
    
    public RagDto ask(RagDto dto) {

        // 1️⃣ 질문 임베딩
        String queryVector =
            openAiService.embedding(dto.getQuestion());

        // 2️⃣ Top-K 검색
        List<EmbeddingSearchResultDto> topChunks =
            similarityService.searchTopK(queryVector, TOP_K);

        // 3️⃣ Context 생성
        String context = topChunks.stream()
            .map(EmbeddingSearchResultDto::getChunkText)
            .collect(Collectors.joining("\n\n"));

        // 4️⃣ LLM 호출
        String answer =
            openAiService.chat(context, dto.getQuestion());

        // 5️⃣ ChatHistory 저장
        ChatHistory chatHistory =
            chatHistoryRepository.save(
                new ChatHistory(
                    dto.getSessionId(),
                    dto.getQuestion(),
                    answer
                )
            );

        // 6️⃣ ChatDataRef 저장 (출처)
        for (EmbeddingSearchResultDto chunk : topChunks) {
            chatDataRefRepository.save(
                new ChatDataRef(
                    chatHistory.getChatId(),
                    chunk.getChunkId(),
                    chunk.getSimilarityScore()
                )
            );
        }

        // 7️⃣ 응답 DTO 구성
        dto.setAnswer(answer);
        dto.setReferences(topChunks);

        return dto;
    }
}

