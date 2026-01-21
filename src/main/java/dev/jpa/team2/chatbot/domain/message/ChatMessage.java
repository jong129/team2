package dev.jpa.team2.chatbot.domain.message;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CHAT_MESSAGE")
@Getter @Setter
@NoArgsConstructor
public class ChatMessage {
    // 식별 / 관계
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
    
    // 메시지 본문
    @Column(name = "ROLE", nullable = false)
    private String role; // USER / ASSISTANT

    @Lob
    @Column(name = "CONTENT", nullable = false)
    private String content;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;
    
    // 사용량 / 성능
    private static final String DEFAULT_MODEL = "gpt-4o-mini";
    
    @Setter(AccessLevel.NONE)
    @Column(name = "MODEL", nullable = false, length = 50)
    private String model = DEFAULT_MODEL;

    @Column(name = "TOKENS_IN")
    private Integer tokensIn;

    @Column(name = "TOKENS_OUT")
    private Integer tokensOut;
    
    @Column(name="TOKENS_TOTAL")
    private Integer tokensTotal;

    @Column(name="LATENCY_MS")
    private Integer latencyMs;
     
    // 피드백 집계 
    @Column(name = "LIKE_COUNT", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "DISLIKE_COUNT", nullable = false)
    private Integer dislikeCount = 0;

    // 추천 후속 질문 3개 저장 컬럼
    @Column(name = "SUGGEST_Q1", length = 300)
    private String suggestQ1;

    @Column(name = "SUGGEST_Q2", length = 300)
    private String suggestQ2;

    @Column(name = "SUGGEST_Q3", length = 300)
    private String suggestQ3;

    // model은 기본값을 가지고, setModel()에서 null/blank면 기본값으로 보정
    public void setModel(String model) {
      if (model == null || model.isBlank()) {
          this.model = DEFAULT_MODEL;
      } else {
          this.model = model.trim();
      }
  }
    
    // 생성 경로가 어디든 상관없이 INSERT 직전에 createdAt이 비어있으면 자동으로 채움
    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // 팩토리로 기본 필드 생성
    public static ChatMessage of(Long memberId, Long sessionId, String role, String content) {
        ChatMessage m = new ChatMessage();
        m.memberId = memberId;
        m.sessionId = sessionId;
        m.role = (role == null) ? null : role.trim().toUpperCase(); // 대문자 통일
        m.content = content;
        return m;
    }
}
