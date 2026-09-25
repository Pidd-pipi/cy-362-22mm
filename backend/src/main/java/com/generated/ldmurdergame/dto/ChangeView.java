package com.generated.ldmurdergame.dto;

import java.time.LocalDateTime;

public record ChangeView(Long id,
                         String changeType,
                         String status,
                         Long roleId,
                         String roleName,
                         Long fromPlayerId,
                         String fromPlayerName,
                         Long toPlayerId,
                         String toPlayerName,
                         Long waitingPlayerId,
                         String waitingPlayerName,
                         Integer versionNo,
                         String reason,
                         String managerNote,
                         LocalDateTime createdAt,
                         LocalDateTime decidedAt) {
}
