package dev.jpa.team2.board_ai;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PythonAiResponse {
    private String resultText;
    private Double score;
    private String modelName;

    private Integer tokensIn;
    private Integer tokensOut;
    private Integer tokensTotal;
    private Integer latencyMs;
}
