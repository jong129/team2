package dev.jpa.team2.checklist.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CHECKLIST_SESSION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistSession {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CHECKLIST_SESSION_ID")
    @SequenceGenerator(
            name = "SEQ_CHECKLIST_SESSION_ID",
            sequenceName = "SEQ_CHECKLIST_SESSION_ID",
            allocationSize = 1
    )
    @Column(name = "SESSION_ID")
    private Long sessionId;

    @Column(name = "MEMBER_ID", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "PHASE", nullable = false, length = 10)
    private Phase phase; // PRE / POST

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEMPLATE_ID", nullable = false)
    private ChecklistTemplate template;

    /**
     * POST 세션일 때만 값이 있어야 함 (DB CHECK 제약 조건 존재)
     * PRE 세션이면 반드시 NULL
     */
    @Column(name = "PROFILE_KEY_ID")
    private Long profileKeyId; // PRE는 null, POST에서만 값 사용

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status; // IN_PROGRESS / COMPLETED

    @Column(name = "STARTED_AT", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "COMPLETED_AT")
    private LocalDateTime completedAt;
    
    @Column(name = "DELETED_YN", nullable = false, length = 1)
    private String deletedYn = "N";   // 'Y'/'N'

    @Column(name = "DELETED_AT")
    private java.util.Date deletedAt;


    @PrePersist
    public void prePersist() {
        // DB DEFAULT SYSDATE가 있어도, JPA가 null로 insert하면 DB default가 안 먹는 경우가 있어
        // 그래서 애초에 엔티티에서 값 세팅해주는 게 안전
        if (this.startedAt == null) this.startedAt = LocalDateTime.now();
        if (this.status == null) this.status = "IN_PROGRESS";
    }
}
