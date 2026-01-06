package dev.jpa.team2.chatbot.message;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CHAT_MESSAGE")
@Getter @Setter
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CHAT_MESSAGE_SEQ")
    @SequenceGenerator(
        name = "CHAT_MESSAGE_SEQ",
        sequenceName = "SEQ_CHAT_MESSAGE_ID",
        allocationSize = 1
    )
    @Column(name = "CHAT_ID")
    private Long chatId; 

    @Column(name = "SESSION_ID", nullable = false)
    private Long sessionId;

    @Column(name = "ROLE", nullable = false)
    private String role; // USER / ASSISTANT / SYSTEM

    @Lob
    @Column(name = "CONTENT", nullable = false)
    private String content;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    // 선택(나중에 추가 가능 : 분석/과금/성능로그)
    @Column(name = "MODEL")
    private String model;

    @Column(name = "TOKENS_IN")
    private Integer tokensIn;

    @Column(name = "TOKENS_OUT")
    private Integer tokensOut;
    
    @Column(name = "LIKE_COUNT", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "DISLIKE_COUNT", nullable = false)
    private Integer dislikeCount = 0;

    public static ChatMessage of(Long sessionId, String role, String content) {
        ChatMessage m = new ChatMessage();
        m.sessionId = sessionId;
        m.role = role;
        m.content = content;
        m.createdAt = LocalDateTime.now();
        return m;
    }
}
