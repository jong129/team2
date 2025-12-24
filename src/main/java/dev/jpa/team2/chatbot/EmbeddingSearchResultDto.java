package dev.jpa.team2.chatbot;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmbeddingSearchResultDto {

    private Long chunkId;
    private Long fileId;
    private String chunkText;
    private Double similarityScore;
}
