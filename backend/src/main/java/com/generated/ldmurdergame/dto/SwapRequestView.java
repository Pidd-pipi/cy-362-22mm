package com.generated.ldmurdergame.dto;

import java.time.LocalDateTime;

public record SwapRequestView(
    Long id,
    Long sessionId,
    Long roleId,
    String roleName,
    Long fromPlayerId,
    String fromPlayerName,
    Long toPlayerId,
    String toPlayerName,
    String reason,
    String status,
    String reviewNote,
    String createdBy,
    String reviewedBy,
    LocalDateTime createdAt,
    LocalDateTime reviewedAt) {
}
