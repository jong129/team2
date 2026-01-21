package dev.jpa.team2.checklist.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PreChecklistSummaryDto {

    private int totalCount;
    private int doneCount;
    private int requiredNotDone;
    private String level;
    private String message;

}
