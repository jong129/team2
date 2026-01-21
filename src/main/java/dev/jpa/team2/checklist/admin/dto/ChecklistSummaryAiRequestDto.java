package dev.jpa.team2.checklist.admin.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * FastAPI 체크리스트 요약 요청 DTO
 */
@Getter
@Setter
public class ChecklistSummaryAiRequestDto {

    private Long templateId;
    private List<String> comments;
}
