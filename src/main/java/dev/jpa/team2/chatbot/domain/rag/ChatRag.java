package dev.jpa.team2.chatbot.domain.rag;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

//. RAG 질문/답변을 별도의 로그 테이블 CHAT_RAG에 저장하는 엔티티
// CHAT_RAG를 따로 두는 이유 : RAG 호출 단위 로그를 따로 관리/보관 용이. 메시지와는 별개로 RAG 파이프라인 결과 추적 가능

@Entity
@Table(name = "CHAT_RAG")
@Getter
@NoArgsConstructor
public class ChatRag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CHAT_RAG_SEQ")
    @SequenceGenerator(name = "CHAT_RAG_SEQ", sequenceName = "SEQ_CHAT_RAG_ID", allocationSize = 1)
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
