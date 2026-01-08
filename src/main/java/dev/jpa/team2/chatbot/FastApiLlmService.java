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

    // ✅ Python /ask: {question, context, top_k, doc_type, stage} -> {answer, references, followUpQuestions}
    public Map<String, Object> ask(String question, String context, Integer topK, String docType, String stage) {
        String url = baseUrl + "/ask";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new java.util.HashMap<>();
        body.put("question", question);
        body.put("context", context);
        body.put("top_k", (topK == null ? 5 : topK));
        if (docType != null && !docType.isBlank()) body.put("doc_type", docType);
        if (stage != null && !stage.isBlank()) body.put("stage", stage);

        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

        ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.POST, req, Map.class);
        return res.getBody();
    }

    // ✅ Python /title: {raw} -> {title}
    public String makeTitle(String raw) {
        String url = baseUrl + "/title";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of("raw", raw);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

        ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.POST, req, Map.class);

        Object t = (res.getBody() == null) ? null : res.getBody().get("title");
        return t == null ? null : String.valueOf(t);
    }
}
