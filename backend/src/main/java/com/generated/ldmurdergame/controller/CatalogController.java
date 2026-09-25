package com.generated.ldmurdergame.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.generated.ldmurdergame.dto.HostView;
import com.generated.ldmurdergame.dto.RoleView;
import com.generated.ldmurdergame.mapper.HostMapper;
import com.generated.ldmurdergame.mapper.ScriptMapper;
import com.generated.ldmurdergame.mapper.ScriptRoleMapper;
import com.generated.ldmurdergame.model.Host;
import com.generated.ldmurdergame.model.Script;
import com.generated.ldmurdergame.model.ScriptRole;

@RestController
public class CatalogController {
  private final ScriptMapper scriptMapper;
  private final ScriptRoleMapper roleMapper;
  private final HostMapper hostMapper;

  public CatalogController(ScriptMapper scriptMapper, ScriptRoleMapper roleMapper, HostMapper hostMapper) {
    this.scriptMapper = scriptMapper;
    this.roleMapper = roleMapper;
    this.hostMapper = hostMapper;
  }

  @GetMapping({"/scripts", "/api/scripts"})
  public List<Script> scripts() {
    return scriptMapper.findAll();
  }

  @GetMapping({"/scripts/{id}/roles", "/api/scripts/{id}/roles"})
  public List<RoleView> roles(@PathVariable Long id) {
    return roleMapper.findByScriptId(id).stream()
        .map((ScriptRole role) -> new RoleView(role.getId(), role.getName(), role.getGender(), role.getProfile()))
        .toList();
  }

  @GetMapping({"/hosts", "/api/hosts"})
  public List<HostView> hosts() {
    return hostMapper.findAll().stream()
        .map((Host host) -> new HostView(host.getId(), host.getName(), host.getTitle(), host.getPhone()))
        .toList();
  }
}
