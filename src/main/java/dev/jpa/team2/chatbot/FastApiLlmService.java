package dev.jpa.team2.chatbot;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FastApiLlmService {

    private final RestTemplate restTemplate;

    @Value("${llm.base-url}")
    private String baseUrl;

    // embedding: text -> List<Double>
    public List<Double> embedding(String text) {
        String url = baseUrl + "/embeddings";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of("text", text);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

        ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.POST, req, Map.class);

        Object emb = res.getBody().get("embedding");
        // Jackson이 List<Number>로 파싱해주므로 캐스팅 처리
        List<?> raw = (List<?>) emb;
        return raw.stream().map(v -> ((Number) v).doubleValue()).toList();
    }

    // chat: context+question -> answer
    public String chat(String context, String question) {
        String url = baseUrl + "/ask";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
            "context", context,
            "question", question
        );

        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

        ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.POST, req, Map.class);
        return (String) res.getBody().get("answer");
    }
}
