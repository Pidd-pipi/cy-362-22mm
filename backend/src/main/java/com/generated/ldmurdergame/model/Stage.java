package com.generated.ldmurdergame.model;

import java.time.LocalDateTime;

public class Stage {
  private Long id;
  private Long scriptId;
  private Long hostId;
  private LocalDateTime startAt;
  private LocalDateTime endAt;
  private LocalDateTime clearEndAt;
  private String status;
  private LocalDateTime createdAt;
  private LocalDateTime confirmedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getScriptId() {
    return scriptId;
  }

  public void setScriptId(Long scriptId) {
    this.scriptId = scriptId;
  }

  public Long getHostId() {
    return hostId;
  }

  public void setHostId(Long hostId) {
    this.hostId = hostId;
  }

  public LocalDateTime getStartAt() {
    return startAt;
  }

  public void setStartAt(LocalDateTime startAt) {
    this.startAt = startAt;
  }

  public LocalDateTime getEndAt() {
    return endAt;
  }

  public void setEndAt(LocalDateTime endAt) {
    this.endAt = endAt;
  }

  public LocalDateTime getClearEndAt() {
    return clearEndAt;
  }

  public void setClearEndAt(LocalDateTime clearEndAt) {
    this.clearEndAt = clearEndAt;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getConfirmedAt() {
    return confirmedAt;
  }

  public void setConfirmedAt(LocalDateTime confirmedAt) {
    this.confirmedAt = confirmedAt;
  }
}
