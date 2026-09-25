package com.generated.ldmurdergame.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.generated.ldmurdergame.domain.Host;
import com.generated.ldmurdergame.domain.Player;
import com.generated.ldmurdergame.domain.RoleSwapRequest;
import com.generated.ldmurdergame.domain.Script;
import com.generated.ldmurdergame.domain.ScriptRole;
import com.generated.ldmurdergame.domain.StageSession;
import com.generated.ldmurdergame.domain.AssignmentVersion;
import com.generated.ldmurdergame.dto.ConflictSessionView;
import com.generated.ldmurdergame.dto.HostView;
import com.generated.ldmurdergame.dto.PlayerView;
import com.generated.ldmurdergame.dto.RoleView;
import com.generated.ldmurdergame.dto.RosterPlayerView;
import com.generated.ldmurdergame.dto.ScriptView;
import com.generated.ldmurdergame.dto.SeatView;
import com.generated.ldmurdergame.dto.SessionDetailView;
import com.generated.ldmurdergame.dto.SessionView;
import com.generated.ldmurdergame.dto.SwapRequestView;
import com.generated.ldmurdergame.dto.VersionView;
import com.generated.ldmurdergame.exception.ApiException;
import com.generated.ldmurdergame.mapper.AssignmentMapper;
import com.generated.ldmurdergame.mapper.ConflictRow;
import com.generated.ldmurdergame.mapper.HostMapper;
import com.generated.ldmurdergame.mapper.PlayerMapper;
import com.generated.ldmurdergame.mapper.RosterMapper;
import com.generated.ldmurdergame.mapper.RosterRow;
import com.generated.ldmurdergame.mapper.ScriptMapper;
import com.generated.ldmurdergame.mapper.ScriptRoleMapper;
import com.generated.ldmurdergame.mapper.SeatRow;
import com.generated.ldmurdergame.mapper.SessionMapper;
import com.generated.ldmurdergame.mapper.SwapMapper;

/** 只读视图组装，供命令服务与控制器复用。 */
@Service
public class StageQueryService {
  private final ScriptMapper scriptMapper;
  private final ScriptRoleMapper roleMapper;
  private final HostMapper hostMapper;
  private final PlayerMapper playerMapper;
  private final SessionMapper sessionMapper;
  private final RosterMapper rosterMapper;
  private final AssignmentMapper assignmentMapper;
  private final SwapMapper swapMapper;

  public StageQueryService(ScriptMapper scriptMapper, ScriptRoleMapper roleMapper,
      HostMapper hostMapper, PlayerMapper playerMapper, SessionMapper sessionMapper,
      RosterMapper rosterMapper, AssignmentMapper assignmentMapper, SwapMapper swapMapper) {
    this.scriptMapper = scriptMapper;
    this.roleMapper = roleMapper;
    this.hostMapper = hostMapper;
    this.playerMapper = playerMapper;
    this.sessionMapper = sessionMapper;
    this.rosterMapper = rosterMapper;
    this.assignmentMapper = assignmentMapper;
    this.swapMapper = swapMapper;
  }

  public List<ScriptView> listScripts() {
    List<ScriptView> result = new ArrayList<>();
    for (Script script : scriptMapper.findAll()) {
      result.add(toScriptView(script, roleMapper.findByScript(script.getId())));
    }
    return result;
  }

  public ScriptView getScript(Long scriptId) {
    Script script = scriptMapper.findById(scriptId);
    if (script == null) {
      throw new ApiException("剧本不存在：" + scriptId);
    }
    return toScriptView(script, roleMapper.findByScript(scriptId));
  }

  public List<HostView> listHosts() {
    return hostMapper.findAll().stream().map(this::toHostView).toList();
  }

  public List<PlayerView> listPlayers() {
    return playerMapper.findAll().stream().map(this::toPlayerView).toList();
  }

  public List<SessionView> listSessions() {
    List<SessionView> result = new ArrayList<>();
    for (StageSession session : sessionMapper.findAll()) {
      result.add(buildSessionView(session));
    }
    return result;
  }

  public SessionDetailView getDetail(Long sessionId) {
    StageSession session = requireSession(sessionId);
    List<ScriptRole> roles = roleMapper.findByScript(session.getScriptId());
    List<RosterPlayerView> roster = rosterMapper.findRoster(sessionId).stream()
        .map(this::toRosterView).toList();

    AssignmentVersion current = assignmentMapper.findCurrentVersion(sessionId);
    VersionView currentVersion = current == null ? null : toVersionView(current);

    List<Long> seatedIds = new ArrayList<>();
    if (currentVersion != null) {
      for (SeatView seat : currentVersion.seats()) {
        if (seat.playerId() != null) {
          seatedIds.add(seat.playerId());
        }
      }
    }
    List<RosterPlayerView> unseated = roster.stream()
        .filter(player -> !seatedIds.contains(player.playerId())).toList();

    List<VersionView> history = assignmentMapper.findVersions(sessionId).stream()
        .map(this::toVersionView).toList();
    List<SwapRequestView> swaps = swapMapper.findBySession(sessionId).stream()
        .map(this::toSwapView).toList();

    return new SessionDetailView(buildSessionView(session), roles.stream().map(this::toRoleView)
        .toList(), roster, unseated, currentVersion, history, swaps);
  }

  public List<ConflictSessionView> toConflictViews(List<ConflictRow> rows) {
    return rows.stream().map(row -> new ConflictSessionView(row.getSessionId(),
        row.getScriptName(), row.getHostName(), row.getSessionDate(), row.getStartTime(),
        row.getEndTime(), row.getStatus(), row.getStartAt(), row.getEndAt())).toList();
  }

  private StageSession requireSession(Long sessionId) {
    StageSession session = sessionMapper.findById(sessionId);
    if (session == null) {
      throw new ApiException("场次不存在：" + sessionId);
    }
    return session;
  }

  private SessionView buildSessionView(StageSession session) {
    Script script = scriptMapper.findById(session.getScriptId());
    Host host = hostMapper.findById(session.getHostId());
    List<ScriptRole> roles = roleMapper.findByScript(session.getScriptId());
    int rosterCount = rosterMapper.countRoster(session.getId());

    AssignmentVersion current = assignmentMapper.findCurrentVersion(session.getId());
    List<SeatView> seats = new ArrayList<>();
    int seated = 0;
    if (current != null) {
      seats = assignmentMapper.findSeats(current.getId()).stream().map(this::toSeatView).toList();
      for (SeatView seat : seats) {
        if (seat.playerId() != null) {
          seated++;
        }
      }
    }

    int required = script != null ? script.getPlayerCount() : roles.size();
    return new SessionView(session.getId(), session.getScriptId(),
        script != null ? script.getName() : "剧本#" + session.getScriptId(),
        session.getHostId(), host != null ? host.getName() : "DM#" + session.getHostId(),
        session.getSessionDate(), session.getStartTime(), session.getEndTime(),
        session.getStartAt(), session.getEndAt(), session.getCleanupMinutes(), required,
        rosterCount, seated, roles.size(), rosterCount >= required && required > 0,
        session.getStatus(), session.getConfirmedAt(), session.getNote(),
        session.getCreatedAt(), seats);
  }

  private VersionView toVersionView(AssignmentVersion version) {
    List<SeatView> seats = assignmentMapper.findSeats(version.getId()).stream()
        .map(this::toSeatView).toList();
    return new VersionView(version.getId(), version.getVersionNo(), version.getKind(),
        version.getChangeNote(), version.getCreatedBy(), version.getCreatedAt(), seats);
  }

  private SwapRequestView toSwapView(RoleSwapRequest request) {
    String roleName = null;
    ScriptRole role = roleMapper.findById(request.getRoleId());
    if (role != null) {
      roleName = role.getName();
    }
    Player from = request.getFromPlayerId() == null ? null
        : playerMapper.findById(request.getFromPlayerId());
    Player to = playerMapper.findById(request.getToPlayerId());
    return new SwapRequestView(request.getId(), request.getSessionId(), request.getRoleId(),
        roleName, request.getFromPlayerId(), from != null ? from.getName() : null,
        request.getToPlayerId(), to != null ? to.getName() : "玩家#" + request.getToPlayerId(),
        request.getReason(), request.getStatus(), request.getReviewNote(), request.getCreatedBy(),
        request.getReviewedBy(), request.getCreatedAt(), request.getReviewedAt());
  }

  private ScriptView toScriptView(Script script, List<ScriptRole> roles) {
    return new ScriptView(script.getId(), script.getName(), script.getGenre(),
        script.getDifficulty(), script.getDurationMinutes(), script.getPlayerCount(),
        script.getSummary(), roles.stream().map(this::toRoleView).toList());
  }

  private RoleView toRoleView(ScriptRole role) {
    return new RoleView(role.getId(), role.getScriptId(), role.getName(),
        role.getGenderRequirement(), role.getDescription(), role.getPositionNo());
  }

  private HostView toHostView(Host host) {
    return new HostView(host.getId(), host.getName(), host.getTitle(), host.getPhone());
  }

  private PlayerView toPlayerView(Player player) {
    return new PlayerView(player.getId(), player.getName(), player.getGender(),
        player.getPhone());
  }

  private RosterPlayerView toRosterView(RosterRow row) {
    return new RosterPlayerView(row.getPlayerId(), row.getName(), row.getGender(),
        row.getPhone(), row.getJoinedOrder());
  }

  private SeatView toSeatView(SeatRow row) {
    return new SeatView(row.getRoleId(), row.getRoleName(), row.getRoleGenderRequirement(),
        row.getPositionNo(), row.getPlayerId(), row.getPlayerName(), row.getPlayerGender());
  }

  /** 单条座位视图（换角时复用）。 */
  public Map<Long, String> playerNameLookup(List<Long> playerIds) {
    Map<Long, String> names = new HashMap<>();
    for (Long id : playerIds) {
      if (id == null) {
        continue;
      }
      Player player = playerMapper.findById(id);
      if (player != null) {
        names.put(id, player.getName());
      }
    }
    return names;
  }
}
