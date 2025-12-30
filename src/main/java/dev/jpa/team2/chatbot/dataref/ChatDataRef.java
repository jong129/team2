package dev.jpa.team2.chatbot.dataref;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CHAT_DATA_REF")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatDataRef {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CHAT_DATA_REF_ID")
    @SequenceGenerator(name = "SEQ_CHAT_DATA_REF_ID", sequenceName = "SEQ_CHAT_DATA_REF_ID", allocationSize = 1)
    @Column(name = "REF_ID")
    private Long refId;

    @Column(name = "MEMBER_ID", nullable = false)
    private Long memberId;

    @Column(name = "SESSION_ID", nullable = false)
    private Long sessionId;

    @Column(name = "REF_TYPE", nullable = false, length = 50)
    private String refType;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Lob
    @Column(name = "SUMMARY", nullable = false)
    private String summary;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
