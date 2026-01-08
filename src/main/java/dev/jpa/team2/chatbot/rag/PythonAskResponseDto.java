package dev.jpa.team2.chatbot.rag;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PythonAskResponseDto {
    private String answer;
    private List<RagReferenceDto> references;
    private List<String> followUpQuestions;
}
