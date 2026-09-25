package com.generated.ldmurdergame.domain;

/** 剧本。 */
public class Script {
  private Long id;
  private String name;
  private String genre;
  private String difficulty;
  private int durationMinutes;
  private int playerCount;
  private String summary;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getGenre() {
    return genre;
  }

  public void setGenre(String genre) {
    this.genre = genre;
  }

  public String getDifficulty() {
    return difficulty;
  }

  public void setDifficulty(String difficulty) {
    this.difficulty = difficulty;
  }

  public int getDurationMinutes() {
    return durationMinutes;
  }

  public void setDurationMinutes(int durationMinutes) {
    this.durationMinutes = durationMinutes;
  }

  public int getPlayerCount() {
    return playerCount;
  }

  public void setPlayerCount(int playerCount) {
    this.playerCount = playerCount;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }
}
