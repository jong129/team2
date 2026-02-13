package dev.jpa.team2.checklist.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import dev.jpa.team2.checklist.ai.dto.ChecklistScoreRequest;
import dev.jpa.team2.checklist.ai.dto.ChecklistScoreResponse;
import lombok.RequiredArgsConstructor;

/**
 * ============================================ Checklist AI 중요도 스코어링 클라이언트 -
 * FastAPI /checklist/ai/score 호출 전용
 * ============================================
 */
@Service
@RequiredArgsConstructor
public class ChecklistAiScoreClient {

  private final RestTemplate restTemplate;

  @Value("${llm.base-url}")
  private String aiServerUrl;

  /**
   * 체크리스트 항목 중요도 점수 조회
   */
  public ChecklistScoreResponse scoreChecklistItems(ChecklistScoreRequest request) {

    System.out.println("===== AI SCORE CALL START =====");
    System.out.println("AI SERVER URL = " + aiServerUrl);
    System.out.println("item count = " + request.getItems().size());

    try {
      ChecklistScoreResponse response = restTemplate.postForObject(aiServerUrl + "/checklist/ai/score", request,
          ChecklistScoreResponse.class);

      System.out.println("AI SCORE RESPONSE = " + response);
      System.out.println("===== AI SCORE CALL END =====");

      return response;

    } catch (ResourceAccessException e) {
      // ⚠️ AI 서버 연결 실패 (타임아웃, 서버 다운 등)
      System.out.println("⚠️ AI SCORE SERVER TIMEOUT - fallback 처리");
      System.out.println("ERROR = " + e.getMessage());

      // 👉 여기서 예외를 던지지 않는 것이 핵심
      return null;
    }
  }
}
