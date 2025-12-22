package dev.jpa.team2.checklist.template.dto;

import dev.jpa.team2.checklist.template.entity.ChecklistTemplate;

import java.util.List;

public class TemplateResponseDto {

    private Long templateId;
    private String templateType;
    private String templateName;
    private Integer versionNo;
    private String description;

    private List<TemplateItemDto> items;

    public static TemplateResponseDto from(ChecklistTemplate template,
                                           List<TemplateItemDto> items) {

        TemplateResponseDto dto = new TemplateResponseDto();
        dto.templateId = template.getTemplateId();
        dto.templateType = template.getTemplateType();
        dto.templateName = template.getTemplateName();
        dto.versionNo = template.getVersionNo();
        dto.description = template.getDescription();
        dto.items = items;
        return dto;
    }

    // getter
    public Long getTemplateId() { return templateId; }
    public String getTemplateType() { return templateType; }
    public String getTemplateName() { return templateName; }
    public Integer getVersionNo() { return versionNo; }
    public String getDescription() { return description; }
    public List<TemplateItemDto> getItems() { return items; }
}
