package com.generated.ldmurdergame.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** 开演场次（开演台）。 */
public class StageSession {
  public static final String STAGING = "STAGING";
  public static final String CONFIRMED = "CONFIRMED";
  public static final String CANCELLED = "CANCELLED";

  private Long id;
  private Long scriptId;
  private Long hostId;
  private LocalDate sessionDate;
  private LocalTime startTime;
  private LocalTime endTime;
  private LocalDateTime startAt;
  private LocalDateTime endAt;
  private int cleanupMinutes;
  private String status;
  private LocalDateTime confirmedAt;
  private String note;
  private LocalDateTime createdAt;

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

  public LocalDate getSessionDate() {
    return sessionDate;
  }

  public void setSessionDate(LocalDate sessionDate) {
    this.sessionDate = sessionDate;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public void setStartTime(LocalTime startTime) {
    this.startTime = startTime;
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public void setEndTime(LocalTime endTime) {
    this.endTime = endTime;
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

  public int getCleanupMinutes() {
    return cleanupMinutes;
  }

  public void setCleanupMinutes(int cleanupMinutes) {
    this.cleanupMinutes = cleanupMinutes;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDateTime getConfirmedAt() {
    return confirmedAt;
  }

  public void setConfirmedAt(LocalDateTime confirmedAt) {
    this.confirmedAt = confirmedAt;
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
