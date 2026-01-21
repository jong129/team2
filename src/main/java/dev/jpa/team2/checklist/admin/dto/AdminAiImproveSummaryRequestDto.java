package dev.jpa.team2.checklist.admin.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * ==========================================
 * AI 개선 요약 생성 요청 DTO
 *
 * - 미리보기 결과를 기반으로
 * - 왜 이렇게 개선했는지 설명 요청
 * ==========================================
 */
@Getter
@Setter
public class AdminAiImproveSummaryRequestDto {

    /** 기준 템플릿 ID */
    private Long templateId;

    /** 미리보기에서 생성된 항목 목록 */
    private List<PreviewItemDto> previewItems;

    /** 항목별 사용자 수행 통계 */
    private List<UserItemStatDto> userStats;

    /** 만족도 요약 정보 */
    private SatisfactionDto satisfaction;

    /* ==========================
     * 내부 DTO
     * ========================== */

    @Getter
    @Setter
    public static class PreviewItemDto {
        private Integer itemOrder;
        private String title;
        private String description;
    }

    @Getter
    @Setter
    public static class UserItemStatDto {
        private String itemTitle;
        private Double doneRate;
        private Double notDoneRate;
        private Double notRequiredRate;
    }

    @Getter
    @Setter
    public static class SatisfactionDto {
        private Double avgScore;
        private List<String> negativeKeywords;
    }
}
