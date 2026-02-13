package dev.jpa.team2.checklist.ai;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import dev.jpa.team2.checklist.dto.PreRiskExplanationDto;
import lombok.RequiredArgsConstructor;

/**
 * ============================================
 * PRE 위험 설명 생성 AI 클라이언트
 * - FastAPI /checklist/pre/risk/explanation 호출
 * ============================================
 */
@Service
@RequiredArgsConstructor
public class PreRiskExplanationAiClient {

  private final RestTemplate restTemplate;

  @Value("${llm.base-url}")
  private String aiServerUrl;

  public PreRiskExplanationDto generateExplanation(
      Double riskScoreSum,
      List<String> reasons
  ) {

    Map<String, Object> request = Map.of(
        "riskScoreSum", riskScoreSum,
        "reasons", reasons
    );

    try {
      return restTemplate.postForObject(
          aiServerUrl + "/checklist/pre/risk/explanation",
          request,
          PreRiskExplanationDto.class
      );

    } catch (ResourceAccessException e) {
      // ⚠️ AI 서버 실패 → Service에서 fallback
      return null;
    }
  }
}
