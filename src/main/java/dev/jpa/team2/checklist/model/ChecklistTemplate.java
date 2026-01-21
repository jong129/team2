package dev.jpa.team2.checklist.model;

import java.util.Date;

import dev.jpa.team2.checklist.enums.ChecklistPhase;
import dev.jpa.team2.checklist.enums.TemplateStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "CHECKLIST_TEMPLATE")
@SequenceGenerator(
    name = "SEQ_CHECKLIST_TEMPLATE_GEN",
    sequenceName = "SEQ_CHECKLIST_TEMPLATE_ID",
    allocationSize = 1
)
public class ChecklistTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CHECKLIST_TEMPLATE_GEN")
    @Column(name = "TEMPLATE_ID")
    private Long templateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "PHASE", length = 10, nullable = false)
    private ChecklistPhase phase;

    @Column(name = "POST_GROUP_CODE", length = 30)
    private String postGroupCode;

    @Column(name = "TEMPLATE_NAME", length = 200, nullable = false)
    private String templateName;

    @Column(name = "VERSION_NO", nullable = false)
    private Integer versionNo = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 20, nullable = false)
    private TemplateStatus status = TemplateStatus.DRAFT;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATED_AT", nullable = false)
    private Date updatedAt;
    
    /* =========================
     * ✅ 생성 시점 자동 세팅
     * ========================= */
    @PrePersist
    protected void onCreate() {
        Date now = new Date();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /* =========================
     * ✅ 수정 시점 자동 갱신
     * ========================= */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }
    
    /* =========================
     * ✅ 템플릿 구성 항목 (추가)
     * ========================= */
    @OneToMany(
        mappedBy = "template",
        fetch = FetchType.LAZY
    )
    private java.util.List<ChecklistTemplateItem> templateItems
            = new java.util.ArrayList<>();
}
