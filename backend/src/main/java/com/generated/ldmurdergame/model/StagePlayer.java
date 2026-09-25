package com.generated.ldmurdergame.model;

import java.time.LocalDateTime;

public class StagePlayer {
  private Long id;
  private Long stageId;
  private String playerName;
  private String gender;
  private Boolean waiting;
  private LocalDateTime createdAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getStageId() {
    return stageId;
  }

  public void setStageId(Long stageId) {
    this.stageId = stageId;
  }

  public String getPlayerName() {
    return playerName;
  }

  public void setPlayerName(String playerName) {
    this.playerName = playerName;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public Boolean getWaiting() {
    return waiting;
  }

  public void setWaiting(Boolean waiting) {
    this.waiting = waiting;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
