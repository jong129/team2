package dev.jpa.team2.chatbot.embeddingchunk;

import java.util.ArrayList;
import java.util.List;

public class CosineSimilarityUtil {

    /**
     * "[0.1, 0.2, -0.3]" → List<Double>
     */
  public static List<Double> parseVector(String vectorString) {
    if (vectorString == null) return List.of();

    String cleaned = vectorString.replace("[", "").replace("]", "").trim();
    if (cleaned.isEmpty()) return List.of();

    String[] tokens = cleaned.split(",");

    List<Double> vector = new ArrayList<>();
    for (String token : tokens) {
        String t = token.trim();
        if (t.isEmpty()) continue;
        vector.add(Double.parseDouble(t));
    }
    return vector;
  }


    /**
     * cosine similarity 계산
     */
    public static double cosineSimilarity(
            List<Double> v1,
            List<Double> v2) {

        if (v1.size() != v2.size()) {
            throw new IllegalArgumentException("Vector size mismatch");
        }

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < v1.size(); i++) {
            dot += v1.get(i) * v2.get(i);
            normA += Math.pow(v1.get(i), 2);
            normB += Math.pow(v2.get(i), 2);
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
