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

    /**
     * LLM #3: 글 초안 생성(WRITE)
     * - 입력(대충 쓴 title/content)을 기반으로 제목+본문 초안을 생성
     * - FastAPI 쪽에 /board/write 엔드포인트가 있어야 함
     */
    @Override
    public PythonAiResponse writeDraft(String prompt, String title, String content) {
        String url = baseUrl + "/board/write";
        PythonAiRequest req = new PythonAiRequest(prompt, title, content, true, 8000);
        return post(url, req);
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
}


