package dev.jpa.team2.chatbot.api.rag;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

// 역활 : Spring 서버가 FastAPI 서버를 호출했을 때, 그 응답 JSON을 자바 객체로 받기 위한 외부 연동 전용 DTO
// 사용 : FastApiLlmService 같은 클래스에서 RestTemplate.exchange() 결과를 매핑할 때

@Getter
@Setter
public class PythonAskResponseDto {            // FastAPI / 외부 연동 응답 DTO
    private String answer;
    private List<RagReferenceDto> references; // RAG 결과면
    private List<String> followUpQuestions;
}
