package com.generated.ldmurdergame.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色开演台详情：场次档期、角色册、到场/候补名单、各版抽签座次、换角申请与历次调整。
 */
public record StageView(Long id,
                        Long scriptId,
                        String scriptName,
                        String genre,
                        String difficulty,
                        Integer durationMinutes,
                        Integer requiredCount,
                        Long hostId,
                        String hostName,
                        String hostTitle,
                        LocalDateTime startAt,
                        LocalDateTime endAt,
                        LocalDateTime clearEndAt,
                        String status,
                        LocalDateTime confirmedAt,
                        List<RoleView> roles,
                        List<PlayerView> players,
                        List<VersionView> versions,
                        List<ChangeView> changes) {
}
