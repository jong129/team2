package dev.jpa.team2.checklist.dto;

public record PostStartResponse(
    Long sessionId,
    String postGroupCode,
    Long templateId
) {}
