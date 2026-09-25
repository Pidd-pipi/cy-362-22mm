package com.generated.ldmurdergame.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddPlayerRequest(
    @NotBlank(message = "请填写玩家姓名") String playerName,
    @NotBlank(message = "请选择玩家性别")
    @Pattern(regexp = "MALE|FEMALE", message = "玩家性别只能是 MALE 或 FEMALE") String gender) {
}
