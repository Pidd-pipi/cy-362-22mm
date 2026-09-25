package com.generated.ldmurdergame.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.generated.ldmurdergame.model.Seat;

@Mapper
public interface SeatMapper {
  @Select("SELECT id, version_id, role_id, player_id FROM seats WHERE version_id = #{versionId} ORDER BY id")
  List<Seat> findByVersionId(Long versionId);

  @Select({
    "<script>",
    "SELECT id, version_id, role_id, player_id FROM seats WHERE version_id IN ",
    "<foreach collection='versionIds' item='vid' open='(' separator=',' close=')'>#{vid}</foreach>",
    "ORDER BY id",
    "</script>"
  })
  List<Seat> findByVersionIds(@Param("versionIds") List<Long> versionIds);

  @Insert({
    "<script>",
    "INSERT INTO seats (version_id, role_id, player_id) VALUES ",
    "<foreach collection='seats' item='seat' separator=','>",
    "(#{seat.versionId}, #{seat.roleId}, #{seat.playerId})",
    "</foreach>",
    "</script>"
  })
  int insertBatch(@Param("seats") List<Seat> seats);

  @Update("UPDATE seats SET player_id = #{playerId} WHERE version_id = #{versionId} AND role_id = #{roleId}")
  int updateSeatPlayer(@Param("versionId") Long versionId,
                       @Param("roleId") Long roleId,
                       @Param("playerId") Long playerId);
}
