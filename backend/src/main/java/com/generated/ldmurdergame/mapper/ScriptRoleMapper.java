package com.generated.ldmurdergame.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.generated.ldmurdergame.domain.ScriptRole;

@Mapper
public interface ScriptRoleMapper {
  @Select("SELECT id, script_id, name, gender_requirement, description, position_no "
      + "FROM script_roles WHERE script_id = #{scriptId} ORDER BY position_no, id")
  List<ScriptRole> findByScript(Long scriptId);

  @Select("SELECT id, script_id, name, gender_requirement, description, position_no "
      + "FROM script_roles WHERE id = #{id}")
  ScriptRole findById(Long id);

  @Select("<script>SELECT id, script_id, name, gender_requirement, description, position_no "
      + "FROM script_roles WHERE id IN "
      + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> "
      + "ORDER BY position_no, id</script>")
  List<ScriptRole> findByIds(@Param("ids") List<Long> ids);
}
