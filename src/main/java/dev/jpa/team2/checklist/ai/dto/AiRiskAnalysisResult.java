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

  /**
   * 모든 미이행 항목 중 가장 높은 importanceScore 반환
   *
   * @return 최고 importanceScore (없으면 0.0)
   */
  public double getMaxImportanceScore() {
    return allResults.stream().map(ChecklistScoreResult::getImportanceScore) // Integer
        .filter(score -> score != null).max(Integer::compareTo) // ✅ Integer 기준 비교
        .map(Integer::doubleValue) // ✅ 최종 반환은 double
        .orElse(0.0);
  }

  /**
   * 단일 항목이라도 주어진 임계치 이상의 고위험 항목이 존재하는지 여부
   *
   * @param threshold 단일 항목 위험 점수 기준 (예: 80.0)
   * @return true = 고위험 항목 존재
   */
  public boolean hasHighRiskItem(double threshold) {
    return getMaxImportanceScore() >= threshold;
  }
}
