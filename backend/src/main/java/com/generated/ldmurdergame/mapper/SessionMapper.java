package com.generated.ldmurdergame.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.generated.ldmurdergame.domain.StageSession;

@Mapper
public interface SessionMapper {
  @Insert("INSERT INTO stage_sessions (script_id, host_id, session_date, start_time, end_time, "
      + "start_at, end_at, cleanup_minutes, status, note) VALUES (#{scriptId}, #{hostId}, "
      + "#{sessionDate}, #{startTime}, #{endTime}, #{startAt}, #{endAt}, #{cleanupMinutes}, "
      + "#{status}, #{note})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insert(StageSession session);

  @Select("SELECT id, script_id, host_id, session_date, start_time, end_time, start_at, end_at, "
      + "cleanup_minutes, status, confirmed_at, note, created_at FROM stage_sessions "
      + "WHERE id = #{id}")
  StageSession findById(Long id);

  @Select("SELECT id, script_id, host_id, session_date, start_time, end_time, start_at, end_at, "
      + "cleanup_minutes, status, confirmed_at, note, created_at FROM stage_sessions "
      + "ORDER BY start_at DESC, id DESC")
  List<StageSession> findAll();

  /** 同一主持人、时间区间（含清场期）重叠且未取消的场次。 */
  @Select("<script>SELECT ss.id AS session_id, sc.name AS script_name, h.name AS host_name, "
      + "ss.session_date AS session_date, ss.start_time AS start_time, ss.end_time AS end_time, "
      + "ss.status AS status, ss.start_at AS start_at, ss.end_at AS end_at "
      + "FROM stage_sessions ss "
      + "JOIN scripts sc ON sc.id = ss.script_id "
      + "JOIN hosts h ON h.id = ss.host_id "
      + "WHERE ss.host_id = #{hostId} AND ss.status &lt;&gt; 'CANCELLED' "
      + "<if test='excludeId != null'>AND ss.id &lt;&gt; #{excludeId} </if>"
      + "AND ss.start_at &lt; #{endAt} AND ss.end_at &gt; #{startAt} "
      + "ORDER BY ss.start_at</script>")
  List<ConflictRow> findHostConflicts(@Param("hostId") Long hostId,
      @Param("startAt") LocalDateTime startAt,
      @Param("endAt") LocalDateTime endAt,
      @Param("excludeId") Long excludeId);

  @Update("UPDATE stage_sessions SET status = #{status}, confirmed_at = #{confirmedAt} "
      + "WHERE id = #{id}")
  int updateStatus(@Param("id") Long id, @Param("status") String status,
      @Param("confirmedAt") LocalDateTime confirmedAt);
}
