package com.generated.ldmurdergame.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.generated.ldmurdergame.domain.AssignmentVersion;
import com.generated.ldmurdergame.domain.Gender;
import com.generated.ldmurdergame.domain.Host;
import com.generated.ldmurdergame.domain.Player;
import com.generated.ldmurdergame.domain.RoleSwapRequest;
import com.generated.ldmurdergame.domain.Script;
import com.generated.ldmurdergame.domain.ScriptRole;
import com.generated.ldmurdergame.domain.StageSession;
import com.generated.ldmurdergame.dto.ConflictSessionView;
import com.generated.ldmurdergame.dto.CreateSessionRequest;
import com.generated.ldmurdergame.dto.SessionDetailView;
import com.generated.ldmurdergame.dto.SessionView;
import com.generated.ldmurdergame.exception.ApiException;
import com.generated.ldmurdergame.exception.ConflictException;
import com.generated.ldmurdergame.mapper.AssignmentMapper;
import com.generated.ldmurdergame.mapper.ConflictRow;
import com.generated.ldmurdergame.mapper.HostMapper;
import com.generated.ldmurdergame.mapper.PlayerMapper;
import com.generated.ldmurdergame.mapper.RosterMapper;
import com.generated.ldmurdergame.mapper.ScriptMapper;
import com.generated.ldmurdergame.mapper.ScriptRoleMapper;
import com.generated.ldmurdergame.mapper.SessionMapper;
import com.generated.ldmurdergame.mapper.SwapMapper;

/** 开演台命令服务：建场/档期、名单、抽签入座、确认定版、换角审批。 */
@Service
public class StageService {
  private static final int DEFAULT_CLEANUP_MINUTES = 20;
  private static final String DEFAULT_OPERATOR = "店长";

  private final ScriptMapper scriptMapper;
  private final ScriptRoleMapper roleMapper;
  private final HostMapper hostMapper;
  private final PlayerMapper playerMapper;
  private final SessionMapper sessionMapper;
  private final RosterMapper rosterMapper;
  private final AssignmentMapper assignmentMapper;
  private final SwapMapper swapMapper;
  private final StageQueryService queryService;

  public StageService(ScriptMapper scriptMapper, ScriptRoleMapper roleMapper,
      HostMapper hostMapper, PlayerMapper playerMapper, SessionMapper sessionMapper,
      RosterMapper rosterMapper, AssignmentMapper assignmentMapper, SwapMapper swapMapper,
      StageQueryService queryService) {
    this.scriptMapper = scriptMapper;
    this.roleMapper = roleMapper;
    this.hostMapper = hostMapper;
    this.playerMapper = playerMapper;
    this.sessionMapper = sessionMapper;
    this.rosterMapper = rosterMapper;
    this.assignmentMapper = assignmentMapper;
    this.swapMapper = swapMapper;
    this.queryService = queryService;
  }

  /** 建场：脚本时长 + 清场期（默认 20 分钟），校验主持人档期，撞档返回冲突场次。 */
  @Transactional
  public SessionView createSession(CreateSessionRequest request) {
    Script script = scriptMapper.findById(request.scriptId());
    if (script == null) {
      throw new ApiException("所选剧本不存在。");
    }
    Host host = hostMapper.findById(request.hostId());
    if (host == null) {
      throw new ApiException("所选主持人不存在。");
    }
    LocalDate date = request.date();
    LocalTime startTime = request.startTime();
    if (date == null || startTime == null) {
      throw new ApiException("请选择开场日期与开始时间。");
    }
    int cleanup = request.cleanupMinutes() == null ? DEFAULT_CLEANUP_MINUTES
        : request.cleanupMinutes();
    if (cleanup < 0) {
      throw new ApiException("清场期不能为负数。");
    }

    LocalDateTime startAt = LocalDateTime.of(date, startTime);
    LocalDateTime endAt = startAt.plusMinutes(script.getDurationMinutes() + cleanup);

    List<ConflictRow> conflictRows = sessionMapper.findHostConflicts(host.getId(), startAt,
        endAt, null);
    if (!conflictRows.isEmpty()) {
      List<ConflictSessionView> conflicts = queryService.toConflictViews(conflictRows);
      throw new ConflictException(
          "主持人「" + host.getName() + "」该时段撞档（脚本时长 "
              + script.getDurationMinutes() + " 分钟 + 清场 " + cleanup + " 分钟）。",
          conflicts);
    }

    StageSession session = new StageSession();
    session.setScriptId(script.getId());
    session.setHostId(host.getId());
    session.setSessionDate(date);
    session.setStartTime(startTime);
    session.setEndTime(endAt.toLocalTime());
    session.setStartAt(startAt);
    session.setEndAt(endAt);
    session.setCleanupMinutes(cleanup);
    session.setStatus(StageSession.STAGING);
    session.setNote(request.note());
    sessionMapper.insert(session);
    return queryService.listSessions().stream()
        .filter(view -> view.id().equals(session.getId())).findFirst()
        .orElseThrow(() -> new ApiException("场次创建失败。"));
  }

  @Transactional
  public SessionDetailView addToRoster(Long sessionId, Long playerId, String operator) {
    StageSession session = requireStaging(sessionId);
    Player player = playerMapper.findById(playerId);
    if (player == null) {
      throw new ApiException("所选玩家不存在。");
    }
    if (rosterMapper.exists(sessionId, playerId) > 0) {
      throw new ApiException("玩家「" + player.getName() + "」已在名单中。");
    }
    rosterMapper.insert(sessionId, playerId);
    return queryService.getDetail(sessionId);
  }

  @Transactional
  public SessionDetailView removeFromRoster(Long sessionId, Long playerId) {
    requireStaging(sessionId);
    rosterMapper.delete(sessionId, playerId);
    return queryService.getDetail(sessionId);
  }

  /**
   * 按角色性别要求抽签入座：每个角色只留一位玩家，多余者候补。
   * 每次抽签（含重抽）都先把上一版作为不可变版本保留。
   */
  @Transactional
  public SessionDetailView draw(Long sessionId, boolean reDraw, String operator) {
    StageSession session = requireStaging(sessionId);
    List<ScriptRole> roles = roleMapper.findByScript(session.getScriptId());
    if (roles.isEmpty()) {
      throw new ApiException("该剧本尚未配置角色，无法抽签。");
    }
    List<com.generated.ldmurdergame.mapper.RosterRow> rosterRows =
        rosterMapper.findRoster(sessionId);
    if (rosterRows.size() < roles.size()) {
      throw new ApiException("名单人数不足：需 " + roles.size() + " 人，当前 "
          + rosterRows.size() + " 人，达到开演人数后才能抽签。");
    }

    Map<Long, Player> playersById = new LinkedHashMap<>();
    List<Player> males = new ArrayList<>();
    List<Player> females = new ArrayList<>();
    for (com.generated.ldmurdergame.mapper.RosterRow row : rosterRows) {
      Player player = playerMapper.findById(row.getPlayerId());
      if (player == null) {
        continue;
      }
      playersById.put(player.getId(), player);
      if (Gender.MALE.equals(player.getGender())) {
        males.add(player);
      } else if (Gender.FEMALE.equals(player.getGender())) {
        females.add(player);
      }
    }

    List<ScriptRole> maleRoles = new ArrayList<>();
    List<ScriptRole> femaleRoles = new ArrayList<>();
    List<ScriptRole> anyRoles = new ArrayList<>();
    for (ScriptRole role : roles) {
      switch (requirement(role)) {
        case Gender.MALE -> maleRoles.add(role);
        case Gender.FEMALE -> femaleRoles.add(role);
        default -> anyRoles.add(role);
      }
    }
    if (males.size() < maleRoles.size()) {
      throw new ApiException("性别要求无法满足：男性角色需 " + maleRoles.size()
          + " 人，名单仅 " + males.size() + " 名男性玩家。");
    }
    if (females.size() < femaleRoles.size()) {
      throw new ApiException("性别要求无法满足：女性角色需 " + femaleRoles.size()
          + " 人，名单仅 " + females.size() + " 名女性玩家。");
    }

    Collections.shuffle(males);
    Collections.shuffle(females);
    Map<Long, Long> assignment = new LinkedHashMap<>();
    Set<Long> used = new HashSet<>();
    assignGroup(maleRoles, males, assignment, used);
    assignGroup(femaleRoles, females, assignment, used);

    List<Player> leftovers = new ArrayList<>();
    for (Player player : playersById.values()) {
      if (!used.contains(player.getId())) {
        leftovers.add(player);
      }
    }
    Collections.shuffle(leftovers);
    if (leftovers.size() < anyRoles.size()) {
      throw new ApiException("性别要求无法满足：不限性别角色缺 "
          + (anyRoles.size() - leftovers.size()) + " 人。");
    }
    assignGroup(anyRoles, leftovers, assignment, used);

    Integer max = assignmentMapper.maxVersionNo(sessionId);
    int versionNo = (max == null ? 0 : max) + 1;
    AssignmentVersion version = new AssignmentVersion();
    version.setSessionId(sessionId);
    version.setVersionNo(versionNo);
    version.setKind(AssignmentVersion.DRAW);
    version.setCreatedBy(op(operator));
    version.setChangeNote((reDraw ? "重新抽签入座" : "抽签入座") + "（第 " + versionNo
        + " 版，已保留历史版本）");
    assignmentMapper.insertVersion(version);
    for (ScriptRole role : roles) {
      assignmentMapper.insertItem(version.getId(), sessionId, role.getId(),
          assignment.get(role.getId()));
    }
    return queryService.getDetail(sessionId);
  }

  /** 确认开演：当前座位冻结为定版，之后换角需店长确认。 */
  @Transactional
  public SessionDetailView confirm(Long sessionId, String operator) {
    StageSession session = requireStaging(sessionId);
    AssignmentVersion current = assignmentMapper.findCurrentVersion(sessionId);
    if (current == null) {
      throw new ApiException("尚未抽签入座，无法确认开演。");
    }
    List<SeatRef> seats = loadCurrentSeats(sessionId);
    if (seats.stream().anyMatch(seat -> seat.playerId() == null)) {
      throw new ApiException("仍有空位角色，请先完成抽签入座。");
    }

    // 把当前座位再固化一份“确认定版”，便于角色册区分。
    Integer max = assignmentMapper.maxVersionNo(sessionId);
    AssignmentVersion frozen = new AssignmentVersion();
    frozen.setSessionId(sessionId);
    frozen.setVersionNo((max == null ? 0 : max) + 1);
    frozen.setKind(AssignmentVersion.CONFIRM);
    frozen.setCreatedBy(op(operator));
    frozen.setChangeNote("确认开演，角色关系定版");
    assignmentMapper.insertVersion(frozen);
    for (SeatRef seat : seats) {
      assignmentMapper.insertItem(frozen.getId(), sessionId, seat.roleId(), seat.playerId());
    }

    sessionMapper.updateStatus(sessionId, StageSession.CONFIRMED, LocalDateTime.now());
    return queryService.getDetail(sessionId);
  }

  /** 确认后发起换角申请（待店长确认）。 */
  @Transactional
  public SessionDetailView requestSwap(Long sessionId, Long roleId, Long toPlayerId,
      String reason, String operator) {
    StageSession session = requireSession(sessionId);
    if (!StageSession.CONFIRMED.equals(session.getStatus())) {
      throw new ApiException("只有确认开演后的场次才走换角申请，请在入座阶段直接重新抽签。");
    }
    ScriptRole role = requireRoleOfScript(roleId, session.getScriptId());
    Player target = playerMapper.findById(toPlayerId);
    if (target == null) {
      throw new ApiException("目标玩家不存在。");
    }
    if (rosterMapper.exists(sessionId, toPlayerId) == 0) {
      throw new ApiException("目标玩家不在本场名单中。");
    }
    if (!Gender.matches(role.getGenderRequirement(), target.getGender())) {
      throw new ApiException("玩家「" + target.getName() + "」不符合角色「" + role.getName()
          + "」的性别要求。");
    }
    if (swapMapper.countPendingForRole(sessionId, roleId) > 0) {
      throw new ApiException("该角色已有待确认的换角申请，请先处理。");
    }
    Long currentPlayerId = assignmentMapper.findCurrentPlayerForRole(sessionId, roleId);
    if (toPlayerId.equals(currentPlayerId)) {
      throw new ApiException("该玩家已持有此角色，无需换角。");
    }

    RoleSwapRequest request = new RoleSwapRequest();
    request.setSessionId(sessionId);
    request.setRoleId(roleId);
    request.setFromPlayerId(currentPlayerId);
    request.setToPlayerId(toPlayerId);
    request.setReason(reason);
    request.setStatus(RoleSwapRequest.PENDING);
    request.setCreatedBy(op(operator));
    swapMapper.insert(request);
    return queryService.getDetail(sessionId);
  }

  /** 店长批准换角：生成新版本并更新定版。 */
  @Transactional
  public SessionDetailView approveSwap(Long requestId, String reviewNote, String reviewer) {
    RoleSwapRequest request = requirePendingSwap(requestId);
    StageSession session = requireSession(request.getSessionId());
    if (!StageSession.CONFIRMED.equals(session.getStatus())) {
      throw new ApiException("场次未确认，不能审批换角。");
    }
    List<SeatRef> seats = loadCurrentSeats(session.getId());
    Long fromPlayerId = request.getFromPlayerId();
    Long toPlayerId = request.getToPlayerId();
    boolean changed = false;
    for (int i = 0; i < seats.size(); i++) {
      SeatRef seat = seats.get(i);
      if (seat.roleId().equals(request.getRoleId())
          && java.util.Objects.equals(seat.playerId(), fromPlayerId)) {
        seats.set(i, new SeatRef(seat.roleId(), toPlayerId));
        changed = true;
        break;
      }
    }
    if (!changed) {
      throw new ApiException("当前座位与申请时不一致，无法批准（角色可能已被调整）。");
    }

    ScriptRole role = requireRoleOfScript(request.getRoleId(), session.getScriptId());
    Map<Long, String> names = queryService.playerNameLookup(List.of(fromPlayerId, toPlayerId));
    Integer max = assignmentMapper.maxVersionNo(session.getId());
    AssignmentVersion version = new AssignmentVersion();
    version.setSessionId(session.getId());
    version.setVersionNo((max == null ? 0 : max) + 1);
    version.setKind(AssignmentVersion.MANUAL);
    version.setCreatedBy(op(reviewer));
    version.setChangeNote("换角（店长确认，申请#" + request.getId() + "）：角色「" + role.getName()
        + "」 " + names.getOrDefault(fromPlayerId, "空位") + " → "
        + names.getOrDefault(toPlayerId, "玩家#" + toPlayerId));
    assignmentMapper.insertVersion(version);
    for (SeatRef seat : seats) {
      assignmentMapper.insertItem(version.getId(), session.getId(), seat.roleId(),
          seat.playerId());
    }

    swapMapper.review(requestId, RoleSwapRequest.APPROVED, reviewNote, op(reviewer),
        LocalDateTime.now());
    return queryService.getDetail(session.getId());
  }

  /** 店长驳回换角。 */
  @Transactional
  public SessionDetailView rejectSwap(Long requestId, String reviewNote, String reviewer) {
    RoleSwapRequest request = requirePendingSwap(requestId);
    swapMapper.review(requestId, RoleSwapRequest.REJECTED, reviewNote, op(reviewer),
        LocalDateTime.now());
    return queryService.getDetail(request.getSessionId());
  }

  // ---- helpers ----

  private void assignGroup(List<ScriptRole> roles, List<Player> pool, Map<Long, Long> assignment,
      Set<Long> used) {
    for (int i = 0; i < roles.size(); i++) {
      Player player = pool.get(i);
      assignment.put(roles.get(i).getId(), player.getId());
      used.add(player.getId());
    }
  }

  private String requirement(ScriptRole role) {
    String requirement = role.getGenderRequirement();
    return requirement == null ? Gender.ANY : requirement;
  }

  private List<SeatRef> loadCurrentSeats(Long sessionId) {
    AssignmentVersion current = assignmentMapper.findCurrentVersion(sessionId);
    List<SeatRef> seats = new ArrayList<>();
    if (current == null) {
      return seats;
    }
    for (com.generated.ldmurdergame.mapper.SeatRow row
        : assignmentMapper.findSeats(current.getId())) {
      seats.add(new SeatRef(row.getRoleId(), row.getPlayerId()));
    }
    return seats;
  }

  private StageSession requireSession(Long sessionId) {
    StageSession session = sessionMapper.findById(sessionId);
    if (session == null) {
      throw new ApiException("场次不存在：" + sessionId);
    }
    return session;
  }

  private StageSession requireStaging(Long sessionId) {
    StageSession session = requireSession(sessionId);
    if (!StageSession.STAGING.equals(session.getStatus())) {
      throw new ApiException("场次已" + statusText(session.getStatus())
          + "，入座与名单已锁定。");
    }
    return session;
  }

  private ScriptRole requireRoleOfScript(Long roleId, Long scriptId) {
    ScriptRole role = roleMapper.findById(roleId);
    if (role == null || !role.getScriptId().equals(scriptId)) {
      throw new ApiException("该角色不属于本场剧本。");
    }
    return role;
  }

  private RoleSwapRequest requirePendingSwap(Long requestId) {
    RoleSwapRequest request = swapMapper.findById(requestId);
    if (request == null) {
      throw new ApiException("换角申请不存在：" + requestId);
    }
    if (!RoleSwapRequest.PENDING.equals(request.getStatus())) {
      throw new ApiException("该换角申请已处理，不能重复审批。");
    }
    return request;
  }

  private String statusText(String status) {
    return switch (status) {
      case StageSession.CONFIRMED -> "确认开演（定版）";
      case StageSession.CANCELLED -> "取消";
      default -> "处于其他状态";
    };
  }

  private String op(String operator) {
    return (operator == null || operator.isBlank()) ? DEFAULT_OPERATOR : operator.trim();
  }

  private record SeatRef(Long roleId, Long playerId) {
  }
}
