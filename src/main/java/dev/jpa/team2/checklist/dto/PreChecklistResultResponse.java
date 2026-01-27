package dev.jpa.team2.checklist.dto;

import java.util.List;

import dev.jpa.team2.checklist.ai.dto.ChecklistScoreResult;
import lombok.Getter;
import lombok.Setter;

/**
 * PRE 체크리스트 결과 응답 DTO
 */
@Getter
@Setter
public class PreChecklistResultResponse {

  private String postGroupCode;
  private Double riskScoreSum;
  private List<String> highRiskItemIds;
  private String message;

  // 요약 (상위 3개)
  private PreRiskExplanationDto riskExplanation;

  // ⭐ 자세히 보기용 (전체)
  private List<ChecklistScoreResult> riskAnalysisItems;
}

