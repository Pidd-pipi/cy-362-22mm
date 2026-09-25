package com.generated.ldmurdergame.mapper;

/** 名单玩家投影（join players）。 */
public class RosterRow {
  private Long playerId;
  private String name;
  private String gender;
  private String phone;
  private int joinedOrder;

  public Long getPlayerId() {
    return playerId;
  }

  public void setPlayerId(Long playerId) {
    this.playerId = playerId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public int getJoinedOrder() {
    return joinedOrder;
  }

  public void setJoinedOrder(int joinedOrder) {
    this.joinedOrder = joinedOrder;
  }
}
