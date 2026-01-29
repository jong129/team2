package dev.jpa.team2.checklist.ai;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import dev.jpa.team2.checklist.dto.PostChecklistReviewResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostChecklistReviewAiClient {

  private final RestTemplate restTemplate;

  @Value("${llm.base-url}")
  private String aiServerUrl;

  public PostChecklistReviewResponse review(int total, int done, List<Map<String, Object>> notDoneItems) {
    Map<String, Object> request = Map.of("total", total, "done", done, "notDoneItems", notDoneItems);

    return restTemplate.postForObject(aiServerUrl + "/checklist/post/review", request,
        PostChecklistReviewResponse.class);
  }
}
