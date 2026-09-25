package com.generated.ldmurdergame.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** 与主持人档期冲突的场次。 */
public record ConflictSessionView(
    Long sessionId,
    String scriptName,
    String hostName,
    LocalDate sessionDate,
    LocalTime startTime,
    LocalTime endTime,
    String status,
    LocalDateTime startAt,
    LocalDateTime endAt) {
}
