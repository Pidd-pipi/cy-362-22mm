package com.generated.ldmurdergame.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.generated.ldmurdergame.dto.AddPlayerRequest;
import com.generated.ldmurdergame.dto.ChangeView;
import com.generated.ldmurdergame.dto.ConflictStage;
import com.generated.ldmurdergame.dto.CreateChangeRequest;
import com.generated.ldmurdergame.dto.CreateStageRequest;
import com.generated.ldmurdergame.dto.DecideChangeRequest;
import com.generated.ldmurdergame.dto.HostView;
import com.generated.ldmurdergame.dto.PlayerView;
import com.generated.ldmurdergame.dto.RoleView;
import com.generated.ldmurdergame.dto.SeatView;
import com.generated.ldmurdergame.dto.StageSummary;
import com.generated.ldmurdergame.dto.StageView;
import com.generated.ldmurdergame.dto.VersionView;
import com.generated.ldmurdergame.exception.ApiException;
import com.generated.ldmurdergame.exception.ScheduleConflictException;
import com.generated.ldmurdergame.mapper.DrawVersionMapper;
import com.generated.ldmurdergame.mapper.HostMapper;
import com.generated.ldmurdergame.mapper.RoleChangeMapper;
import com.generated.ldmurdergame.mapper.ScriptMapper;
import com.generated.ldmurdergame.mapper.ScriptRoleMapper;
import com.generated.ldmurdergame.mapper.SeatMapper;
import com.generated.ldmurdergame.mapper.StageMapper;
import com.generated.ldmurdergame.mapper.StagePlayerMapper;
import com.generated.ldmurdergame.model.DrawVersion;
import com.generated.ldmurdergame.model.Host;
import com.generated.ldmurdergame.model.RoleChange;
import com.generated.ldmurdergame.model.Script;
import com.generated.ldmurdergame.model.ScriptRole;
import com.generated.ldmurdergame.model.Seat;
import com.generated.ldmurdergame.model.Stage;
import com.generated.ldmurdergame.model.StagePlayer;

/**
 * 角色开演台：
 * 1. 选剧本/日期时间/主持人，按“脚本时长 + 20 分钟清场期”查主持人档期，撞档指出冲突场次；
 * 2. 名单达到开演人数后按角色性别要求抽签入座，重抽前保留上一版；
 * 3. 确认开演后角色关系定版，后续换角走店长确认，页面展示角色册与历次调整。
 */
@Service
public class StageConsoleService {
  public static final int CLEARING_MINUTES = 20;
  private static final String STATUS_SCHEDULED = "SCHEDULED";
  private static final String STATUS_CONFIRMED = "CONFIRMED";
  private static final String TRIGGER_DRAW = "DRAW";
  private static final String TRIGGER_REDRAW = "REDRAW";
  private static final String TRIGGER_CHANGE = "CHANGE";
  private static final String PENDING = "PENDING";
  private static final String APPROVED = "APPROVED";
  private static final String REJECTED = "REJECTED";
  private static final String SWAP = "SWAP";
  private static final String REPLACE = "REPLACE";

  private final ScriptMapper scriptMapper;
  private final ScriptRoleMapper roleMapper;
  private final HostMapper hostMapper;
  private final StageMapper stageMapper;
  private final StagePlayerMapper playerMapper;
  private final DrawVersionMapper versionMapper;
  private final SeatMapper seatMapper;
  private final RoleChangeMapper changeMapper;
  private final RoleDrawAssigner assigner;

  public StageConsoleService(ScriptMapper scriptMapper,
                             ScriptRoleMapper roleMapper,
                             HostMapper hostMapper,
                             StageMapper stageMapper,
                             StagePlayerMapper playerMapper,
                             DrawVersionMapper versionMapper,
                             SeatMapper seatMapper,
                             RoleChangeMapper changeMapper,
                             RoleDrawAssigner assigner) {
    this.scriptMapper = scriptMapper;
    this.roleMapper = roleMapper;
    this.hostMapper = hostMapper;
    this.stageMapper = stageMapper;
    this.playerMapper = playerMapper;
    this.versionMapper = versionMapper;
    this.seatMapper = seatMapper;
    this.changeMapper = changeMapper;
    this.assigner = assigner;
  }

  public List<StageSummary> listStages() {
    List<Stage> stages = stageMapper.findAll();
    List<StageSummary> result = new ArrayList<>();
    for (Stage stage : stages) {
      Script script = scriptMapper.findById(stage.getScriptId());
      Host host = hostMapper.findById(stage.getHostId());
      int activeCount = playerMapper.countActive(stage.getId());
      Integer currentVersionNo = latestVersionNo(stage.getId());
      result.add(new StageSummary(
          stage.getId(),
          script.getName(),
          script.getGenre(),
          host.getName(),
          stage.getStartAt(),
          stage.getEndAt(),
          stage.getClearEndAt(),
          stage.getStatus(),
          script.getPlayerCount(),
          activeCount,
          activeCount >= script.getPlayerCount(),
          currentVersionNo));
    }
    return result;
  }

  /** 档期预检（不创建场次）：返回与拟开场次撞档的已有场次。 */
  public List<ConflictStage> checkConflicts(Long scriptId, Long hostId, LocalDateTime startAt) {
    Script script = requireScript(scriptId);
    requireHost(hostId);
    LocalDateTime clearEnd = startAt.plusMinutes(script.getDurationMinutes() + CLEARING_MINUTES);
    return toConflictViews(stageMapper.findHostConflicts(hostId, -1L, startAt, clearEnd));
  }

  @Transactional
  public StageSummary createStage(CreateStageRequest request) {
    Script script = requireScript(request.scriptId());
    Host host = requireHost(request.hostId());
    LocalDateTime start = request.startAt();
    if (start.isBefore(LocalDateTime.now().minusMinutes(1))) {
      throw new ApiException("开场时间不能早于当前时间");
    }
    LocalDateTime end = start.plusMinutes(script.getDurationMinutes());
    LocalDateTime clearEnd = end.plusMinutes(CLEARING_MINUTES);

    List<Stage> conflicts = stageMapper.findHostConflicts(host.getId(), -1L, start, clearEnd);
    if (!conflicts.isEmpty()) {
      throw new ScheduleConflictException(
          "主持人「" + host.getName() + "」该时段已有场次（含 20 分钟清场期），无法重复排档",
          toConflictViews(conflicts));
    }

    Stage stage = new Stage();
    stage.setScriptId(script.getId());
    stage.setHostId(host.getId());
    stage.setStartAt(start);
    stage.setEndAt(end);
    stage.setClearEndAt(clearEnd);
    stage.setStatus(STATUS_SCHEDULED);
    stageMapper.insert(stage);

    return new StageSummary(stage.getId(), script.getName(), script.getGenre(), host.getName(),
        start, end, clearEnd, STATUS_SCHEDULED, script.getPlayerCount(), 0, false, null);
  }

  public StageView getStage(Long stageId) {
    Stage stage = requireStage(stageId);
    Script script = requireScript(stage.getScriptId());
    Host host = requireHost(stage.getHostId());
    List<ScriptRole> roles = roleMapper.findByScriptId(script.getId());
    List<StagePlayer> players = playerMapper.findByStageId(stageId);
    List<DrawVersion> versions = versionMapper.findByStageId(stageId);
    List<RoleChange> changes = changeMapper.findByStageId(stageId);

    Map<Long, ScriptRole> roleMap = roles.stream()
        .collect(Collectors.toMap(ScriptRole::getId, Function.identity()));
    Map<Long, StagePlayer> playerMap = players.stream()
        .collect(Collectors.toMap(StagePlayer::getId, Function.identity()));

    List<Long> versionIds = versions.stream().map(DrawVersion::getId).toList();
    Map<Long, List<Seat>> seatsByVersion = new HashMap<>();
    if (!versionIds.isEmpty()) {
      for (Seat seat : seatMapper.findByVersionIds(versionIds)) {
        seatsByVersion.computeIfAbsent(seat.getVersionId(), key -> new ArrayList<>()).add(seat);
      }
    }
    Map<Long, Integer> versionNoMap = versions.stream()
        .collect(Collectors.toMap(DrawVersion::getId, DrawVersion::getVersionNo));

    List<VersionView> versionViews = versions.stream()
        .map(version -> toVersionView(version,
            seatsByVersion.getOrDefault(version.getId(), Collections.emptyList()),
            roleMap, playerMap))
        .toList();

    List<ChangeView> changeViews = changes.stream()
        .map(change -> toChangeView(change, roleMap, playerMap, versionNoMap))
        .toList();

    List<RoleView> roleViews = roles.stream()
        .map(role -> new RoleView(role.getId(), role.getName(), role.getGender(), role.getProfile()))
        .toList();
    List<PlayerView> playerViews = players.stream()
        .map(player -> new PlayerView(player.getId(), player.getPlayerName(), player.getGender(),
            Boolean.TRUE.equals(player.getWaiting())))
        .sorted(Comparator.comparing(PlayerView::waiting))
        .toList();

    return new StageView(stage.getId(), script.getId(), script.getName(), script.getGenre(),
        script.getDifficulty(), script.getDurationMinutes(), script.getPlayerCount(),
        host.getId(), host.getName(), host.getTitle(),
        stage.getStartAt(), stage.getEndAt(), stage.getClearEndAt(),
        stage.getStatus(), stage.getConfirmedAt(),
        roleViews, playerViews, versionViews, changeViews);
  }

  @Transactional
  public PlayerView addPlayer(Long stageId, AddPlayerRequest request) {
    Stage stage = requireStage(stageId);
    Script script = requireScript(stage.getScriptId());
    String name = request.playerName().trim();
    if (name.isEmpty()) {
      throw new ApiException("玩家姓名不能为空");
    }
    List<StagePlayer> existed = playerMapper.findByStageId(stageId);
    boolean duplicate = existed.stream().anyMatch(player -> player.getPlayerName().equals(name));
    if (duplicate) {
      throw new ApiException("玩家「" + name + "」已在本场名单中");
    }

    // 达到开演人数后到场的玩家自动进入候补，只有开演人数的玩家参与抽签
    int activeCount = (int) existed.stream().filter(player -> !Boolean.TRUE.equals(player.getWaiting())).count();
    boolean waiting = activeCount >= script.getPlayerCount();

    StagePlayer player = new StagePlayer();
    player.setStageId(stageId);
    player.setPlayerName(name);
    player.setGender(request.gender());
    player.setWaiting(waiting);
    playerMapper.insert(player);
    return new PlayerView(player.getId(), name, request.gender(), waiting);
  }

  @Transactional
  public void removePlayer(Long stageId, Long playerId) {
    Stage stage = requireStage(stageId);
    StagePlayer player = playerMapper.findById(playerId);
    if (player == null || !player.getStageId().equals(stageId)) {
      throw new ApiException("名单中没有这位玩家");
    }
    Integer versionNo = latestVersionNo(stageId);
    if (versionNo != null && STATUS_SCHEDULED.equals(stage.getStatus())) {
      throw new ApiException("已完成抽签，重抽前名单不可删减；如确需换人请重新抽签或开演后走换角审批");
    }
    if (STATUS_CONFIRMED.equals(stage.getStatus())) {
      throw new ApiException("已确认开演，玩家离场请通过候补顶替的换角申请处理");
    }
    playerMapper.delete(playerId, stageId);
  }

  /** 抽签入座：名单达到开演人数才开放；重抽时上一版完整保留为历史版本。 */
  @Transactional
  public StageView redraw(Long stageId, String note, boolean initial) {
    Stage stage = requireStage(stageId);
    if (STATUS_CONFIRMED.equals(stage.getStatus())) {
      throw new ApiException("已确认开演，角色关系已定版；如需调整请提交换角申请并由店长确认");
    }
    Script script = requireScript(stage.getScriptId());
    List<ScriptRole> roles = roleMapper.findByScriptId(script.getId());
    if (roles.size() != script.getPlayerCount()) {
      throw new ApiException("剧本角色配置与开演人数不一致，请联系管理员修正角色册");
    }
    List<StagePlayer> all = playerMapper.findByStageId(stageId);
    List<StagePlayer> active = all.stream()
        .filter(player -> !Boolean.TRUE.equals(player.getWaiting()))
        .toList();
    if (active.size() < script.getPlayerCount()) {
      throw new ApiException("到场人数不足开演人数（" + script.getPlayerCount() + " 人），暂不能抽签");
    }

    int previous = versionMapper.maxVersionNo(stageId);
    if (initial && previous > 0) {
      throw new ApiException("本场已经抽过签，请使用重抽");
    }
    if (!initial && previous == 0) {
      throw new ApiException("本场还未抽过签，请先进行首次抽签");
    }

    Map<Long, Long> assignment = assigner.assign(roles, active);

    DrawVersion version = new DrawVersion();
    version.setStageId(stageId);
    version.setVersionNo(previous + 1);
    version.setDrawTrigger(initial ? TRIGGER_DRAW : TRIGGER_REDRAW);
    String trimmedNote = note == null ? null : note.trim();
    version.setNote(trimmedNote != null && !trimmedNote.isEmpty()
        ? trimmedNote
        : (initial ? "首次抽签入座" : "店长重新抽签，上一版已存档"));
    versionMapper.insert(version);

    List<Seat> seats = new ArrayList<>();
    assignment.forEach((roleId, playerId) -> {
      Seat seat = new Seat();
      seat.setVersionId(version.getId());
      seat.setRoleId(roleId);
      seat.setPlayerId(playerId);
      seats.add(seat);
    });
    seatMapper.insertBatch(seats);
    return getStage(stageId);
  }

  /** 确认开演：角色关系定版。 */
  @Transactional
  public StageView confirm(Long stageId) {
    Stage stage = requireStage(stageId);
    if (STATUS_CONFIRMED.equals(stage.getStatus())) {
      throw new ApiException("本场已经确认开演");
    }
    if (latestVersionNo(stageId) == null) {
      throw new ApiException("还未抽签入座，不能确认开演");
    }
    stageMapper.updateStatus(stageId, STATUS_CONFIRMED, LocalDateTime.now());
    return getStage(stageId);
  }

  /** 确认开演后的换角申请（不直接改动座次，等待店长确认）。 */
  @Transactional
  public ChangeView createChange(Long stageId, CreateChangeRequest request) {
    Stage stage = requireStage(stageId);
    if (!STATUS_CONFIRMED.equals(stage.getStatus())) {
      throw new ApiException("确认开演前可直接重新抽签，无需提交换角申请");
    }
    ScriptRole role = requireRole(request.roleId(), stage.getScriptId());
    DrawVersion current = requireLatestVersion(stageId);
    List<Seat> currentSeats = seatMapper.findByVersionId(current.getId());
    Seat targetSeat = currentSeats.stream()
        .filter(seat -> seat.getRoleId().equals(role.getId()))
        .findFirst()
        .orElseThrow(() -> new ApiException("当前座次中找不到该角色"));
    List<StagePlayer> players = playerMapper.findByStageId(stageId);
    Map<Long, StagePlayer> playerMap = players.stream()
        .collect(Collectors.toMap(StagePlayer::getId, Function.identity()));

    RoleChange change = new RoleChange();
    change.setStageId(stageId);
    change.setRoleId(role.getId());
    change.setFromPlayerId(targetSeat.getPlayerId());
    change.setReason(request.reason() == null ? null : request.reason().trim());
    change.setStatus(PENDING);

    if (SWAP.equals(request.changeType())) {
      if (request.toPlayerId() == null) {
        throw new ApiException("互换角色请选择另一位在场玩家");
      }
      StagePlayer other = playerMap.get(request.toPlayerId());
      if (other == null || Boolean.TRUE.equals(other.getWaiting())) {
        throw new ApiException("互换对象必须是本场在场玩家");
      }
      if (other.getId().equals(targetSeat.getPlayerId())) {
        throw new ApiException("不能与自己互换角色");
      }
      change.setChangeType(SWAP);
      change.setToPlayerId(other.getId());
    } else {
      if (request.waitingPlayerId() == null) {
        throw new ApiException("候补顶替请选择一位候补玩家");
      }
      StagePlayer waiting = playerMap.get(request.waitingPlayerId());
      if (waiting == null || !Boolean.TRUE.equals(waiting.getWaiting())) {
        throw new ApiException("顶替者必须是候补名单中的玩家");
      }
      change.setChangeType(REPLACE);
      change.setWaitingPlayerId(waiting.getId());
    }
    changeMapper.insert(change);
    return toChangeView(change,
        role,
        playerMap.get(targetSeat.getPlayerId()),
        change.getToPlayerId() == null ? null : playerMap.get(change.getToPlayerId()),
        change.getWaitingPlayerId() == null ? null : playerMap.get(change.getWaitingPlayerId()),
        null);
  }

  /** 店长审批换角：批准则克隆当前版本形成定版后的新版本，驳回则仅记录意见。 */
  @Transactional
  public StageView decideChange(Long stageId, Long changeId, DecideChangeRequest request) {
    Stage stage = requireStage(stageId);
    RoleChange change = changeMapper.findById(changeId);
    if (change == null || !change.getStageId().equals(stageId)) {
      throw new ApiException("找不到该换角申请");
    }
    if (!PENDING.equals(change.getStatus())) {
      throw new ApiException("该换角申请已审批：" + (APPROVED.equals(change.getStatus()) ? "已批准" : "已驳回"));
    }

    String note = request.managerNote() == null ? null : request.managerNote().trim();
    if (APPROVED.equals(request.decision())) {
      DrawVersion current = requireLatestVersion(stageId);
      List<Seat> seats = seatMapper.findByVersionId(current.getId());

      DrawVersion next = new DrawVersion();
      next.setStageId(stageId);
      next.setVersionNo(current.getVersionNo() + 1);
      next.setDrawTrigger(TRIGGER_CHANGE);
      next.setNote(SWAP.equals(change.getChangeType())
          ? "店长批准换角：两位玩家互换角色"
          : "店长批准换角：候补玩家顶替入座");
      versionMapper.insert(next);
      seatMapper.insertBatch(cloneSeats(seats, next.getId()));

      if (SWAP.equals(change.getChangeType())) {
        Seat first = seats.stream().filter(seat -> seat.getPlayerId().equals(change.getFromPlayerId())).findFirst()
            .orElseThrow(() -> new ApiException("座次数据异常：找不到原玩家"));
        Seat second = seats.stream().filter(seat -> seat.getPlayerId().equals(change.getToPlayerId())).findFirst()
            .orElseThrow(() -> new ApiException("座次数据异常：找不到互换玩家"));
        seatMapper.updateSeatPlayer(next.getId(), first.getRoleId(), change.getToPlayerId());
        seatMapper.updateSeatPlayer(next.getId(), second.getRoleId(), change.getFromPlayerId());
      } else {
        Seat vacated = seats.stream().filter(seat -> seat.getRoleId().equals(change.getRoleId())).findFirst()
            .orElseThrow(() -> new ApiException("座次数据异常：找不到对应角色"));
        seatMapper.updateSeatPlayer(next.getId(), vacated.getRoleId(), change.getWaitingPlayerId());
        // 被顶替的在场玩家转为候补，候补玩家入座
        playerMapper.updateWaiting(change.getWaitingPlayerId(), false);
        playerMapper.updateWaiting(change.getFromPlayerId(), true);
      }
      changeMapper.updateVersion(changeId, next.getId());
    }

    changeMapper.decide(changeId, request.decision(), note, LocalDateTime.now());
    return getStage(stageId);
  }

  private List<Seat> cloneSeats(List<Seat> seats, Long newVersionId) {
    return seats.stream().map(seat -> {
      Seat copy = new Seat();
      copy.setVersionId(newVersionId);
      copy.setRoleId(seat.getRoleId());
      copy.setPlayerId(seat.getPlayerId());
      return copy;
    }).toList();
  }

  private DrawVersion requireLatestVersion(Long stageId) {
    List<DrawVersion> versions = versionMapper.findByStageId(stageId);
    if (versions.isEmpty()) {
      throw new ApiException("本场还没有抽签版本");
    }
    return versions.get(0); // 按 version_no DESC 排序
  }

  private Integer latestVersionNo(Long stageId) {
    int max = versionMapper.maxVersionNo(stageId);
    return max == 0 ? null : max;
  }

  private List<ConflictStage> toConflictViews(List<Stage> conflicts) {
    return conflicts.stream().map(stage -> {
      Script script = scriptMapper.findById(stage.getScriptId());
      Host host = hostMapper.findById(stage.getHostId());
      return new ConflictStage(stage.getId(), script.getName(), host.getName(),
          stage.getStartAt(), stage.getEndAt(), stage.getClearEndAt(), stage.getStatus());
    }).toList();
  }

  private VersionView toVersionView(DrawVersion version,
                                    List<Seat> seats,
                                    Map<Long, ScriptRole> roleMap,
                                    Map<Long, StagePlayer> playerMap) {
    List<SeatView> seatViews = seats.stream()
        .map(seat -> {
          ScriptRole role = roleMap.get(seat.getRoleId());
          StagePlayer player = playerMap.get(seat.getPlayerId());
          return new SeatView(
              seat.getRoleId(),
              role == null ? "未知角色" : role.getName(),
              role == null ? null : role.getGender(),
              seat.getPlayerId(),
              player == null ? "（名单已移除）" : player.getPlayerName(),
              player == null ? null : player.getGender());
        })
        .sorted(Comparator.comparing(SeatView::roleName))
        .toList();
    return new VersionView(version.getVersionNo(), version.getDrawTrigger(),
        version.getNote(), version.getCreatedAt(), seatViews);
  }

  private ChangeView toChangeView(RoleChange change,
                                  Map<Long, ScriptRole> roleMap,
                                  Map<Long, StagePlayer> playerMap,
                                  Map<Long, Integer> versionNoMap) {
    return toChangeView(change,
        roleMap.get(change.getRoleId()),
        playerMap.get(change.getFromPlayerId()),
        playerMap.get(change.getToPlayerId()),
        playerMap.get(change.getWaitingPlayerId()),
        change.getVersionId() == null ? null : versionNoMap.get(change.getVersionId()));
  }

  private ChangeView toChangeView(RoleChange change,
                                  ScriptRole role,
                                  StagePlayer from,
                                  StagePlayer to,
                                  StagePlayer waiting,
                                  Integer versionNo) {
    return new ChangeView(change.getId(), change.getChangeType(), change.getStatus(),
        change.getRoleId(), role == null ? "未知角色" : role.getName(),
        change.getFromPlayerId(), from == null ? null : from.getPlayerName(),
        change.getToPlayerId(), to == null ? null : to.getPlayerName(),
        change.getWaitingPlayerId(), waiting == null ? null : waiting.getPlayerName(),
        versionNo, change.getReason(), change.getManagerNote(),
        change.getCreatedAt(), change.getDecidedAt());
  }

  private Stage requireStage(Long stageId) {
    Stage stage = stageMapper.findById(stageId);
    if (stage == null) {
      throw new ApiException("开演台场次不存在");
    }
    return stage;
  }

  private Script requireScript(Long scriptId) {
    Script script = scriptMapper.findById(scriptId);
    if (script == null) {
      throw new ApiException("剧本不存在");
    }
    return script;
  }

  private Host requireHost(Long hostId) {
    Host host = hostMapper.findById(hostId);
    if (host == null) {
      throw new ApiException("主持人不存在");
    }
    return host;
  }

  private ScriptRole requireRole(Long roleId, Long scriptId) {
    ScriptRole role = roleMapper.findById(roleId);
    if (role == null || !role.getScriptId().equals(scriptId)) {
      throw new ApiException("角色册中没有该角色");
    }
    return role;
  }
}
