package com.generated.ldmurdergame.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.generated.ldmurdergame.model.Stage;

@Mapper
public interface StageMapper {
  @Select("SELECT id, script_id, host_id, start_at, end_at, clear_end_at, status, created_at, confirmed_at FROM stages ORDER BY start_at DESC, id DESC")
  List<Stage> findAll();

  @Select("SELECT id, script_id, host_id, start_at, end_at, clear_end_at, status, created_at, confirmed_at FROM stages WHERE id = #{id}")
  Stage findById(Long id);

  // 档期占用按“脚本时长 + 20 分钟清场期”计算；与主持人已有场次区间重叠即撞档
  @Select("SELECT id, script_id, host_id, start_at, end_at, clear_end_at, status, created_at, confirmed_at "
      + "FROM stages WHERE host_id = #{hostId} AND id <> #{excludeId} "
      + "AND start_at < #{clearEnd} AND clear_end_at > #{start} ORDER BY start_at")
  List<Stage> findHostConflicts(@Param("hostId") Long hostId,
                                @Param("excludeId") Long excludeId,
                                @Param("start") LocalDateTime start,
                                @Param("clearEnd") LocalDateTime clearEnd);

  @Insert("INSERT INTO stages (script_id, host_id, start_at, end_at, clear_end_at, status) "
      + "VALUES (#{scriptId}, #{hostId}, #{startAt}, #{endAt}, #{clearEndAt}, #{status})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insert(Stage stage);

  @Update("UPDATE stages SET status = #{status}, confirmed_at = #{confirmedAt} WHERE id = #{id}")
  int updateStatus(@Param("id") Long id,
                   @Param("status") String status,
                   @Param("confirmedAt") java.time.LocalDateTime confirmedAt);
}
