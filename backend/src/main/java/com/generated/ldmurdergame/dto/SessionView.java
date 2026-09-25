package com.generated.ldmurdergame.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record SessionView(
    Long id,
    Long scriptId,
    String scriptName,
    Long hostId,
    String hostName,
    LocalDate sessionDate,
    LocalTime startTime,
    LocalTime endTime,
    LocalDateTime startAt,
    LocalDateTime endAt,
    int cleanupMinutes,
    int requiredPlayers,
    int rosterCount,
    int seatedCount,
    int roleCount,
    boolean full,
    String status,
    LocalDateTime confirmedAt,
    String note,
    LocalDateTime createdAt,
    List<SeatView> seats) {
}
