package com.generated.ldmurdergame.model;

import java.time.LocalDateTime;

public class RoleChange {
  private Long id;
  private Long stageId;
  private Long versionId;
  private String changeType;
  private Long fromPlayerId;
  private Long toPlayerId;
  private Long roleId;
  private Long waitingPlayerId;
  private String reason;
  private String status;
  private String managerNote;
  private LocalDateTime createdAt;
  private LocalDateTime decidedAt;

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

  public Long getVersionId() {
    return versionId;
  }

  public void setVersionId(Long versionId) {
    this.versionId = versionId;
  }

  public String getChangeType() {
    return changeType;
  }

  public void setChangeType(String changeType) {
    this.changeType = changeType;
  }

  public Long getFromPlayerId() {
    return fromPlayerId;
  }

  public void setFromPlayerId(Long fromPlayerId) {
    this.fromPlayerId = fromPlayerId;
  }

  public Long getToPlayerId() {
    return toPlayerId;
  }

  public void setToPlayerId(Long toPlayerId) {
    this.toPlayerId = toPlayerId;
  }

  public Long getRoleId() {
    return roleId;
  }

  public void setRoleId(Long roleId) {
    this.roleId = roleId;
  }

  public Long getWaitingPlayerId() {
    return waitingPlayerId;
  }

  public void setWaitingPlayerId(Long waitingPlayerId) {
    this.waitingPlayerId = waitingPlayerId;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getManagerNote() {
    return managerNote;
  }

  public void setManagerNote(String managerNote) {
    this.managerNote = managerNote;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getDecidedAt() {
    return decidedAt;
  }

  public void setDecidedAt(LocalDateTime decidedAt) {
    this.decidedAt = decidedAt;
  }
}
