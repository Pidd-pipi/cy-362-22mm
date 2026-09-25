package com.generated.ldmurdergame.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.generated.ldmurdergame.model.ScriptRole;

@Mapper
public interface ScriptRoleMapper {
  @Select("SELECT id, script_id, name, gender, profile FROM script_roles WHERE script_id = #{scriptId} ORDER BY id")
  List<ScriptRole> findByScriptId(Long scriptId);

  @Select({
    "<script>",
    "SELECT id, script_id, name, gender, profile FROM script_roles WHERE id IN ",
    "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
    "ORDER BY id",
    "</script>"
  })
  List<ScriptRole> findByIds(@Param("ids") List<Long> ids);

  @Select("SELECT id, script_id, name, gender, profile FROM script_roles WHERE id = #{id}")
  ScriptRole findById(Long id);
}
