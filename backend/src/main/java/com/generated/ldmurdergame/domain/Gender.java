package com.generated.ldmurdergame.domain;

/** 角色性别要求 / 玩家性别。 */
public final class Gender {
  public static final String MALE = "MALE";
  public static final String FEMALE = "FEMALE";
  /** 不限性别。 */
  public static final String ANY = "ANY";

  private Gender() {
  }

  /** 玩家是否符合角色的性别要求。 */
  public static boolean matches(String requirement, String playerGender) {
    if (requirement == null || ANY.equals(requirement)) {
      return true;
    }
    return requirement.equals(playerGender);
  }
}
