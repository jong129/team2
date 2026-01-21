package dev.jpa.team2.checklist.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostChecklistDto {

    private Long sessionId;
    private Long templateId;
    private String templateName;
    private List<PostChecklistItemDto> items;

}
