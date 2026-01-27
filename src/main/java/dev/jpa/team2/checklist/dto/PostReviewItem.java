package dev.jpa.team2.checklist.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostReviewItem {

    private Long itemId;
    private String title;
    private String reason;      // 왜 중요한지
    private String actionGuide; // PDF/체크리스트 기반 후속조치
}
