package dev.jpa.team2.checklist.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * POST 체크리스트 분기 사유 응답 DTO
 */
@Getter
@Setter
public class PostDecisionResponse {

    /** POST_A / POST_B */
    private String postGroupCode;

    /** 누적 위험 점수 */
    private Double riskScoreSum;

    /** 고위험 항목 ID 목록 */
    private List<String> highRiskItemIds;

    /** 사용자 표시용 메시지 */
    private String message;
}
