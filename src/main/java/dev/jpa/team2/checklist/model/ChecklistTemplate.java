package dev.jpa.team2.checklist.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * CHECKLIST_TEMPLATE
 *
 * ✔ 사전/사후 체크리스트 템플릿 메타 정보
 * ✔ 버전 관리 대상
 * ✔ PRE / POST 분기 + POST_GROUP_CODE 제약 존재
 */
@Entity
@Table(name = "CHECKLIST_TEMPLATE")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChecklistTemplate {

    /**
     * TEMPLATE_ID (PK)
     * SEQ_CHECKLIST_TEMPLATE_ID 사용
     */
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "SEQ_CHECKLIST_TEMPLATE_ID"
    )
    @SequenceGenerator(
            name = "SEQ_CHECKLIST_TEMPLATE_ID",
            sequenceName = "SEQ_CHECKLIST_TEMPLATE_ID",
            allocationSize = 1
    )
    @Column(name = "TEMPLATE_ID")
    private Long templateId;

    /**
     * 단계 (PRE / POST)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "PHASE", nullable = false, length = 10)
    private Phase phase;

    /**
     * 사후 체크리스트 그룹 코드
     *  - PRE  : NULL
     *  - POST : 필수 (POST_A / POST_B / POST_C ...)
     */
    @Column(name = "POST_GROUP_CODE", length = 30)
    private String postGroupCode;

    /**
     * 템플릿 이름 (화면 표시용)
     */
    @Column(name = "TEMPLATE_NAME", nullable = false, length = 200)
    private String templateName;

    /**
     * 버전 번호
     */
    @Column(name = "VERSION_NO", nullable = false)
    private Integer versionNo;

    /**
     * 템플릿 상태 (DRAFT / ACTIVE / RETIRED)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private TemplateStatus status;

    /**
     * 템플릿 설명
     */
    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    /**
     * 생성일
     */
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 수정일
     */
    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;
    
    public void changeStatus(TemplateStatus status) {
      this.status = status;
      this.updatedAt = java.time.LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
      if (this.createdAt == null) this.createdAt = LocalDateTime.now();
      if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
      this.updatedAt = LocalDateTime.now();
    }

    
}
