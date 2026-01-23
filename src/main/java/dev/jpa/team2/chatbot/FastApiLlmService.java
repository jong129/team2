package dev.jpa.team2.chatbot;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FastApiLlmService {

    private final RestTemplate restTemplate;

    @Value("${llm.base-url}")
    private String baseUrl;

    // =========================
    // embedding: text -> List<Double>
    // =========================
    public List<Double> embedding(String text) {
        String url = baseUrl + "/embeddings";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of("text", text);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

        log.info("[FASTAPI /embeddings req] url={} textLen={}", url, text == null ? 0 : text.length());

        try {
            ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.POST, req, Map.class);
            Map<?, ?> resBody = (res == null) ? null : res.getBody();

            log.info("[FASTAPI /embeddings res] status={} hasBody={}",
                res == null ? "null" : res.getStatusCode(),
                (resBody != null)
            );

            if (resBody == null) return List.of();

            Object emb = resBody.get("embedding");
            if (!(emb instanceof List<?> raw)) {
                log.warn("[FASTAPI /embeddings] embedding field not a list. type={}", emb == null ? "null" : emb.getClass().getName());
                return List.of();
            }

            return raw.stream().map(v -> ((Number) v).doubleValue()).toList();

        } catch (HttpStatusCodeException e) {
            log.error("[FASTAPI /embeddings] HTTP error status={} body={}", e.getStatusCode(), safeTrim(e.getResponseBodyAsString()), e);
            return List.of();
        } catch (ResourceAccessException e) {
            // timeout / connection refused 등
            log.error("[FASTAPI /embeddings] ResourceAccess error (timeout/connection) url={} err={}", url, e.toString(), e);
            return List.of();
        } catch (RestClientException e) {
            log.error("[FASTAPI /embeddings] RestClientException url={} err={}", url, e.toString(), e);
            return List.of();
        } catch (Exception e) {
            log.error("[FASTAPI /embeddings] Unexpected error url={} err={}", url, e.toString(), e);
            return List.of();
        }
    }

    // =========================
    // (레거시) chat -> ask 호출
    // =========================
    public String chat(String context, String question) {
        Map<String, Object> res = ask(question, context, null, null, null, null, null);
        Object a = res.get("answer");
        return a == null ? null : String.valueOf(a);
    }

    /**
     * ✅ (권장) RAG 기반 질문 응답 API : Python FastAPI /ask 엔드포인트 호출
     * FastAPI는 snake_case를 기대하므로 반드시 아래 키로 보냄:
     *  - question, context, top_k, doc_type, doc_id, stage, user_id
     *
     * 🔥 중요: 예외를 삼키면 (답변 없음)만 나오므로,
     *          실패 시에도 answer에 "실패 메시지"를 넣어 프론트/로그에서 원인 확인 가능하게 함.
     */
    public Map<String, Object> ask(
        String question,
        String context,
        Integer topK,
        String docType,
        String docId,
        String stage,
        String userId
    ) {
        String url = baseUrl + "/ask";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("question", question);
        body.put("top_k", (topK == null ? 5 : topK));

        // ✅ context가 null이면 키 자체를 제거(서버 422 방지)
        if (context != null && !context.isBlank()) body.put("context", context);

        // ✅ snake_case 필터들
        if (docType != null && !docType.isBlank()) body.put("doc_type", docType);
        if (docId != null && !docId.isBlank()) body.put("doc_id", docId);
        if (stage != null && !stage.isBlank()) body.put("stage", stage);
        if (userId != null && !userId.isBlank()) body.put("user_id", userId);

        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

        // 요청 로그 (길이만 찍어서 너무 길게 안 남기기)
        log.info(
            "[FASTAPI /ask req] url={} qLen={} ctxLen={} topK={} docType={} docId={} stage={} userId={}",
            url,
            question == null ? 0 : question.length(),
            context == null ? 0 : context.length(),
            (topK == null ? 5 : topK),
            docType,
            docId,
            stage,
            userId
        );

        try {
            ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.POST, req, Map.class);
            Map<String, Object> resBody = (res == null) ? null : res.getBody();

            log.info(
                "[FASTAPI /ask res] status={} hasBody={} keys={}",
                res == null ? "null" : res.getStatusCode(),
                (resBody != null),
                (resBody == null ? "null" : resBody.keySet())
            );

            if (resBody == null) {
                return failAsk("⚠️ FastAPI /ask 응답 바디가 비었습니다.(null)", null);
            }

            // answer가 없으면 경고 로그
            if (!resBody.containsKey("answer")) {
                log.warn("[FASTAPI /ask] response has no 'answer' field. keys={}", resBody.keySet());
            }

            return resBody;

        } catch (HttpStatusCodeException e) {
            // 4xx/5xx 서버 응답을 가장 정확하게 볼 수 있음
            String respBody = safeTrim(e.getResponseBodyAsString());
            log.error(
                "[FASTAPI /ask] HTTP error status={} respBody={} (qLen={} ctxLen={} docId={} userId={})",
                e.getStatusCode(),
                respBody,
                question == null ? 0 : question.length(),
                context == null ? 0 : context.length(),
                docId,
                userId,
                e
            );
            return failAsk("⚠️ FastAPI /ask HTTP 오류: " + e.getStatusCode(), respBody);

        } catch (ResourceAccessException e) {
            // timeout / connection refused 등
            log.error(
                "[FASTAPI /ask] ResourceAccess error (timeout/connection) url={} err={} (docId={} userId={})",
                url, e.toString(), docId, userId, e
            );
            return failAsk("⚠️ FastAPI /ask 연결/타임아웃 오류", e.getMessage());

        } catch (RestClientException e) {
            log.error(
                "[FASTAPI /ask] RestClientException url={} err={} (docId={} userId={})",
                url, e.toString(), docId, userId, e
            );
            return failAsk("⚠️ FastAPI /ask 호출 실패(RestClientException)", e.getMessage());

        } catch (Exception e) {
            log.error(
                "[FASTAPI /ask] Unexpected error url={} err={} (docId={} userId={})",
                url, e.toString(), docId, userId, e
            );
            return failAsk("⚠️ FastAPI /ask 호출 실패(Unexpected)", e.getMessage());
        }
    }

    /**
     * ✅ (호환 유지) 기존 시그니처 유지용 오버로드
     */
    public Map<String, Object> ask(String question, String context, Integer topK, String docType, String stage) {
        return ask(question, context, topK, docType, null, stage, null);
    }

    // =========================
    // title: raw -> title
    // =========================
    public String makeTitle(String raw) {
        String url = baseUrl + "/title";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of("raw", raw);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

        log.info("[FASTAPI /title req] url={} rawLen={}", url, raw == null ? 0 : raw.length());

        try {
            ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.POST, req, Map.class);
            Map<?, ?> resBody = (res == null) ? null : res.getBody();

            log.info("[FASTAPI /title res] status={} hasBody={}",
                res == null ? "null" : res.getStatusCode(),
                (resBody != null)
            );

            if (resBody == null) return null;
            Object t = resBody.get("title");
            return t == null ? null : String.valueOf(t);

        } catch (HttpStatusCodeException e) {
            log.error("[FASTAPI /title] HTTP error status={} body={}", e.getStatusCode(), safeTrim(e.getResponseBodyAsString()), e);
            return null;
        } catch (ResourceAccessException e) {
            log.error("[FASTAPI /title] ResourceAccess error (timeout/connection) url={} err={}", url, e.toString(), e);
            return null;
        } catch (RestClientException e) {
            log.error("[FASTAPI /title] RestClientException url={} err={}", url, e.toString(), e);
            return null;
        } catch (Exception e) {
            log.error("[FASTAPI /title] Unexpected error url={} err={}", url, e.toString(), e);
            return null;
        }
    }

    // =========================
    // helpers
    // =========================
    private Map<String, Object> failAsk(String msg, String detail) {
        Map<String, Object> out = new HashMap<>();
        out.put("answer", detail == null || detail.isBlank() ? msg : (msg + "\n" + detail));
        out.put("references", List.of());
        out.put("followUpQuestions", List.of());
        return out;
    }

    private String safeTrim(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.length() > 1200) return t.substring(0, 1200) + "...(truncated)";
        return t;
    }
}
