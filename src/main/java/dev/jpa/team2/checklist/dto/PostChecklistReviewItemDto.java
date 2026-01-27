package dev.jpa.team2.checklist.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostChecklistReviewItemDto {

    private Long itemId;
    private String title;
    private Double importanceScore;
    private String reason;     // 왜 확인이 필요한지
    private String action;     // 권장 후속조치
}
