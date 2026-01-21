package dev.jpa.team2.checklist.admin.dto;

import java.util.List;

import dev.jpa.team2.checklist.enums.ChecklistPhase;
import lombok.Builder;
import lombok.Getter;

/**
 * ==========================================
 * AI 개선 기준 템플릿 DTO
 *
 * - AI 기반 사후 체크리스트 개선 화면
 * - 좌측 "기존 템플릿" 영역 데이터
 * ==========================================
 */
@Getter
@Builder
public class AdminAiBaseTemplateDto {

    /**
     * 템플릿 ID
     */
    private Long templateId;

    /**
     * 템플릿 이름
     */
    private String templateName;

    /**
     * 템플릿 버전
     */
    private Integer versionNo;

    /**
     * 체크리스트 단계 (PRE / POST)
     */
    private ChecklistPhase phase;

    /**
     * POST 그룹 코드 (POST_A / POST_B 등)
     */
    private String postGroupCode;

    /**
     * 템플릿에 포함된 아이템 목록
     */
    private List<AdminAiBaseTemplateItemDto> items;
}
