package dev.jpa.team2.chatbot.api.response;

import java.util.List;

import dev.jpa.team2.chatbot.domain.message.ChatMessageDto;
import lombok.*;

// 역활 : DB 모델이 아니라 프론트 요구사항을 만족시키기 위한 API 응답 모델

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessagesResponseDto {        
    private Long sessionId;
    private List<ChatMessageDto> messages;
}
