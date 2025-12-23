package dev.jpa.team2.chatbot;

import dev.jpa.team2.chatbot.EmbeddingSearchResultDto;
import dev.jpa.team2.chatbot.EmbeddingChunk;
import dev.jpa.team2.chatbot.EmbeddingChunkRepository;
import dev.jpa.team2.chatbot.CosineSimilarityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmbeddingSimilarityService {

    private final EmbeddingChunkRepository repository;

    /**
     * 질문 벡터 기준 Top-K 검색
     */
    public List<EmbeddingSearchResultDto> searchTopK(
            String queryVectorString,
            int topK) {

        List<Double> queryVector =
            CosineSimilarityUtil.parseVector(queryVectorString);

        return repository.findAll()
                .stream()
                .map(chunk -> {

                    List<Double> chunkVector =
                        CosineSimilarityUtil.parseVector(
                            chunk.getVectorData());

                    double score =
                        CosineSimilarityUtil.cosineSimilarity(
                            queryVector, chunkVector);

                    return new EmbeddingSearchResultDto(
                        chunk.getChunkId(),
                        chunk.getFileId(),
                        chunk.getChunkText(),
                        score
                    );
                })
                .sorted(Comparator.comparing(
                    EmbeddingSearchResultDto::getSimilarityScore)
                    .reversed())
                .limit(topK)
                .collect(Collectors.toList());
    }
}
