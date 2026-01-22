package dev.jpa.team2.checklist.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminPostDecisionScoreRowDto {

    private Long itemId;
    private String title;
    private Double importanceScore;
}
