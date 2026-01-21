package dev.jpa.team2.checklist.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import dev.jpa.team2.checklist.ai.dto.ChecklistScoreRequest;
import dev.jpa.team2.checklist.ai.dto.ChecklistScoreResponse;
import lombok.RequiredArgsConstructor;

/**
 * ============================================
 * Checklist AI 중요도 스코어링 클라이언트
 * - FastAPI /checklist/ai/score 호출 전용
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

        ChecklistScoreResponse response =
            restTemplate.postForObject(
                aiServerUrl + "/checklist/ai/score",
                request,
                ChecklistScoreResponse.class
            );

        System.out.println("AI SCORE RESPONSE = " + response);
        System.out.println("===== AI SCORE CALL END =====");

        return response;
    }
}
