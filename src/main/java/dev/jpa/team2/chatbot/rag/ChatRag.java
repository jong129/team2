package dev.jpa.team2.chatbot.rag;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "CHAT_RAG")
@Getter
@NoArgsConstructor
@SequenceGenerator(
    name = "CHAT_RAG_SEQ",
    sequenceName = "SEQ_CHAT_RAG_ID",
    allocationSize = 1
)
public class ChatRag {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "CHAT_RAG_SEQ"
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

    public ChatRag(Long sessionId, String question, String answer) {
        this.sessionId = sessionId;
        this.question = question;
        this.answer = answer;
        this.createdAt = LocalDateTime.now();
    }
}
