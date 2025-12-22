package dev.jpa.team2.checklist.template.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "TEMPLATE_ITEM", schema = "TEAM2",
       uniqueConstraints = {
           @UniqueConstraint(
               name = "UQ_TEMPLATE_ITEM",
               columnNames = {"TEMPLATE_ID", "ITEM_ID"}
           )
       }
)
public class TemplateItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "SEQ_TEMPLATE_ITEM_ID")
    @SequenceGenerator(
            name = "SEQ_TEMPLATE_ITEM_ID",
            sequenceName = "SEQ_TEMPLATE_ITEM_ID",
            allocationSize = 1
    )
    @Column(name = "TEMPLATE_ITEM_ID")
    private Long templateItemId;

    // ======================
    // 관계
    // ======================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEMPLATE_ID", nullable = false)
    private ChecklistTemplate template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ITEM_ID", nullable = false)
    private ChecklistItem item;

    // ======================
    // 속성
    // ======================
    @Column(name = "ITEM_ORDER", nullable = false)
    private Integer itemOrder;

    @Column(name = "REQUIRED_YN", nullable = false, length = 1)
    private String requiredYn = "N";

    @Column(name = "RISK_LEVEL_OVERRIDE", length = 10)
    private String riskLevelOverride;

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
