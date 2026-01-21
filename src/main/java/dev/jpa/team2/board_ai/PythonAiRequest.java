package dev.jpa.team2.board_ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PythonAiRequest {
    private String prompt;
    private String title;
    private String content;

    private boolean truncate;
    private int maxChars;
}
