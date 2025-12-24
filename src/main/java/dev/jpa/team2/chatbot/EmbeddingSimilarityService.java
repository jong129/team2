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

    private final EmbeddingChunkRepository embeddingChunkRepository;

    public List<EmbeddingSearchResultDto> searchTopK(List<Double> queryVector, int topK) {

        return embeddingChunkRepository.findAll().stream()
            .map(chunk -> {
                List<Double> chunkVector = parseVector(chunk.getVectorData());
                double score = cosineSimilarity(queryVector, chunkVector);

                return new EmbeddingSearchResultDto(
                    chunk.getChunkId(),
                    chunk.getFileId(),
                    chunk.getChunkText(),
                    score
                );
            })
            .sorted((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()))
            .limit(topK)
            .toList();
    }

    // ✅ DB에 저장된 embedding_vector가 문자열(JSON)이라면 파싱 필요
    private List<Double> parseVector(String vectorJson) {
        // 예: "[0.1,0.2,0.3]" 형태라고 가정
        String cleaned = vectorJson.replace("[", "").replace("]", "").trim();
        if (cleaned.isEmpty()) return List.of();

        String[] parts = cleaned.split(",");
        return java.util.Arrays.stream(parts)
            .map(String::trim)
            .map(Double::parseDouble)
            .toList();
    }

    private double cosineSimilarity(List<Double> a, List<Double> b) {
        if (a.size() != b.size() || a.isEmpty()) return 0.0;

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.size(); i++) {
            double x = a.get(i);
            double y = b.get(i);
            dot += x * y;
            normA += x * x;
            normB += y * y;
        }

        double denom = Math.sqrt(normA) * Math.sqrt(normB);
        return denom == 0 ? 0.0 : dot / denom;
    }
}

