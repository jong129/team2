package dev.jpa.team2.chatbot.domain.session;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CHAT_SESSION_SEQ")
    @SequenceGenerator(name = "CHAT_SESSION_SEQ", sequenceName = "SEQ_SESSION_ID", allocationSize = 1)
    @Column(name = "SESSION_ID")
    private Long sessionId;

    @Column(name = "MEMBER_ID", nullable = false)
    private Long memberId;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "SESSION_STATUS", nullable = false)
    private String sessionStatus; // ACTIVE / DELETED

    @Column(name = "START_TIME")
    private LocalDateTime startTime;

    @Column(name = "END_TIME")
    private LocalDateTime endTime;

    // 세션 리스트 정렬 핵심
    @Column(name = "LAST_MESSAGE_AT")
    private LocalDateTime lastMessageAt;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    @PrePersist // 자동 기본값 세팅
    public void prePersist() {
        if (this.sessionStatus == null) this.sessionStatus = "ACTIVE";
        if (this.startTime == null) this.startTime = LocalDateTime.now();
        if (this.lastMessageAt == null) this.lastMessageAt = this.startTime;
    }
}
