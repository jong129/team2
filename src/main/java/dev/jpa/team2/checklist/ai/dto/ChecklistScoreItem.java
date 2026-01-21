package dev.jpa.team2.checklist.ai.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * AI 중요도 스코어링 요청용 항목 DTO
 */
@Getter
@Setter
public class ChecklistScoreItem {

    private Long itemId;
    private String title;
    private String description;
}
