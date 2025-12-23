package dev.jpa.team2.chatbot;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@SequenceGenerator(
    name = "CHAT_HISTORY_SEQ_GEN",
    sequenceName = "SEQ_CHAT_HISTORY_ID",
    allocationSize = 1
)
public class ChatHistory {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "CHAT_HISTORY_SEQ_GEN"
    )
    @Column(name = "CHAT_ID")
    private Long chatId;

    @Column(name = "SESSION_ID", nullable = false)
    private Long sessionId;

    @Lob
    @Column(name = "QUESTION", nullable = false)
    private String question;

    @Lob
    @Column(name = "ANSWER", nullable = false)
    private String answer;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    public ChatHistory(Long sessionId, String question, String answer) {
        this.sessionId = sessionId;
        this.question = question;
        this.answer = answer;
        this.createdAt = LocalDateTime.now();
    }
}