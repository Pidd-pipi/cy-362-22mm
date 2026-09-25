package com.generated.ldmurdergame.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 开演后的换角申请。
 * SWAP：两位在场玩家互换角色（toPlayerId 为另一位玩家）；
 * REPLACE：指定角色由候补玩家顶替（waitingPlayerId 为候补玩家）。
 */
public record CreateChangeRequest(
    @NotNull(message = "请选择调整类型")
    @Pattern(regexp = "SWAP|REPLACE", message = "调整类型只能是 SWAP 或 REPLACE") String changeType,
    @NotNull(message = "请选择需要调整的角色") Long roleId,
    Long toPlayerId,
    Long waitingPlayerId,
    String reason) {
}
