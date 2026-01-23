package dev.jpa.team2.checklist.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostChecklistSummaryDto {

    private String level;     // "안전" | "주의" | "위험" 등
    private String message;   // 프론트 표시용 메시지

}
