package dev.jpa.team2.admin;

import java.time.LocalDateTime;

public record AdminPasswordChangeHistoryRowDto(
    Long changeId,

    Long memberId,
    String loginId,
    String name,

    Long changedById,
    String changedByLoginId,
    String changedByName,

    String changeType,
    LocalDateTime changedAt
) {}
