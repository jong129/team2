package dev.jpa.team2.checklist.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostChecklistReviewResponse {

    private int totalCount;
    private int doneCount;
    private int notDoneCount;
    private String summary;

    private List<PostChecklistReviewItemDto> items;
}
