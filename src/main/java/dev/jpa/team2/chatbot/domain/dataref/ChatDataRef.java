package dev.jpa.team2.chatbot.domain.dataref;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

// 문서 분석 결과/체크리스트 결과 같은 컨텍스트 요약을 특정 세션(sessionId)에 붙여 저장하는 테이블 매핑

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
    @Column(name = "DATA_REF_ID")
    private Long dataRefId;

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

    @PrePersist // 저장 직전에 createdAt이 비어 있으면 현재시간 자동 세팅
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
