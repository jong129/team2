package dev.jpa.team2.checklist.admin.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminAiImproveSaveRequestDto {

    /** 미리보기에서 확정된 항목 목록 */
    private List<PreviewItemDto> items;

    /** 관리자가 참고한 개선 요약 (선택) */
    private List<String> summary;

    @Getter
    @Setter
    public static class PreviewItemDto {
        private Integer itemOrder;
        private String title;
        private String description;
        private String requiredYn; // 기본 N
    }
}
