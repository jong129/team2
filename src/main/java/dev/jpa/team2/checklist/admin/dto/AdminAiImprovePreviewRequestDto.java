package dev.jpa.team2.checklist.admin.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * ==========================================
 * AI 개선 템플릿 미리보기 요청 DTO
 *
 * - 기준 템플릿 ID는 PathVariable
 * - 필요 시 관리자 옵션만 전달
 * ==========================================
 */
@Getter
@Setter
public class AdminAiImprovePreviewRequestDto {

    /**
     * 개선 전략 (예: BASIC, STRICT, SAFE 등)
     * - 초기 버전에서는 optional
     */
    private String strategy;

    /**
     * 제외할 아이템 ID 목록 (관리자 수동 제외 시)
     */
    private List<Long> excludeItemIds;
}
