package dev.jpa.team2.checklist.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * POST 체크리스트 행동 가이드 DTO (LLM)
 */
@Getter
@Setter
public class PostActionGuideDto {

    /** 한 줄 요약 */
    private String summary;

    /** 지금 당장 해야 할 행동 목록 */
    private List<ActionItem> actions;

    @Getter
    @Setter
    public static class ActionItem {
        private String title;        // 행동 제목
        private String description;  // 왜 필요한지
        private String timing;       // 언제 해야 하는지
        private String priority;     // HIGH / MEDIUM / LOW
    }
}
