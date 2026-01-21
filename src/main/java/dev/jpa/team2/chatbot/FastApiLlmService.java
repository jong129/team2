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

// Spring에서 임베딩 생성, RAG 질의, 답변 생성, 제목 생성을 FastAPI에 위임하는 서비스

@Service
@RequiredArgsConstructor
public class FastApiLlmService {

    private final RestTemplate restTemplate;  // Spring의 HTTP 클라이언트, FastAPI 서버로 REST 요청을 보냄

    @Value("${llm.base-url}")
    private String baseUrl; // FastAPI 서버 주소

    // embedding: text -> List<Double> : 텍스트 -> 임베딩 벡터
    public List<Double> embedding(String text) {
        String url = baseUrl + "/embeddings"; // 요청 URL

        HttpHeaders headers = new HttpHeaders(); 
        headers.setContentType(MediaType.APPLICATION_JSON); // JSON 헤더 설정

        Map<String, Object> body = Map.of("text", text);  // 요청 바디 구성
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

        ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.POST, req, Map.class);  // HTTP POST 실행

        Map<?, ?> resBody = (res == null) ? null : res.getBody();
        if (resBody == null) return List.of();

        Object emb = resBody.get("embedding");  // 응답 안전 파싱
        if (!(emb instanceof List<?> raw)) return List.of();

        return raw.stream().map(v -> ((Number) v).doubleValue()).toList();  // List<?> -> List<Double> 변환
    }

    // (레거시) chat: context+question -> answer : 이전 방식의 단순 chat API, 내부적으로 ask()를 호출
    public String chat(String context, String question) {
        Map<String, Object> res = ask(question, context, null, null, null);
        Object a = res.get("answer");
        return a == null ? null : String.valueOf(a);
    }

    /**
     * RAG 기반 질문 응답 API : Python FastAPI /ask 엔드포인트 호출 
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
        body.put("top_k", (topK == null ? 5 : topK)); // topK 기본값 처리

        if (docType != null && !docType.isBlank()) body.put("doc_type", docType); // 선택 파라미터만 전송
        if (stage != null && !stage.isBlank()) body.put("stage", stage);

        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.POST, req, Map.class);
            Map<String, Object> resBody = (res == null) ? null : res.getBody();
            return resBody == null ? Collections.emptyMap() : resBody;
        } catch (RestClientException e) { // 장애 대비
            // FastAPI 다운/타임아웃 등
            return Collections.emptyMap();
        }
    }

    // 대화 내용 -> 세션 제목 자동 생성 : Python /title API 호출
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
