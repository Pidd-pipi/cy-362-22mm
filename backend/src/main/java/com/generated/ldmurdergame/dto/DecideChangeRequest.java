package com.generated.ldmurdergame.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record DecideChangeRequest(
    @NotNull(message = "请选择批准或驳回")
    @Pattern(regexp = "APPROVED|REJECTED", message = "审批结果只能是 APPROVED 或 REJECTED") String decision,
    String managerNote) {
}
