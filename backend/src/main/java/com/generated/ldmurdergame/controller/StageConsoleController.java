package com.generated.ldmurdergame.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import com.generated.ldmurdergame.dto.AddPlayerRequest;
import com.generated.ldmurdergame.dto.ChangeView;
import com.generated.ldmurdergame.dto.ConflictStage;
import com.generated.ldmurdergame.dto.CreateChangeRequest;
import com.generated.ldmurdergame.dto.CreateStageRequest;
import com.generated.ldmurdergame.dto.DecideChangeRequest;
import com.generated.ldmurdergame.dto.PlayerView;
import com.generated.ldmurdergame.dto.RedrawRequest;
import com.generated.ldmurdergame.dto.StageSummary;
import com.generated.ldmurdergame.dto.StageView;
import com.generated.ldmurdergame.service.StageConsoleService;

@RestController
public class StageConsoleController {
  private final StageConsoleService service;

  public StageConsoleController(StageConsoleService service) {
    this.service = service;
  }

  @GetMapping({"/stages", "/api/stages"})
  public List<StageSummary> stages() {
    return service.listStages();
  }

  @GetMapping({"/stages/conflicts", "/api/stages/conflicts"})
  public List<ConflictStage> checkConflicts(@RequestParam Long scriptId,
                                            @RequestParam Long hostId,
                                            @RequestParam String startAt) {
    return service.checkConflicts(scriptId, hostId, java.time.LocalDateTime.parse(startAt));
  }

  @PostMapping({"/stages", "/api/stages"})
  @ResponseStatus(HttpStatus.CREATED)
  public StageSummary create(@Valid @RequestBody CreateStageRequest request) {
    return service.createStage(request);
  }

  @GetMapping({"/stages/{id}", "/api/stages/{id}"})
  public StageView detail(@PathVariable Long id) {
    return service.getStage(id);
  }

  @PostMapping({"/stages/{id}/players", "/api/stages/{id}/players"})
  @ResponseStatus(HttpStatus.CREATED)
  public PlayerView addPlayer(@PathVariable Long id, @Valid @RequestBody AddPlayerRequest request) {
    return service.addPlayer(id, request);
  }

  @DeleteMapping({"/stages/{id}/players/{playerId}", "/api/stages/{id}/players/{playerId}"})
  public void removePlayer(@PathVariable Long id, @PathVariable Long playerId) {
    service.removePlayer(id, playerId);
  }

  @PostMapping({"/stages/{id}/draw", "/api/stages/{id}/draw"})
  public StageView draw(@PathVariable Long id, @RequestBody(required = false) RedrawRequest body) {
    String note = body == null ? null : body.note();
    return service.redraw(id, note, true);
  }

  @PostMapping({"/stages/{id}/redraw", "/api/stages/{id}/redraw"})
  public StageView redraw(@PathVariable Long id, @RequestBody(required = false) RedrawRequest body) {
    String note = body == null ? null : body.note();
    return service.redraw(id, note, false);
  }

  @PostMapping({"/stages/{id}/confirm", "/api/stages/{id}/confirm"})
  public StageView confirm(@PathVariable Long id) {
    return service.confirm(id);
  }

  @PostMapping({"/stages/{id}/changes", "/api/stages/{id}/changes"})
  @ResponseStatus(HttpStatus.CREATED)
  public ChangeView createChange(@PathVariable Long id, @Valid @RequestBody CreateChangeRequest request) {
    return service.createChange(id, request);
  }

  @PostMapping({"/stages/{id}/changes/{changeId}/decision", "/api/stages/{id}/changes/{changeId}/decision"})
  public StageView decideChange(@PathVariable Long id,
                                @PathVariable Long changeId,
                                @Valid @RequestBody DecideChangeRequest request) {
    return service.decideChange(id, changeId, request);
  }
}
