package com.generated.ldmurdergame.dto;

import java.time.LocalDateTime;

public record ConflictStage(Long id,
                            String scriptName,
                            String hostName,
                            LocalDateTime startAt,
                            LocalDateTime endAt,
                            LocalDateTime clearEndAt,
                            String status) {
}
