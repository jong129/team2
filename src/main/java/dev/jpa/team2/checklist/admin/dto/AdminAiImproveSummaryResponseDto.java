package dev.jpa.team2.checklist.admin.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * ==========================================
 * AI 개선 요약 생성 응답 DTO
 *
 * - 항목별 "개선 사유" 설명
 * ==========================================
 */
@Getter
@Setter
public class AdminAiImproveSummaryResponseDto {

    /** 항목별 개선 요약 */
    private List<ItemSummaryDto> summaries;

    @Getter
    @Setter
    public static class ItemSummaryDto {

        /** 체크리스트 항목 제목 */
        private String title;

        /** 왜 해당 항목이 추가/변경되었는지 설명 */
        private String reason;
    }
}
