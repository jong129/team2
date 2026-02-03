package dev.jpa.team2.checklist.ai;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import dev.jpa.team2.checklist.dto.PostChecklistReviewResponse;
import dev.jpa.team2.checklist.dto.PostChecklistSummaryDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostChecklistReviewAiClient {

  private final RestTemplate restTemplate;

  @Value("${llm.base-url}")
  private String aiServerUrl;

  /**
   * 진행 중 리뷰 (NOT_DONE 기준)
   */
  public PostChecklistReviewResponse review(int total, int done, List<Map<String, Object>> notDoneItems) {
    Map<String, Object> request = Map.of("total", total, "done", done, "notDoneItems", notDoneItems);

    return restTemplate.postForObject(aiServerUrl + "/checklist/post/review", request,
        PostChecklistReviewResponse.class);
  }

  /**
   * 완료 요약 (DONE / NOT_REQUIRED 기준)
   */
  public PostChecklistSummaryDto summarize(int total, int done, List<Map<String, Object>> completedItems) {
    Map<String, Object> request = Map.of("total", total, "done", done, "completedItems", completedItems);

    return restTemplate.postForObject(aiServerUrl + "/checklist/post/summary", request, PostChecklistSummaryDto.class);
  }

}
