package dev.jpa.team2.checklist.ai.dto;

import java.util.Comparator;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 위험 분석 결과 (점수 + 근거) - 내부 서비스 전용 DTO
 */
@Getter
@AllArgsConstructor
public class AiRiskAnalysisResult {

  /** AI 계산 총 위험 점수 */
  private final double totalScore;

  /** AI가 판단한 위험 근거(reason) 목록 */
  private final List<String> reasons;

  /** 모든 미체크 항목 (정렬 가능) */
  private final List<ChecklistScoreResult> allResults;

  /** 요약용 상위 N개 */
  public List<ChecklistScoreResult> topResults(int limit) {
    return allResults.stream().filter(r -> r.getImportanceScore() != null)
        .sorted(Comparator.comparing(ChecklistScoreResult::getImportanceScore).reversed()).limit(limit).toList();
  }
}
