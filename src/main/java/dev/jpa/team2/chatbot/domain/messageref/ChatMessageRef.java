package dev.jpa.team2.chatbot.domain.messageref;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

// 답변 메시지 1개(chatId) <-> 근거 청크 여러개(chunkId) 매핑을 저장하는 테이블 엔티티
// AI 답변 근거를 UI로 투명하게 보여줄 수 있고, 이후 품질 분석(싫어요 많이 받은 답변에 자주 등장하는 chunk 찾기)도 가능

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
    
    // AI 답변 메시지 ID
    @Column(name="CHAT_ID", nullable = false)
    private Long chatId;  
    
    // 문서 근거
    @Column(name = "CHUNK_ID", nullable = false)
    private Long chunkId; 
    
    // 검색 결과 순위
    @Column(name="RANK_NO", nullable=false)
    private Integer rankNo;
    
    // 유사도 점수
    @Column(name = "SCORE", nullable = false)
    private Double score; 

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist // createdAt 비었으면 now() 자동 세팅
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
