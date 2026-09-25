package com.generated.ldmurdergame.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RosterMapper {
  @Insert("INSERT INTO session_roster (session_id, player_id) VALUES (#{sessionId}, #{playerId})")
  int insert(@Param("sessionId") Long sessionId, @Param("playerId") Long playerId);

  @Delete("DELETE FROM session_roster WHERE session_id = #{sessionId} AND player_id = #{playerId}")
  int delete(@Param("sessionId") Long sessionId, @Param("playerId") Long playerId);

  @Select("SELECT p.id AS player_id, p.name AS name, p.gender AS gender, p.phone AS phone, "
      + "ROW_NUMBER() OVER (ORDER BY sr.joined_at, sr.id) AS joined_order "
      + "FROM session_roster sr JOIN players p ON p.id = sr.player_id "
      + "WHERE sr.session_id = #{sessionId} ORDER BY sr.joined_at, sr.id")
  List<RosterRow> findRoster(Long sessionId);

  @Select("SELECT COUNT(1) FROM session_roster WHERE session_id = #{sessionId}")
  int countRoster(Long sessionId);

  @Select("SELECT COUNT(1) FROM session_roster WHERE session_id = #{sessionId} "
      + "AND player_id = #{playerId}")
  int exists(@Param("sessionId") Long sessionId, @Param("playerId") Long playerId);
}
