package com.generated.ldmurdergame.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 幂等演示数据：剧本/角色、DM、玩家，以及一场用于展示撞档的已排场次。 */
@Component
public class DataSeeder implements CommandLineRunner {
  private final JdbcTemplate jdbc;

  public DataSeeder(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  @Transactional
  public void run(String... args) {
    Integer scriptCount = jdbc.queryForObject("SELECT COUNT(1) FROM scripts", Integer.class);
    if (scriptCount != null && scriptCount > 0) {
      return;
    }

    long script1 = insertScript("年轮", "情感/还原", "进阶", 240, 4,
        "一桩跨越时间的家族谜案，情感与还原并重。");
    insertRole(script1, "母亲", "FEMALE", "温柔坚韧的家族支柱。", 1);
    insertRole(script1, "父亲", "MALE", "沉默寡言、藏着秘密。", 2);
    insertRole(script1, "长子", "ANY", "承担家族期望。", 3);
    insertRole(script1, "幼女", "FEMALE", "敏锐细腻。", 4);

    long script2 = insertScript("雾起金陵", "推理/本格", "困难", 300, 6,
        "民国金陵的连环命案，硬核本格推理。");
    insertRole(script2, "探长", "MALE", "负责查案的警探。", 1);
    insertRole(script2, "法医", "ANY", "冷静专业。", 2);
    insertRole(script2, "歌女", "FEMALE", "风情万种的线人。", 3);
    insertRole(script2, "商人", "MALE", "圆滑世故。", 4);
    insertRole(script2, "记者", "ANY", "嗅觉敏锐。", 5);
    insertRole(script2, "女仆", "FEMALE", "目击关键一幕。", 6);

    long script3 = insertScript("欢乐幼儿园", "欢乐/机制", "新手", 150, 5,
        "轻松欢乐的机制本，适合新手团建。");
    insertRole(script3, "园长", "ANY", "幼儿园园长。", 1);
    insertRole(script3, "体育老师", "MALE", "活力满满。", 2);
    insertRole(script3, "美术老师", "FEMALE", "温柔有创意。", 3);
    insertRole(script3, "保健医", "ANY", "细心负责。", 4);
    insertRole(script3, "大厨", "ANY", "掌勺的开心果。", 5);

    insertHost("林默", "金牌DM", "13800000001");
    insertHost("苏晴", "情感本DM", "13800000002");
    insertHost("阿哲", "硬核本DM", "13800000003");

    String[][] players = {
        {"陈一帆", "MALE", "13900000001"},
        {"王梓萱", "FEMALE", "13900000002"},
        {"李昊然", "MALE", "13900000003"},
        {"赵雨桐", "FEMALE", "13900000004"},
        {"孙嘉树", "MALE", "13900000005"},
        {"周诗涵", "FEMALE", "13900000006"},
        {"吴启明", "MALE", "13900000007"},
        {"郑雅文", "FEMALE", "13900000008"},
        {"冯子墨", "MALE", "13900000009"},
        {"蒋若曦", "FEMALE", "13900000010"}
    };
    for (String[] player : players) {
      insertPlayer(player[0], player[1], player[2]);
    }

    seedExistingSession(script1, 1L, LocalDate.now(), LocalTime.of(19, 0),
        LocalDate.now().atTime(19, 0), LocalDate.now().atTime(23, 20));
  }

  private long insertScript(String name, String genre, String difficulty, int duration,
      int playerCount, String summary) {
    jdbc.update("INSERT INTO scripts (name, genre, difficulty, duration_minutes, player_count, "
        + "summary) VALUES (?, ?, ?, ?, ?, ?)", name, genre, difficulty, duration, playerCount,
        summary);
    return jdbc.queryForObject("SELECT id FROM scripts WHERE name = ?", Long.class, name);
  }

  private void insertRole(long scriptId, String name, String gender, String description,
      int positionNo) {
    jdbc.update("INSERT INTO script_roles (script_id, name, gender_requirement, description, "
        + "position_no) VALUES (?, ?, ?, ?, ?)", scriptId, name, gender, description, positionNo);
  }

  private void insertHost(String name, String title, String phone) {
    jdbc.update("INSERT INTO hosts (name, title, phone) VALUES (?, ?, ?)", name, title, phone);
  }

  private void insertPlayer(String name, String gender, String phone) {
    jdbc.update("INSERT INTO players (name, gender, phone) VALUES (?, ?, ?)", name, gender, phone);
  }

  private void seedExistingSession(long scriptId, long hostId, LocalDate date, LocalTime start,
      LocalDateTime startAt, LocalDateTime endAt) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(1) FROM stage_sessions WHERE host_id = ? AND start_at = ?",
        Integer.class, hostId, startAt);
    if (count != null && count > 0) {
      return;
    }
    jdbc.update("INSERT INTO stage_sessions (script_id, host_id, session_date, start_time, "
        + "end_time, start_at, end_at, cleanup_minutes, status, note) VALUES "
        + "(?, ?, ?, ?, ?, ?, ?, 20, 'CONFIRMED', '种子演示场次（用于撞档提示）')",
        scriptId, hostId, date, start, endAt.toLocalTime(), startAt, endAt);
    Long sessionId = jdbc.queryForObject(
        "SELECT id FROM stage_sessions WHERE host_id = ? AND start_at = ?",
        Long.class, hostId, startAt);
    if (sessionId != null) {
      jdbc.update("UPDATE stage_sessions SET status = 'CONFIRMED', confirmed_at = ? WHERE id = ?",
          LocalDateTime.now(), sessionId);
      List<Long> roleIds = jdbc.queryForList(
          "SELECT id FROM script_roles WHERE script_id = ? ORDER BY position_no", Long.class,
          scriptId);
      List<Long> playerIds = jdbc.queryForList("SELECT id FROM players ORDER BY id", Long.class);
      for (int i = 0; i < roleIds.size() && i < playerIds.size(); i++) {
        jdbc.update("INSERT INTO session_roster (session_id, player_id) VALUES (?, ?)",
            sessionId, playerIds.get(i));
      }
      jdbc.update("INSERT INTO assignment_versions (session_id, version_no, kind, change_note, "
          + "created_by) VALUES (?, 1, 'CONFIRM', '种子定版数据', '系统')", sessionId);
      Long versionId = jdbc.queryForObject(
          "SELECT id FROM assignment_versions WHERE session_id = ? ORDER BY id DESC",
          Long.class, sessionId);
      // 母亲->女玩家2，父亲->男玩家1，长子->男玩家3，幼女->女玩家4
      long[] mapping = {2, 1, 3, 4};
      for (int i = 0; i < roleIds.size(); i++) {
        jdbc.update("INSERT INTO assignment_version_items (version_id, session_id, role_id, "
            + "player_id) VALUES (?, ?, ?, ?)", versionId, sessionId, roleIds.get(i),
            playerIds.get((int) mapping[i] - 1));
      }
    }
  }
}
