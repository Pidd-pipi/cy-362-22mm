package com.generated.ldmurdergame.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.generated.ldmurdergame.model.StagePlayer;

@Mapper
public interface StagePlayerMapper {
  @Select("SELECT id, stage_id, player_name, gender, waiting, created_at FROM stage_players WHERE stage_id = #{stageId} ORDER BY id")
  List<StagePlayer> findByStageId(Long stageId);

  @Select("SELECT id, stage_id, player_name, gender, waiting, created_at FROM stage_players WHERE id = #{id}")
  StagePlayer findById(Long id);

  @Select("SELECT COUNT(1) FROM stage_players WHERE stage_id = #{stageId} AND waiting = FALSE")
  int countActive(@Param("stageId") Long stageId);

  @Insert("INSERT INTO stage_players (stage_id, player_name, gender, waiting) "
      + "VALUES (#{stageId}, #{playerName}, #{gender}, #{waiting})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insert(StagePlayer player);

  @Update("UPDATE stage_players SET waiting = #{waiting} WHERE id = #{id}")
  int updateWaiting(@Param("id") Long id, @Param("waiting") Boolean waiting);

  @Delete("DELETE FROM stage_players WHERE id = #{id} AND stage_id = #{stageId}")
  int delete(@Param("id") Long id, @Param("stageId") Long stageId);
}
