package com.generated.ldmurdergame.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;

public record CreateStageRequest(
    @NotNull(message = "请选择剧本") Long scriptId,
    @NotNull(message = "请选择主持人") Long hostId,
    @NotNull(message = "请选择日期和开始时间") LocalDateTime startAt) {
}
