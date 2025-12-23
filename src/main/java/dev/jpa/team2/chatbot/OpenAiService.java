package dev.jpa.team2.chatbot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model.chat}")
    private String chatModel;

    @Value("${openai.model.embedding}")
    private String embeddingModel;

    private static final String CHAT_URL = "https://api.openai.com/v1/chat/completions";
    private static final String EMBEDDING_URL = "https://api.openai.com/v1/embeddings";

    /**
     * 🔹 Embedding 생성
     */
    public String embedding(String text) {

      HttpHeaders headers = createHeaders();

      Map<String, Object> body = Map.of(
          "model", embeddingModel,
          "input", text
      );

      HttpEntity<Map<String, Object>> request =
          new HttpEntity<>(body, headers);

      ResponseEntity<Map> response =
          restTemplate.postForEntity(
              EMBEDDING_URL, request, Map.class);

      Map data = ((List<Map>) response.getBody().get("data")).get(0);

      List<Double> embedding =
          (List<Double>) data.get("embedding");

      return embedding.toString();
  }


    /**
     * 🔹 Chat Completion (RAG)
     */
    public String chat(String context, String question) {

        HttpHeaders headers = createHeaders();

        Map<String, Object> body = Map.of(
            "model", chatModel,
            "messages", List.of(
                Map.of("role", "system",
                       "content", "너는 문서를 기반으로 답변하는 AI다."),
                Map.of("role", "user",
                       "content",
                       "문서:\n" + context + "\n\n질문:\n" + question)
            )
        );

        HttpEntity<Map<String, Object>> request =
            new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
            restTemplate.postForEntity(
                CHAT_URL, request, Map.class);

        Map choice = ((List<Map>) response.getBody().get("choices")).get(0);
        Map message = (Map) choice.get("message");

        return (String) message.get("content");
    }

    /**
     * 공통 Header
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
