package dev.jpa.team2.chatbot;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CHAT_MESSAGE")
@Getter @Setter
@NoArgsConstructor
public class ChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CHAT_MESSAGE_SEQ")
    @SequenceGenerator(
        name = "CHAT_MESSAGE_SEQ",
        sequenceName = "SEQ_CHAT_MESSAGE_ID",
        allocationSize = 1
    )
    @Column(name = "MESSAGE_ID")
    private Long chatId; // 기존 코드 호환 위해 변수명 chatId 유지(원하면 messageId로 바꿔도 됨)

    @Column(name = "SESSION_ID", nullable = false)
    private Long sessionId;

    @Column(name = "ROLE", nullable = false)
    private String role; // USER / ASSISTANT / SYSTEM

    @Lob
    @Column(name = "CONTENT", nullable = false)
    private String content;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    // 선택(나중에 분석/과금/성능로그)
    @Column(name = "MODEL")
    private String model;

    @Column(name = "TOKENS_IN")
    private Integer tokensIn;

    @Column(name = "TOKENS_OUT")
    private Integer tokensOut;

    public static ChatHistory of(Long sessionId, String role, String content) {
        ChatHistory m = new ChatHistory();
        m.sessionId = sessionId;
        m.role = role;
        m.content = content;
        m.createdAt = LocalDateTime.now();
        return m;
    }
}
