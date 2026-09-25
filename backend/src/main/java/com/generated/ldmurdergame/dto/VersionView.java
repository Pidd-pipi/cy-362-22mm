package com.generated.ldmurdergame.dto;

import java.time.LocalDateTime;
import java.util.List;

public record VersionView(Integer versionNo,
                          String drawTrigger,
                          String note,
                          LocalDateTime createdAt,
                          List<SeatView> seats) {
}
