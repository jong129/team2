package dev.jpa.team2.checklist.admin.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자 체크리스트 템플릿 목록 조회용 DTO
 * - 템플릿 목록 테이블의 한 행을 표현
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminChecklistTemplateRowDto {

    private Long templateId;        // 템플릿 ID
    private String phase;           // PRE / POST
    private String templateName;    // 템플릿명
    private Integer versionNo;      // 버전 번호
    private String status;          // DRAFT / ACTIVE / RETIRED

    private Integer itemCnt;        // 전체 항목 수
    private Integer activeItemCnt;  // 활성 항목 수

    private LocalDateTime updatedAt; // 수정일
}
