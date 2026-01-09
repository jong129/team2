package dev.jpa.team2.chatbot;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
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

        Map<?, ?> resBody = (res == null) ? null : res.getBody();
        if (resBody == null) return List.of();

        Object emb = resBody.get("embedding");
        if (!(emb instanceof List<?> raw)) return List.of();

        return raw.stream().map(v -> ((Number) v).doubleValue()).toList();
    }

    /**
     * (레거시) chat: context+question -> answer
     * - 운영에서는 ask()를 쓰는 게 더 좋음(top_k/doc_type/stage 지원)
     */
    public String chat(String context, String question) {
        Map<String, Object> res = ask(question, context, null, null, null);
        Object a = res.get("answer");
        return a == null ? null : String.valueOf(a);
    }

    /**
     * ✅ Python /ask:
     * Request: {question, context, top_k, doc_type, stage}
     * Response: {answer, references, followUpQuestions, model, tokensIn, tokensOut}
     */
    public Map<String, Object> ask(String question, String context, Integer topK, String docType, String stage) {
        String url = baseUrl + "/ask";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("question", question);
        body.put("context", context);
        body.put("top_k", (topK == null ? 5 : topK));

        if (docType != null && !docType.isBlank()) body.put("doc_type", docType);
        if (stage != null && !stage.isBlank()) body.put("stage", stage);

        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.POST, req, Map.class);
            Map<String, Object> resBody = (res == null) ? null : res.getBody();
            return resBody == null ? Collections.emptyMap() : resBody;
        } catch (RestClientException e) {
            // FastAPI 다운/타임아웃 등
            return Collections.emptyMap();
        }
    }

    // ✅ Python /title: {raw} -> {title}
    public String makeTitle(String raw) {
        String url = baseUrl + "/title";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of("raw", raw);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.POST, req, Map.class);

            Map<?, ?> resBody = (res == null) ? null : res.getBody();
            if (resBody == null) return null;

            Object t = resBody.get("title");
            return t == null ? null : String.valueOf(t);
        } catch (RestClientException e) {
            return null;
        }
    }
}
