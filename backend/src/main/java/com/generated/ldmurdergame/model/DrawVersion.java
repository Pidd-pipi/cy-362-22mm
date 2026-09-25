package com.generated.ldmurdergame.model;

import java.time.LocalDateTime;

public class DrawVersion {
  private Long id;
  private Long stageId;
  private Integer versionNo;
  private String drawTrigger;
  private String note;
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

  public Integer getVersionNo() {
    return versionNo;
  }

  public void setVersionNo(Integer versionNo) {
    this.versionNo = versionNo;
  }

  public String getDrawTrigger() {
    return drawTrigger;
  }

  public void setDrawTrigger(String drawTrigger) {
    this.drawTrigger = drawTrigger;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
