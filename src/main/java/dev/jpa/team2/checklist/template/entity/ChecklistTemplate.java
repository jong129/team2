package dev.jpa.team2.checklist.template.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@Entity
@Table(
    name = "CHECKLIST_TEMPLATE",
    schema = "TEAM2",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UQ_TEMPLATE_NAME_VER",
            columnNames = {"TEMPLATE_NAME", "VERSION_NO"}
        )
    }
)
public class ChecklistTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "SEQ_CHECKLIST_TEMPLATE_ID")
    @SequenceGenerator(
            name = "SEQ_CHECKLIST_TEMPLATE_ID",
            sequenceName = "SEQ_CHECKLIST_TEMPLATE_ID",
            allocationSize = 1
    )
    @Column(name = "TEMPLATE_ID")
    private Long templateId;

    @Column(name = "TEMPLATE_TYPE", nullable = false, length = 10)
    private String templateType; // PRE / POST

    @Column(name = "TEMPLATE_NAME", nullable = false, length = 100)
    private String templateName;

    @Column(name = "VERSION_NO", nullable = false)
    private Integer versionNo = 1;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "IS_ACTIVE_YN", nullable = false, length = 1)
    private String isActiveYn = "Y";

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    // ======================
    // 관계
    // ======================
    @OneToMany(mappedBy = "template", fetch = FetchType.LAZY)
    private List<TemplateItem> templateItems;

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
