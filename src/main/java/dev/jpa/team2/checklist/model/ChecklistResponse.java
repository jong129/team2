package dev.jpa.team2.checklist.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

/**
 * CHECKLIST_RESPONSE
 * - 한 세션(session)에서 한 항목(item)에 대한 체크 상태 저장
 * - (SESSION_ID, ITEM_ID) 유니크
 */
@Entity
@Table(
        name = "CHECKLIST_RESPONSE",
        uniqueConstraints = {
                @UniqueConstraint(name = "UQ_CHECKLIST_RESPONSE_SESSION_ITEM", columnNames = {"SESSION_ID", "ITEM_ID"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CHECKLIST_RESPONSE_ID")
    @SequenceGenerator(
            name = "SEQ_CHECKLIST_RESPONSE_ID",
            sequenceName = "SEQ_CHECKLIST_RESPONSE_ID",
            allocationSize = 1
    )
    @Column(name = "RESPONSE_ID")
    private Long responseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SESSION_ID", nullable = false)
    private ChecklistSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ITEM_ID", nullable = false)
    private ChecklistItem item;

    /**
     * DONE / NOT_DONE / NOT_REQUIRED
     * (일단 String으로 두고, 나중에 enum으로 바꿔도 됨)
     */
    @Column(name = "CHECK_STATUS", nullable = false, length = 20)
    private String checkStatus;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
