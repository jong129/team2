package dev.jpa.team2.chatbot;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryDto {

    private Long chatId;
    private Long sessionId;
    private String question;
    private String answer;
    private LocalDateTime createdAt;

    public static ChatHistoryDto fromEntity(ChatHistory entity) {
        return new ChatHistoryDto(
            entity.getChatId(),
            entity.getSessionId(),
            entity.getQuestion(),
            entity.getAnswer(),
            entity.getCreatedAt()
        );
    }
}


