package com.generated.ldmurdergame.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.generated.ldmurdergame.model.DrawVersion;

@Mapper
public interface DrawVersionMapper {
  @Select("SELECT id, stage_id, version_no, draw_trigger, note, created_at FROM draw_versions WHERE stage_id = #{stageId} ORDER BY version_no DESC, id DESC")
  List<DrawVersion> findByStageId(Long stageId);

  @Select("SELECT id, stage_id, version_no, draw_trigger, note, created_at FROM draw_versions WHERE id = #{id}")
  DrawVersion findById(Long id);

  @Select("SELECT COALESCE(MAX(version_no), 0) FROM draw_versions WHERE stage_id = #{stageId}")
  int maxVersionNo(@Param("stageId") Long stageId);

  @Insert("INSERT INTO draw_versions (stage_id, version_no, draw_trigger, note) "
      + "VALUES (#{stageId}, #{versionNo}, #{drawTrigger}, #{note})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insert(DrawVersion version);
}
