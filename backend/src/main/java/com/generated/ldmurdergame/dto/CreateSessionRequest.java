package com.generated.ldmurdergame.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.validation.constraints.NotNull;

public record CreateSessionRequest(
    @NotNull Long scriptId,
    @NotNull Long hostId,
    @NotNull LocalDate date,
    @NotNull LocalTime startTime,
    /** 清场期（分钟），默认 20。 */
    Integer cleanupMinutes,
    String note) {
}
