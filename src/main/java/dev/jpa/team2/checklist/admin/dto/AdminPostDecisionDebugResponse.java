package dev.jpa.team2.checklist.admin.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminPostDecisionDebugResponse {

    private String postGroupCode;
    private Double riskScoreSum;
    private List<AdminPostDecisionScoreRowDto> scores;
}
