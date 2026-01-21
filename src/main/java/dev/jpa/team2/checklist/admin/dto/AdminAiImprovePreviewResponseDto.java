package dev.jpa.team2.checklist.admin.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * ==========================================
 * AI 개선 템플릿 미리보기 응답 DTO
 * - DB에 저장되지 않는 "초안"
 * ==========================================
 */
@Getter
@Builder
public class AdminAiImprovePreviewResponseDto {

    private Long baseTemplateId;

    private String previewTemplateName;

    private Integer previewVersionNo;

    /**
     * AI가 제안한 항목 목록
     */
    private List<AdminAiBaseTemplateItemDto> items;

}
