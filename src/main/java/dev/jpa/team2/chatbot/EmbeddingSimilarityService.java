package dev.jpa.team2.chatbot;

import java.util.List;

import org.springframework.stereotype.Service;

import dev.jpa.team2.chatbot.embeddingchunk.EmbeddingChunkDto;
import dev.jpa.team2.chatbot.embeddingchunk.EmbeddingChunkRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmbeddingSimilarityService {

    private final EmbeddingChunkRepository embeddingChunkRepository;

    public List<EmbeddingChunkDto.SearchResult> searchTopK(List<Double> queryVector, int topK) {

        int safeTopK = Math.max(1, Math.min(topK, 50));

        return embeddingChunkRepository.findAll().stream()
            .map(chunk -> {
                List<Double> chunkVector = CosineSimilarityUtil.parseVector(chunk.getVectorData());
                double score = CosineSimilarityUtil.cosineSimilarity(queryVector, chunkVector);
                return EmbeddingChunkDto.SearchResult.of(chunk, score);
            })
            .sorted((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()))
            .limit(safeTopK)
            .toList();
      }
}
