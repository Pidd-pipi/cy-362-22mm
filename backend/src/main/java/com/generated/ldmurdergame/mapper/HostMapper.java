package com.generated.ldmurdergame.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.generated.ldmurdergame.domain.Host;

@Mapper
public interface HostMapper {
  @Select("SELECT id, name, title, phone FROM hosts ORDER BY id")
  List<Host> findAll();

  @Select("SELECT id, name, title, phone FROM hosts WHERE id = #{id}")
  Host findById(Long id);
}
