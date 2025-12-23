package dev.jpa.team2.checklist.template;

import java.util.List;

public class TemplateDTO {

    private Long templateId;
    private String templateType;
    private String templateName;
    private Integer versionNo;
    private String description;

    private List<TemplateItemDTO> items;

    public static TemplateDTO from(Template template,
                                           List<TemplateItemDTO> items) {

        TemplateDTO dto = new TemplateDTO();
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
    public List<TemplateItemDTO> getItems() { return items; }
}
