package com.generated.ldmurdergame.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record SessionDetailView(
    SessionView session,
    List<RoleView> roles,
    List<RosterPlayerView> roster,
    List<RosterPlayerView> unseatedRoster,
    VersionView currentVersion,
    List<VersionView> history,
    List<SwapRequestView> swapRequests) {
}
