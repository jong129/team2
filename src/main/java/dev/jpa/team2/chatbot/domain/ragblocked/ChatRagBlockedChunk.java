package dev.jpa.team2.chatbot.domain.ragblocked;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

// 차단 chunk를 저장하는 테이블 엔티티

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table( // 같은 chunk를 중복 차단 레코드로 여러 개 만들 수 없게 DB에서 막음
    name = "CHAT_RAG_BLOCKED_CHUNK",
    uniqueConstraints = @UniqueConstraint(name = "UK_CHAT_RBC_CHUNK", columnNames = "CHUNK_ID") 
)
public class ChatRagBlockedChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RBC_SEQ")
    @SequenceGenerator(name = "RBC_SEQ", sequenceName = "SEQ_CHAT_RBC_ID", allocationSize = 1)
    @Column(name = "BLOCK_ID")
    private Long blockId;

    @Column(name = "CHUNK_ID", nullable = false)
    private Long chunkId;

    @Column(name = "REASON", length = 200)
    private String reason;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Integer isActive; // 1 or 0

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (isActive == null) isActive = 1;
    }
    
    // 편의 메소드
    public void deactivate() { this.isActive = 0; }
    public void activate()   { this.isActive = 1; }
}
