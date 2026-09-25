package com.generated.ldmurdergame.controller;

import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import com.generated.ldmurdergame.dto.AddRosterRequest;
import com.generated.ldmurdergame.dto.ConfirmRequest;
import com.generated.ldmurdergame.dto.CreateSessionRequest;
import com.generated.ldmurdergame.dto.DrawRequest;
import com.generated.ldmurdergame.dto.HostView;
import com.generated.ldmurdergame.dto.PlayerView;
import com.generated.ldmurdergame.dto.ReviewSwapRequest;
import com.generated.ldmurdergame.dto.ScriptView;
import com.generated.ldmurdergame.dto.SessionDetailView;
import com.generated.ldmurdergame.dto.SessionView;
import com.generated.ldmurdergame.dto.SwapRequest;
import com.generated.ldmurdergame.service.StageQueryService;
import com.generated.ldmurdergame.service.StageService;

/** 角色开演台：排期指派、名单、抽签入座、确认定版、换角确认。 */
@RestController
@RequestMapping({"/api/stage", "/stage"})
public class StageController {
  private final StageService stageService;
  private final StageQueryService queryService;

  public StageController(StageService stageService, StageQueryService queryService) {
    this.stageService = stageService;
    this.queryService = queryService;
  }

  // 基础数据
  @GetMapping("/scripts")
  public List<ScriptView> scripts() {
    return queryService.listScripts();
  }

  @GetMapping("/hosts")
  public List<HostView> hosts() {
    return queryService.listHosts();
  }

  @GetMapping("/players")
  public List<PlayerView> players() {
    return queryService.listPlayers();
  }

  // 开演场次
  @GetMapping("/sessions")
  public List<SessionView> sessions() {
    return queryService.listSessions();
  }

  @GetMapping("/sessions/{id}")
  public SessionDetailView detail(@PathVariable Long id) {
    return queryService.getDetail(id);
  }

  @PostMapping("/sessions")
  public SessionDetailView create(@Valid @RequestBody CreateSessionRequest request) {
    SessionView created = stageService.createSession(request);
    return queryService.getDetail(created.id());
  }

  // 名单
  @PostMapping("/sessions/{id}/roster")
  public SessionDetailView addRoster(@PathVariable Long id,
      @Valid @RequestBody AddRosterRequest request) {
    return stageService.addToRoster(id, request.playerId(), request.operator());
  }

  @DeleteMapping("/sessions/{id}/roster/{playerId}")
  public SessionDetailView removeRoster(@PathVariable Long id, @PathVariable Long playerId) {
    return stageService.removeFromRoster(id, playerId);
  }

  // 抽签入座（重抽时 reDraw=true，系统会保留上一版）
  @PostMapping("/sessions/{id}/draw")
  public SessionDetailView draw(@PathVariable Long id, @RequestBody(required = false) DrawRequest body) {
    boolean reDraw = body != null && Boolean.TRUE.equals(body.reDraw());
    String operator = body == null ? null : body.operator();
    return stageService.draw(id, reDraw, operator);
  }

  // 确认开演，角色关系定版
  @PostMapping("/sessions/{id}/confirm")
  public SessionDetailView confirm(@PathVariable Long id,
      @RequestBody(required = false) ConfirmRequest body) {
    return stageService.confirm(id, body == null ? null : body.operator());
  }

  // 确认后的换角：申请 -> 店长确认/驳回
  @PostMapping("/sessions/{id}/swaps")
  public SessionDetailView requestSwap(@PathVariable Long id,
      @Valid @RequestBody SwapRequest request) {
    return stageService.requestSwap(id, request.roleId(), request.toPlayerId(),
        request.reason(), request.operator());
  }

  @PostMapping("/swaps/{requestId}/approve")
  public SessionDetailView approve(@PathVariable Long requestId,
      @RequestBody(required = false) ReviewSwapRequest body) {
    return stageService.approveSwap(requestId, body == null ? null : body.reviewNote(),
        body == null ? null : body.reviewer());
  }

  @PostMapping("/swaps/{requestId}/reject")
  public SessionDetailView reject(@PathVariable Long requestId,
      @RequestBody(required = false) ReviewSwapRequest body) {
    return stageService.rejectSwap(requestId, body == null ? null : body.reviewNote(),
        body == null ? null : body.reviewer());
  }
}
