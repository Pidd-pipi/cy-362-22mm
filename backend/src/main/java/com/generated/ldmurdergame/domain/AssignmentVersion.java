package com.generated.ldmurdergame.domain;

import java.time.LocalDateTime;

/** 角色分座版本（不可变快照）。 */
public class AssignmentVersion {
  public static final String DRAW = "DRAW";
  public static final String CONFIRM = "CONFIRM";
  public static final String MANUAL = "MANUAL";

  private Long id;
  private Long sessionId;
  private int versionNo;
  private String kind;
  private String changeNote;
  private String createdBy;
  private LocalDateTime createdAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getSessionId() {
    return sessionId;
  }

  public void setSessionId(Long sessionId) {
    this.sessionId = sessionId;
  }

  public int getVersionNo() {
    return versionNo;
  }

  public void setVersionNo(int versionNo) {
    this.versionNo = versionNo;
  }

  public String getKind() {
    return kind;
  }

  public void setKind(String kind) {
    this.kind = kind;
  }

  public String getChangeNote() {
    return changeNote;
  }

  public void setChangeNote(String changeNote) {
    this.changeNote = changeNote;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
