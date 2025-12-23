package dev.jpa.team2.chatbot;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CHAT_SESSION_SEQ")
    @SequenceGenerator(name = "CHAT_SESSION_SEQ", sequenceName = "SEQ_SESSION_ID", allocationSize = 1)
    @Column(name = "SESSION_ID")
    private Long sessionId;

    @Column(name = "MEMBER_ID", nullable = false)
    private Long memberId;

    @Column(name = "START_TIME")
    private LocalDateTime startTime;

    @Column(name = "END_TIME")
    private LocalDateTime endTime;

    @Column(name = "SESSION_STATUS")
    private String sessionStatus;

    @Column(name = "TITLE")
    private String title;
}
