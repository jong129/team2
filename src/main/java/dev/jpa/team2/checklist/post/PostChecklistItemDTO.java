package dev.jpa.team2.checklist.post;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostChecklistItemDTO {
    private Long itemId;
    private Integer itemOrder;
    private String checkArea;
    private String title;
    private String description;
    private String requiredYn;
}
