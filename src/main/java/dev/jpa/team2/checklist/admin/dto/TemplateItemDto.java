package dev.jpa.team2.checklist.admin.dto;

import dev.jpa.team2.checklist.enums.ChecklistPhase;
import dev.jpa.team2.checklist.enums.Yn;
import lombok.Getter;
import lombok.Setter;

/**
 * 템플릿 구성 항목 DTO
 */
@Getter
@Setter
public class TemplateItemDto {

    private Long itemMasterId;

    private Integer itemOrder;

    private Yn requiredYn;

    private Yn activeYn;

    private String phase;

    private String postGroupCode;

    private String title;

    private String description;
}
