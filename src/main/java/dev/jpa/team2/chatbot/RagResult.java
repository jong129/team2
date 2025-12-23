package dev.jpa.team2.chatbot;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "RAG_RESULT")
@Getter
@NoArgsConstructor
@SequenceGenerator(
    name = "RAG_SEQ_GEN",
    sequenceName = "SEQ_RAG_ID",
    allocationSize = 1
)
public class RagResult {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "RAG_SEQ_GEN"
    )
    @Column(name = "RAG_ID")
    private Long ragId;

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

    public RagResult(Long sessionId, String question, String answer) {
        this.sessionId = sessionId;
        this.question = question;
        this.answer = answer;
        this.createdAt = LocalDateTime.now();
    }
}
