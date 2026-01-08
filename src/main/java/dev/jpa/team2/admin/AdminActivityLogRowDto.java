package dev.jpa.team2.admin;

import java.time.LocalDateTime;

public record AdminActivityLogRowDto(
    Long logId,
    Long memberId,
    String loginId,
    String name,
    String actionType,
    String actionDetail,
    LocalDateTime actionAt,
    String actionIp
) {}
