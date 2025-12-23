package dev.jpa.team2.chatbot;

import dev.jpa.team2.chatbot.EmbeddingSearchResultDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RagDto {

    // ===== 요청 =====
    private Long sessionId;
    private String question;

    // ===== 응답 =====
    private Long ragId;
    private String answer;
    private List<EmbeddingSearchResultDto> references;
}
