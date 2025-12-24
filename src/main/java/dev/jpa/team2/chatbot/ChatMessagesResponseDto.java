package dev.jpa.team2.chatbot;

import java.util.List;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessagesResponseDto {
    private Long sessionId;
    private List<ChatMessageDto> messages;
}
