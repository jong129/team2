package dev.jpa.team2.admin;

import java.time.LocalDateTime;

public record AdminLoginHistoryRowDto(
    Long historyId,
    Long memberId,
    String loginId,
    String name,
    LocalDateTime loginAt,
    String loginIp,
    String successYn
) {}
