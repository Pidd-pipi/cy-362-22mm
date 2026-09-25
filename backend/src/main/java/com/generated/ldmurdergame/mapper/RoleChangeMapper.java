package com.generated.ldmurdergame.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.generated.ldmurdergame.model.RoleChange;

@Mapper
public interface RoleChangeMapper {
  @Select("SELECT id, stage_id, version_id, change_type, from_player_id, to_player_id, role_id, waiting_player_id, "
      + "reason, status, manager_note, created_at, decided_at FROM role_changes WHERE stage_id = #{stageId} ORDER BY id DESC")
  List<RoleChange> findByStageId(Long stageId);

  @Select("SELECT id, stage_id, version_id, change_type, from_player_id, to_player_id, role_id, waiting_player_id, "
      + "reason, status, manager_note, created_at, decided_at FROM role_changes WHERE id = #{id}")
  RoleChange findById(Long id);

  @Insert("INSERT INTO role_changes (stage_id, version_id, change_type, from_player_id, to_player_id, role_id, "
      + "waiting_player_id, reason, status) VALUES (#{stageId}, #{versionId}, #{changeType}, #{fromPlayerId}, "
      + "#{toPlayerId}, #{roleId}, #{waitingPlayerId}, #{reason}, #{status})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insert(RoleChange change);

  @Update("UPDATE role_changes SET status = #{status}, manager_note = #{managerNote}, decided_at = #{decidedAt} WHERE id = #{id}")
  int decide(@Param("id") Long id,
             @Param("status") String status,
             @Param("managerNote") String managerNote,
             @Param("decidedAt") LocalDateTime decidedAt);

  @Update("UPDATE role_changes SET version_id = #{versionId} WHERE id = #{id}")
  int updateVersion(@Param("id") Long id, @Param("versionId") Long versionId);
}
