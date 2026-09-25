package com.generated.ldmurdergame.domain;

import java.time.LocalDateTime;

/** 换角申请。 */
public class RoleSwapRequest {
  public static final String PENDING = "PENDING";
  public static final String APPROVED = "APPROVED";
  public static final String REJECTED = "REJECTED";

  private Long id;
  private Long sessionId;
  private Long roleId;
  private Long fromPlayerId;
  private Long toPlayerId;
  private String reason;
  private String status;
  private String reviewNote;
  private String createdBy;
  private String reviewedBy;
  private LocalDateTime createdAt;
  private LocalDateTime reviewedAt;

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

  public Long getRoleId() {
    return roleId;
  }

  public void setRoleId(Long roleId) {
    this.roleId = roleId;
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

  public String getReviewNote() {
    return reviewNote;
  }

  public void setReviewNote(String reviewNote) {
    this.reviewNote = reviewNote;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public String getReviewedBy() {
    return reviewedBy;
  }

  public void setReviewedBy(String reviewedBy) {
    this.reviewedBy = reviewedBy;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getReviewedAt() {
    return reviewedAt;
  }

  public void setReviewedAt(LocalDateTime reviewedAt) {
    this.reviewedAt = reviewedAt;
  }
}
