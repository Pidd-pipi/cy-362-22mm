package com.generated.ldmurdergame.dto;

import java.util.List;

public record ScriptView(
    Long id,
    String name,
    String genre,
    String difficulty,
    int durationMinutes,
    int playerCount,
    String summary,
    List<RoleView> roles) {
}
