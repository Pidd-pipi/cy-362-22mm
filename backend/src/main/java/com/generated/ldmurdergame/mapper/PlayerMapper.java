package com.generated.ldmurdergame.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.generated.ldmurdergame.domain.Player;

@Mapper
public interface PlayerMapper {
  @Select("SELECT id, name, gender, phone FROM players ORDER BY id")
  List<Player> findAll();

  @Select("SELECT id, name, gender, phone FROM players WHERE id = #{id}")
  Player findById(Long id);
}
