package dev.jpa.team2.checklist.service;

import java.util.List;

import org.springframework.stereotype.Service;

import dev.jpa.team2.checklist.ai.PreRiskExplanationAiClient;
import dev.jpa.team2.checklist.dto.PreRiskExplanationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PreRiskExplanationAiService {

  // ✅ FastAPI 연동 클라이언트
  private final PreRiskExplanationAiClient preRiskExplanationAiClient;

  /**
   * PRE 체크리스트 결과를 "3줄 이내 요약된 위험 설명"으로 변환
   *
   * - AI 서버 장애 / reason 없음 → 기본 설명 반환 - riskScoreSum null → 프로그래밍 오류 (예외)
   */
  public PreRiskExplanationDto generateExplanation(Double riskScoreSum, List<String> aiReasons) {

    // 1️⃣ 필수 값 검증 (절대 null이면 안 됨)
    if (riskScoreSum == null) {
      throw new IllegalStateException("riskScoreSum is null");
    }

    // 2️⃣ reason 없음 → AI 호출 ❌, 기본 설명 ⭕
    if (aiReasons == null || aiReasons.isEmpty()) {
      return buildDefaultExplanation(riskScoreSum);
    }

    // 3️⃣ FastAPI 호출
    try {
      PreRiskExplanationDto result = preRiskExplanationAiClient.generateExplanation(riskScoreSum, aiReasons);

      // 4️⃣ AI 실패(null) → fallback
      if (result == null) {
        log.warn("PRE 위험 설명 AI 응답 null → 기본 설명으로 대체");
        return buildDefaultExplanation(riskScoreSum);
      }

      return result;

    } catch (Exception e) {
      // 5️⃣ 예외 발생 → fallback
      log.warn("PRE 위험 설명 AI 호출 중 예외 발생 → 기본 설명으로 대체", e);
      return buildDefaultExplanation(riskScoreSum);
    }
  }

  /**
   * AI 실패 / reason 없음 시 사용하는 기본 설명
   */
  private PreRiskExplanationDto buildDefaultExplanation(Double riskScoreSum) {

    PreRiskExplanationDto dto = new PreRiskExplanationDto();

    dto.setSummary(riskScoreSum >= 70 ? "사전 점검 결과, 일부 항목에서 주의가 필요한 상황입니다." : "사전 점검 결과, 추가 확인이 권장되는 항목이 있습니다.");

    dto.setReasons(List.of());

    dto.setActions(List.of("계약 전 주요 점검 항목을 다시 한 번 확인해 주세요."));

    return dto;
  }
}
