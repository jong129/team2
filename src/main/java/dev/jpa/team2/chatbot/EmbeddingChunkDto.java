package dev.jpa.team2.chatbot;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmbeddingChunkDto {

    // ===== 요청 시 사용 =====
    private Long fileId;
    private String chunkText;

    // ===== 응답 시 채워짐 =====
    private Long chunkId;
}
