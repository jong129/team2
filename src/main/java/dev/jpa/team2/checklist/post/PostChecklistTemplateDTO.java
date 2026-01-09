package dev.jpa.team2.checklist.post;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostChecklistTemplateDTO {
    private Long templateId;
    private String postGroupCode;
    private String templateName;
    private Integer versionNo;
    private List<PostChecklistItemDTO> items;
}
