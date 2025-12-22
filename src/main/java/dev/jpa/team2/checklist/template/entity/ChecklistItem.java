package dev.jpa.team2.checklist.template.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "CHECKLIST_ITEM", schema = "TEAM2")
public class ChecklistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "SEQ_CHECKLIST_ITEM_ID")
    @SequenceGenerator(
            name = "SEQ_CHECKLIST_ITEM_ID",
            sequenceName = "SEQ_CHECKLIST_ITEM_ID",
            allocationSize = 1
    )
    @Column(name = "ITEM_ID")
    private Long itemId;

    @Column(name = "ITEM_TITLE", nullable = false, length = 200)
    private String itemTitle;

    @Column(name = "ITEM_DESCRIPTION", length = 1000)
    private String itemDescription;

    @Column(name = "DEFAULT_REQUIRED_YN", nullable = false, length = 1)
    private String defaultRequiredYn = "N";

    @Column(name = "DEFAULT_RISK_LEVEL", nullable = false, length = 10)
    private String defaultRiskLevel = "LOW";

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // getter / setter
}
