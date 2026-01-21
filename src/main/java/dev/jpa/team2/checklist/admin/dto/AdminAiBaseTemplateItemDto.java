package dev.jpa.team2.checklist.admin.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * ==========================================
 * AI 개선 기준 템플릿 - 아이템 DTO
 *
 * - 기존 템플릿의 개별 항목
 * - 읽기 전용 (비교용)
 * ==========================================
 */
@Getter
@Builder
public class AdminAiBaseTemplateItemDto {

    /**
     * 템플릿 아이템 ID
     */
    private Long itemId;

    /**
     * 아이템 순서
     */
    private Integer itemOrder;

    /**
     * 항목 제목
     */
    private String title;

    /**
     * 항목 설명
     */
    private String description;

    /**
     * 필수 여부 (Y/N)
     */
    private String requiredYn;
}
