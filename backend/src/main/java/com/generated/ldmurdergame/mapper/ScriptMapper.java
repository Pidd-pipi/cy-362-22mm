package com.generated.ldmurdergame.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.generated.ldmurdergame.domain.Script;

@Mapper
public interface ScriptMapper {
  @Select("SELECT id, name, genre, difficulty, duration_minutes, player_count, summary "
      + "FROM scripts ORDER BY id")
  List<Script> findAll();

  @Select("SELECT id, name, genre, difficulty, duration_minutes, player_count, summary "
      + "FROM scripts WHERE id = #{id}")
  Script findById(Long id);
}
