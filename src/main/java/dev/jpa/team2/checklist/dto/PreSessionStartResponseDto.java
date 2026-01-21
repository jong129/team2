package dev.jpa.team2.checklist.dto;

import dev.jpa.team2.checklist.enums.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PreSessionStartResponseDto {

    private Long sessionId;
    private Long templateId;
    private SessionStatus status;
    private boolean isNew;

}
