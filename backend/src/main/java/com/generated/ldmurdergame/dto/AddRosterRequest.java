package com.generated.ldmurdergame.dto;

import jakarta.validation.constraints.NotNull;

public record AddRosterRequest(
    @NotNull Long playerId,
    String operator) {
}
