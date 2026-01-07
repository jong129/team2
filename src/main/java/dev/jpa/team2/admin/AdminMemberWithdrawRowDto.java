package dev.jpa.team2.admin;

import java.time.LocalDateTime;

public record AdminMemberWithdrawRowDto(
    Long withdrawId,
    Long memberId,
    String loginId,
    String name,
    String reasonCode,
    String reasonText,
    LocalDateTime createdAt
) {}
