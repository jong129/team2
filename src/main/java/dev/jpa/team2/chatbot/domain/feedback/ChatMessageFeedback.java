package dev.jpa.team2.chatbot.domain.feedback;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "CHAT_MESSAGE_FEEDBACK",
    uniqueConstraints = {
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
