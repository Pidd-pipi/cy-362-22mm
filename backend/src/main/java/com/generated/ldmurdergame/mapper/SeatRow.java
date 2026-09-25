package com.generated.ldmurdergame.mapper;

/** 角色座位投影（角色 + 当前玩家，含跨版本查询）。 */
public class SeatRow {
  private Long versionId;
  private Long roleId;
  private String roleName;
  private String roleGenderRequirement;
  private int positionNo;
  private Long playerId;
  private String playerName;
  private String playerGender;

  public Long getVersionId() {
    return versionId;
  }

  public void setVersionId(Long versionId) {
    this.versionId = versionId;
  }

  public Long getRoleId() {
    return roleId;
  }

  public void setRoleId(Long roleId) {
    this.roleId = roleId;
  }

  public String getRoleName() {
    return roleName;
  }

  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  public String getRoleGenderRequirement() {
    return roleGenderRequirement;
  }

  public void setRoleGenderRequirement(String roleGenderRequirement) {
    this.roleGenderRequirement = roleGenderRequirement;
  }

  public int getPositionNo() {
    return positionNo;
  }

  public void setPositionNo(int positionNo) {
    this.positionNo = positionNo;
  }

  public Long getPlayerId() {
    return playerId;
  }

  public void setPlayerId(Long playerId) {
    this.playerId = playerId;
  }

  public String getPlayerName() {
    return playerName;
  }

  public void setPlayerName(String playerName) {
    this.playerName = playerName;
  }

  public String getPlayerGender() {
    return playerGender;
  }

  public void setPlayerGender(String playerGender) {
    this.playerGender = playerGender;
  }
}
