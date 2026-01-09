package dev.jpa.team2.checklist.post;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PostChecklistStatusDTO {
    private Long itemId;
    private String checkStatus; // DONE / NOT_DONE / NOT_REQUIRED
}
