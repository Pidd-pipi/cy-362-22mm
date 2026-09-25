package com.generated.ldmurdergame.exception;

import java.util.List;
import com.generated.ldmurdergame.dto.ConflictStage;

/**
 * 主持人档期冲突：新场次（含 20 分钟清场期）与该主持人已有场次时间重叠。
 */
public class ScheduleConflictException extends RuntimeException {
  private final transient List<ConflictStage> conflicts;

  public ScheduleConflictException(String message, List<ConflictStage> conflicts) {
    super(message);
    this.conflicts = conflicts;
  }

  public List<ConflictStage> getConflicts() {
    return conflicts;
  }
}
