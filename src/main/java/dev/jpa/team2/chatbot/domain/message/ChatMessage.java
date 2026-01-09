package dev.jpa.team2.chatbot.domain.message;

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

    @Column(name = "MEMBER_ID", nullable = false)
    private Long memberId;

    @Column(name = "SESSION_ID", nullable = false)
    private Long sessionId;

    @Column(name = "ROLE", nullable = false)
    private String role; // USER / ASSISTANT

    @Lob
    @Column(name = "CONTENT", nullable = false)
    private String content;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Setter(AccessLevel.NONE)
    @Column(name = "MODEL", nullable = false, length = 50)
    private String model = "gpt-4o-mini";

    @Column(name = "TOKENS_IN")
    private Integer tokensIn;

    @Column(name = "TOKENS_OUT")
    private Integer tokensOut;
    
    @Column(name="TOKENS_TOTAL")
    private Integer tokensTotal;

    @Column(name="LATENCY_MS")
    private Integer latencyMs;
    
    @Column(name = "LIKE_COUNT", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "DISLIKE_COUNT", nullable = false)
    private Integer dislikeCount = 0;

    // ✅ 추가: 추천 후속 질문 3개 저장 컬럼
    @Column(name = "SUGGEST_Q1", length = 300)
    private String suggestQ1;

    @Column(name = "SUGGEST_Q2", length = 300)
    private String suggestQ2;

    @Column(name = "SUGGEST_Q3", length = 300)
    private String suggestQ3;

    
    public void setModel(String model) {
      if (model == null || model.isBlank()) {
          this.model = "gpt-4o-mini";
      } else {
          this.model = model;
      }
    }
    
    public static ChatMessage of(Long memberId, Long sessionId, String role, String content) {
        ChatMessage m = new ChatMessage();
        m.memberId = memberId;
        m.sessionId = sessionId;
        m.role = role;
        m.content = content;
        m.createdAt = LocalDateTime.now();
        return m;
    }
}
