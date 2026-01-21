package dev.jpa.team2.checklist.admin.dto;

import dev.jpa.team2.checklist.enums.ChecklistPhase;
import dev.jpa.team2.checklist.enums.TemplateStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * 템플릿 단건 메타 DTO
 */
@Getter
@Setter
public class TemplateMetaDto {

    private Long templateId;

    private String templateName;

    private String description;

    private ChecklistPhase phase;

    private Integer versionNo;

    private TemplateStatus status;

    private String postGroupCode; // POST 템플릿일 경우
}
