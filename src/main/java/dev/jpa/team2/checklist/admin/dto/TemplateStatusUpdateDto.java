package dev.jpa.team2.checklist.admin.dto;

import dev.jpa.team2.checklist.enums.TemplateStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * 템플릿 상태 변경 DTO
 */
@Getter
@Setter
public class TemplateStatusUpdateDto {

    private TemplateStatus status;
}
