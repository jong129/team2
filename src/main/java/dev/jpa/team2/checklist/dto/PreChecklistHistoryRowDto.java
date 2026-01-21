package dev.jpa.team2.checklist.dto;

import java.util.Date;

import dev.jpa.team2.checklist.enums.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PreChecklistHistoryRowDto {

    private Long sessionId;
    private SessionStatus status;
    private Date startedAt;
    private Date completedAt;

}
