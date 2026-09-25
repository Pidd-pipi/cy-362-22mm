package com.generated.ldmurdergame.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.generated.ldmurdergame.domain.RoleSwapRequest;

@Mapper
public interface SwapMapper {
  @Insert("INSERT INTO role_swap_requests (session_id, role_id, from_player_id, to_player_id, "
      + "reason, status, created_by) VALUES (#{sessionId}, #{roleId}, #{fromPlayerId}, "
      + "#{toPlayerId}, #{reason}, #{status}, #{createdBy})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insert(RoleSwapRequest request);

  @Select("SELECT id, session_id, role_id, from_player_id, to_player_id, reason, status, "
      + "review_note, created_by, reviewed_by, created_at, reviewed_at "
      + "FROM role_swap_requests WHERE id = #{id}")
  RoleSwapRequest findById(Long id);

  @Select("SELECT id, session_id, role_id, from_player_id, to_player_id, reason, status, "
      + "review_note, created_by, reviewed_by, created_at, reviewed_at "
      + "FROM role_swap_requests WHERE session_id = #{sessionId} "
      + "ORDER BY created_at DESC, id DESC")
  List<RoleSwapRequest> findBySession(Long sessionId);

  @Select("SELECT COUNT(1) FROM role_swap_requests WHERE session_id = #{sessionId} "
      + "AND role_id = #{roleId} AND status = 'PENDING'")
  int countPendingForRole(@Param("sessionId") Long sessionId, @Param("roleId") Long roleId);

  @Update("UPDATE role_swap_requests SET status = #{status}, review_note = #{reviewNote}, "
      + "reviewed_by = #{reviewedBy}, reviewed_at = #{reviewedAt} WHERE id = #{id}")
  int review(@Param("id") Long id, @Param("status") String status,
      @Param("reviewNote") String reviewNote, @Param("reviewedBy") String reviewedBy,
      @Param("reviewedAt") LocalDateTime reviewedAt);
}
