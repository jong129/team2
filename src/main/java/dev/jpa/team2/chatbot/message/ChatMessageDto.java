package dev.jpa.team2.chatbot.message;

import java.time.LocalDateTime;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private Long chatId;
    private String role;     // user / ai 
    private String content;
    private LocalDateTime createdAt;

    public static ChatMessageDto from(ChatMessage e) {
        return new ChatMessageDto(
            e.getChatId(),
            e.getRole(),
            e.getContent(),
            e.getCreatedAt()
        );
    }
}
