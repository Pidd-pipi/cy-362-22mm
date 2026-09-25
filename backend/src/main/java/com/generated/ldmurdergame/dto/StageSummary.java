package com.generated.ldmurdergame.dto;

import java.time.LocalDateTime;

public record StageSummary(Long id,
                           String scriptName,
                           String genre,
                           String hostName,
                           LocalDateTime startAt,
                           LocalDateTime endAt,
                           LocalDateTime clearEndAt,
                           String status,
                           int requiredCount,
                           int activeCount,
                           boolean rosterReady,
                           Integer currentVersionNo) {
}
