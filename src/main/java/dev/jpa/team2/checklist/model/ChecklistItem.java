package dev.jpa.team2.checklist.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * CHECKLIST_ITEM
 *
 * ✔ 체크리스트 템플릿에 속한 개별 항목
 * ✔ 출력 순서 + 필수 여부 + 활성 여부 관리
 */
@Entity
@Table(name = "CHECKLIST_ITEM")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChecklistItem {

    /**
     * ITEM_ID (PK)
     * SEQ_CHECKLIST_ITEM_ID 사용
     */
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "SEQ_CHECKLIST_ITEM_ID"
    )
    @SequenceGenerator(
            name = "SEQ_CHECKLIST_ITEM_ID",
            sequenceName = "SEQ_CHECKLIST_ITEM_ID",
            allocationSize = 1
    )
    @Column(name = "ITEM_ID")
    private Long itemId;

    /**
     * 소속 템플릿
     * (CHECKLIST_TEMPLATE.TEMPLATE_ID FK)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEMPLATE_ID", nullable = false)
    private ChecklistTemplate template;

    /**
     * 출력 순서
     */
    @Column(name = "ITEM_ORDER", nullable = false)
    private Integer itemOrder;

    /*
     * 확인 영역 
     */
    @Column(name = "CHECK_AREA", length = 50)
    private String checkArea;

    
    /**
     * 항목 제목
     */
    @Column(name = "TITLE", nullable = false, length = 300)
    private String title;

    /**
     * 항목 설명 / 권고 문구
     */
    @Column(name = "DESCRIPTION", length = 2000)
    private String description;

    /**
     * 필수 여부 (Y / N)
     */
    @Column(name = "REQUIRED_YN", nullable = false, length = 1)
    private String requiredYn;

    /**
     * 활성 여부 (Y / N)
     */
    @Column(name = "ACTIVE_YN", nullable = false, length = 1)
    private String activeYn;

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
}
