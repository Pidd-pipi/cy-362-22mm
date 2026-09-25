package com.generated.ldmurdergame.dto;

import jakarta.validation.constraints.NotNull;

public record SwapRequest(
    @NotNull Long roleId,
    @NotNull Long toPlayerId,
    String reason,
    String operator) {
}
