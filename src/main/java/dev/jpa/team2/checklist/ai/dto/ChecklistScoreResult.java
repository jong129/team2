package dev.jpa.team2.checklist.ai.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * AI 중요도 스코어링 결과 DTO (항목별)
 */
@Getter
@Setter
public class ChecklistScoreResult {

    private Long itemId;
    private Double importanceScore;
    private String reason;
}
