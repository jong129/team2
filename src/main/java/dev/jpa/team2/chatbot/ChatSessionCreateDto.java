package dev.jpa.team2.chatbot;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChatSessionCreateDto {
    private Long memberId;
    private String title;
}
