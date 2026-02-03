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

    /** 전체 위험도 점수 (0~100) */
    private Integer riskScore;

    /** 항목별 중요도 점수 */
    private List<ChecklistScoreResult> scores;
}
