package dev.jpa.team2.chatbot.messageref;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CHAT_MESSAGE_REF")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRef {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CHAT_MESSAGE_REF_ID")
    @SequenceGenerator(name = "SEQ_CHAT_MESSAGE_REF_ID", sequenceName = "SEQ_CHAT_MESSAGE_REF_ID", allocationSize = 1)
    @Column(name = "MESSAGE_REF_ID")
    private Long messageRefId;

    @Column(name="CHAT_ID", nullable = false)
    private Long chatId;  // AI 답변 메시지 ID

    @Column(name = "CHUNK_ID", nullable = false)
    private Long chunkId; // 문서 근거

    @Column(name="RANK_NO", nullable=false)
    private Integer rankNo;
    
    @Column(name = "SCORE", nullable = false)
    private Double score; // 유사도 점수

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
