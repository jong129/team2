package dev.jpa.team2.checklist.admin.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminPreSessionRowDto {

    private Long sessionId;
    private Long memberId;
    private Date completedAt;
}
