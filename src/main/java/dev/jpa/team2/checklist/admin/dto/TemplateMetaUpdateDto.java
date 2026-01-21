package dev.jpa.team2.checklist.admin.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 템플릿 메타 수정 DTO
 */
@Getter
@Setter
public class TemplateMetaUpdateDto {

    private String templateName;

    private String description;
}
