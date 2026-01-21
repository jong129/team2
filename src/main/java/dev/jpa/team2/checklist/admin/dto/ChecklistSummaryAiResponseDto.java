package dev.jpa.team2.checklist.admin.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * FastAPI 체크리스트 요약 응답 DTO
 */
@Getter
@Setter
public class ChecklistSummaryAiResponseDto {

    private List<String> positive;
    private List<String> negative;
    private List<String> suggestions;
}
