package com.generated.ldmurdergame.domain;

/** 剧本角色。 */
public class ScriptRole {
  private Long id;
  private Long scriptId;
  private String name;
  private String genderRequirement;
  private String description;
  private int positionNo;

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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getGenderRequirement() {
    return genderRequirement;
  }

  public void setGenderRequirement(String genderRequirement) {
    this.genderRequirement = genderRequirement;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getPositionNo() {
    return positionNo;
  }

  public void setPositionNo(int positionNo) {
    this.positionNo = positionNo;
  }
}
