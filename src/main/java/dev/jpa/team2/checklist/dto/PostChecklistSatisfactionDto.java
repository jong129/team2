package dev.jpa.team2.checklist.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostChecklistSatisfactionDto {

    private Integer rating;      // 1~5
    private String commentText;  // optional
}
