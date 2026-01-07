package dev.jpa.team2.checklist.post;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostChecklistResponseDTO {

    private Long sessionId;
    private Long templateId;
    private String templateName;

    private List<PostChecklistItemDTO> items;
}
