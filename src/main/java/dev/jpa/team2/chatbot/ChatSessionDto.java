package dev.jpa.team2.chatbot;

import java.time.LocalDateTime;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionDto {
    private Long sessionId;
    private String title;
    private String sessionStatus;
    private LocalDateTime lastMessageAt;

    public static ChatSessionDto from(ChatSession s) {
        return new ChatSessionDto(
            s.getSessionId(),
            s.getTitle(),
            s.getSessionStatus(),
            s.getLastMessageAt()
        );
    }
}
