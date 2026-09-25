package com.generated.ldmurdergame.dto;

import java.time.LocalDateTime;
import java.util.List;

public record VersionView(
    Long id,
    int versionNo,
    String kind,
    String changeNote,
    String createdBy,
    LocalDateTime createdAt,
    List<SeatView> seats) {
}
