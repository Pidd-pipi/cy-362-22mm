package com.generated.ldmurdergame.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.generated.ldmurdergame.domain.AssignmentVersion;

@Mapper
public interface AssignmentMapper {
  @Insert("INSERT INTO assignment_versions (session_id, version_no, kind, change_note, created_by) "
      + "VALUES (#{sessionId}, #{versionNo}, #{kind}, #{changeNote}, #{createdBy})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insertVersion(AssignmentVersion version);

  @Insert("INSERT INTO assignment_version_items (version_id, session_id, role_id, player_id) "
      + "VALUES (#{versionId}, #{sessionId}, #{roleId}, #{playerId})")
  int insertItem(@Param("versionId") Long versionId, @Param("sessionId") Long sessionId,
      @Param("roleId") Long roleId, @Param("playerId") Long playerId);

  @Select("SELECT MAX(version_no) FROM assignment_versions WHERE session_id = #{sessionId}")
  Integer maxVersionNo(Long sessionId);

  @Select("SELECT id, session_id, version_no, kind, change_note, created_by, created_at "
      + "FROM assignment_versions WHERE session_id = #{sessionId} "
      + "ORDER BY version_no DESC, id DESC")
  List<AssignmentVersion> findVersions(Long sessionId);

  @Select("SELECT id, session_id, version_no, kind, change_note, created_by, created_at "
      + "FROM assignment_versions WHERE session_id = #{sessionId} "
      + "ORDER BY version_no DESC, id DESC FETCH FIRST 1 ROW ONLY")
  AssignmentVersion findCurrentVersion(Long sessionId);

  /** 某版本的座位（角色 + 玩家），按角色座序。 */
  @Select("SELECT i.version_id AS version_id, r.id AS role_id, r.name AS role_name, "
      + "r.gender_requirement AS role_gender_requirement, r.position_no AS position_no, "
      + "p.id AS player_id, p.name AS player_name, p.gender AS player_gender "
      + "FROM assignment_version_items i "
      + "JOIN script_roles r ON r.id = i.role_id "
      + "LEFT JOIN players p ON p.id = i.player_id "
      + "WHERE i.version_id = #{versionId} ORDER BY r.position_no, r.id")
  List<SeatRow> findSeats(Long versionId);

  /** 当前版本中某角色对应的玩家（换角时使用）。 */
  @Select("SELECT i.player_id FROM assignment_version_items i "
      + "JOIN assignment_versions v ON v.id = i.version_id "
      + "WHERE v.session_id = #{sessionId} AND i.role_id = #{roleId} "
      + "ORDER BY v.version_no DESC FETCH FIRST 1 ROW ONLY")
  Long findCurrentPlayerForRole(@Param("sessionId") Long sessionId,
      @Param("roleId") Long roleId);
}
