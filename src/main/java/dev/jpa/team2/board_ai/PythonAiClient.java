package dev.jpa.team2.board_ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Component
public class PythonAiClient implements AiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${llm.base-url:http://localhost:8000}")
    private String baseUrl;

    @Override
    public PythonAiResponse summarize(String prompt, String title, String content) {
        String url = baseUrl + "/board/summary";
        PythonAiRequest req = new PythonAiRequest(prompt, title, content, true, 8000);
        return post(url, req);
    }

    @Override
    public PythonAiResponse sentiment(String prompt, String title, String content) {
        String url = baseUrl + "/board/sentiment";
        PythonAiRequest req = new PythonAiRequest(prompt, title, content, true, 8000);
        return post(url, req);
    }

    @Override
    public PythonAiResponse writeDraft(String prompt, String title, String content) {
        String url = baseUrl + "/board/write";
        PythonAiRequest req = new PythonAiRequest(prompt, title, content, true, 8000);
        return post(url, req);
    }

    // ✅ 추가: 이미지 판별
    @Override
    public PythonAiResponse moderateImage(String prompt, String imageBase64, String filename, String contentType) {
        String url = baseUrl + "/board/moderate-image";

        // DTO는 유지하되, FastAPI가 extra 필드를 싫어할 수 있으므로
        // 요청은 핵심 4개만 보내는 방식을 추천(아래 toSafeImageRequest 사용)
        PythonAiImageRequest req = new PythonAiImageRequest();
        req.setPrompt(prompt);
        req.setImageBase64(imageBase64);
        req.setFilename(filename);
        req.setContentType(contentType);

        return postImage(url, req);
    }

    private PythonAiResponse post(String url, PythonAiRequest req) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<PythonAiRequest> entity = new HttpEntity<>(req, headers);

            ResponseEntity<PythonAiResponse> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, PythonAiResponse.class);

            PythonAiResponse body = response.getBody();
            if (body == null || body.getResultText() == null || body.getResultText().isBlank()) {
                throw new ResponseStatusException(SERVICE_UNAVAILABLE, "AI response empty");
            }
            return body;

        } catch (Exception e) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "AI server not available: " + e.getMessage());
        }
    }

    private PythonAiResponse postImage(String url, PythonAiImageRequest req) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // ✅ FastAPI가 jsonMode/maxTokens 같은 extra 필드를 거부(422)할 수 있으니,
            // 여기서 payload를 핵심 4개만 가진 객체로 변환해서 보내는 게 가장 안전함.
            Object safeBody = toSafeImageRequest(req);

            HttpEntity<Object> entity = new HttpEntity<>(safeBody, headers);

            ResponseEntity<PythonAiResponse> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, PythonAiResponse.class);

            PythonAiResponse body = response.getBody();
            if (body == null || body.getResultText() == null || body.getResultText().isBlank()) {
                throw new ResponseStatusException(SERVICE_UNAVAILABLE, "AI response empty");
            }
            return body;

        } catch (Exception e) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "AI server not available: " + e.getMessage());
        }
    }

    // ✅ 전송 payload는 4필드만(가장 안전)
    private Object toSafeImageRequest(PythonAiImageRequest req) {
        return new Object() {
            public final String prompt = req.getPrompt();
            public final String imageBase64 = req.getImageBase64();
            public final String filename = req.getFilename();
            public final String contentType = req.getContentType();
        };
    }
}

