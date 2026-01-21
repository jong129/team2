package dev.jpa.team2.checklist.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PreChecklistSessionDto {

    private Long sessionId;
    private Long templateId;
    private List<PreChecklistSessionItemDto> items;
}
