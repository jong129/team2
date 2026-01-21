package dev.jpa.team2.checklist.ai.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * AI 중요도 스코어링 응답 DTO
 */
@Getter
@Setter
public class ChecklistScoreResponse {

    private List<ChecklistScoreResult> scores;
}
