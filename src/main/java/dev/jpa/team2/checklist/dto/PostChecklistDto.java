package dev.jpa.team2.checklist.dto;

import java.util.List;

import dev.jpa.team2.checklist.enums.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostChecklistDto {

    private Long sessionId;
    private SessionStatus status;
    private Long templateId;
    private String templateName;
    private List<PostChecklistItemDto> items;

}
