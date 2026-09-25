package com.generated.ldmurdergame.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.springframework.stereotype.Component;
import com.generated.ldmurdergame.exception.ApiException;
import com.generated.ldmurdergame.model.ScriptRole;
import com.generated.ldmurdergame.model.StagePlayer;

/**
 * 抽签入座：每个角色只留一位玩家。
 * 先满足男/女专属角色（同性别玩家池中随机抽签），剩余玩家随机填入不限性别的角色。
 */
@Component
public class RoleDrawAssigner {
  private final Random random = new Random();

  public Map<Long, Long> assign(List<ScriptRole> roles, List<StagePlayer> activePlayers) {
    if (activePlayers.size() != roles.size()) {
      throw new ApiException("到场人数与角色数不一致，无法抽签入座");
    }

    int needMale = 0;
    int needFemale = 0;
    for (ScriptRole role : roles) {
      if ("MALE".equals(role.getGender())) {
        needMale++;
      } else if ("FEMALE".equals(role.getGender())) {
        needFemale++;
      }
    }

    List<Long> malePool = new ArrayList<>();
    List<Long> femalePool = new ArrayList<>();
    for (StagePlayer player : activePlayers) {
      if ("MALE".equals(player.getGender())) {
        malePool.add(player.getId());
      } else if ("FEMALE".equals(player.getGender())) {
        femalePool.add(player.getId());
      }
    }

    if (malePool.size() < needMale) {
      throw new ApiException("男玩家不足：男性角色需要 " + needMale + " 人，当前仅 " + malePool.size() + " 人");
    }
    if (femalePool.size() < needFemale) {
      throw new ApiException("女玩家不足：女性角色需要 " + needFemale + " 人，当前仅 " + femalePool.size() + " 人");
    }

    Collections.shuffle(malePool, random);
    Collections.shuffle(femalePool, random);

    // 专属角色抽完后剩余的玩家进入“不限性别”角色池
    List<Long> anyPool = new ArrayList<>();
    anyPool.addAll(malePool.subList(needMale, malePool.size()));
    anyPool.addAll(femalePool.subList(needFemale, femalePool.size()));
    Collections.shuffle(anyPool, random);

    Map<Long, Long> result = new HashMap<>();
    int maleIndex = 0;
    int femaleIndex = 0;
    int anyIndex = 0;
    for (ScriptRole role : roles) {
      switch (role.getGender()) {
        case "MALE" -> result.put(role.getId(), malePool.get(maleIndex++));
        case "FEMALE" -> result.put(role.getId(), femalePool.get(femaleIndex++));
        default -> result.put(role.getId(), anyPool.get(anyIndex++));
      }
    }
    return result;
  }
}
