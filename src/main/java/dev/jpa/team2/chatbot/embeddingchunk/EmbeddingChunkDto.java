package dev.jpa.team2.chatbot.embeddingchunk;

import lombok.*;

public class EmbeddingChunkDto {

    // =========================
    // 1) 청크 생성 요청
    // =========================
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private Long fileId;
        private String chunkText;
    }

    // =========================
    // 2) 청크 생성 응답
    // =========================
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateResponse {
        private boolean success;
        private Long chunkId;
    }

    // =========================
    // 3) 임베딩 검색 결과
    // =========================
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchResult {
        private Long chunkId;
        private Long fileId;
        private String chunkText;
        private Double similarityScore;

        public static SearchResult of(EmbeddingChunk chunk, double score) {
            return SearchResult.builder()
                .chunkId(chunk.getChunkId())
                .fileId(chunk.getFileId())
                .chunkText(chunk.getChunkText())
                .similarityScore(score)
                .build();
        }
    }
}
