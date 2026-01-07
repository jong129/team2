package dev.jpa.team2.admin;

import java.time.LocalDateTime;

public record AdminMemberUpdateHistoryRowDto(
    Long historyId,

    Long memberId,
    String loginId,
    String name,

    Long changedById,
    String changedByLoginId,
    String changedByName,

    String fieldName,
    String oldValue,
    String newValue,
    String changeType,
    LocalDateTime changedAt
) {}
