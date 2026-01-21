package dev.jpa.team2.board_ai;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiResultResponse {
    private Long aiAnalysisId;
    private Long boardId;
    private Long categoryId;
    private String aiType;

    private String resultText;
    private Double score;

    private String promptCode;
    private String modelName;

    private boolean cached;
}
