package dev.jpa.team2.checklist.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * PRE 체크리스트 위험 설명 DTO
 * - LLM 또는 기본 로직에서 생성
 */
@Getter
@Setter
public class PreRiskExplanationDto {

    private String summary;          // 한 줄 요약
    private List<String> reasons;    // 위험 원인 설명
    private List<String> actions;    // 권장 조치 (완곡)
}
