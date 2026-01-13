package dev.jpa.team2.chatbot.domain.feedback;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

// 한 사용자가 특정 메시지(chatId)에 남긴 평가(좋아요/싫어요)를 저장하는 테이블 매핑 엔티티

@Entity
@Table(
    name = "CHAT_MESSAGE_FEEDBACK",
    uniqueConstraints = {   // 한 사람이 같은 메시지에 좋아요를 여러 번 누르거나 레코드 중복 성생 방지
        @UniqueConstraint(name = "UK_CMF_MEMBER_CHAT", columnNames = {"MEMBER_ID", "CHAT_ID"})
    }
)
@Getter @Setter
@NoArgsConstructor
public class ChatMessageFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CMF_SEQ")
    @SequenceGenerator(
        name = "CMF_SEQ",
        sequenceName = "SEQ_CHAT_MESSAGE_FEEDBACK_ID",
        allocationSize = 1
    )
    @Column(name = "FEEDBACK_ID")
    private Long feedbackId;

    @Column(name = "CHAT_ID", nullable = false)
    private Long chatId;

    @Column(name = "MEMBER_ID", nullable = false)
    private Long memberId;

    // +1(좋아요), -1(싫어요)
    @Column(name = "VALUE", nullable = false)
    private Integer value;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;
    
    // 생명주기 콜백
    @PrePersist // createdAt/updatedAt 자동 세팅
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }
    @PreUpdate  // updatedAt 자동 갱신
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 생성 핼퍼 : 서비스에서 저장할 때 new 세팅 반복을 줄이기 위한 팩토리 메서드
    public static ChatMessageFeedback of(Long chatId, Long memberId, Integer value) {
        ChatMessageFeedback f = new ChatMessageFeedback();
        f.chatId = chatId;
        f.memberId = memberId;
        f.value = value;
        f.createdAt = LocalDateTime.now();
        f.updatedAt = LocalDateTime.now();
        return f;
    }
}
