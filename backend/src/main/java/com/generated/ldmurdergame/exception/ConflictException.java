package com.generated.ldmurdergame.exception;

import java.util.List;
import com.generated.ldmurdergame.dto.ConflictSessionView;

/** 主持人档期冲突，返回 409 与冲突场次。 */
public class ConflictException extends RuntimeException {
  private final transient List<ConflictSessionView> conflicts;

  public ConflictException(String message, List<ConflictSessionView> conflicts) {
    super(message);
    this.conflicts = conflicts;
  }

  public List<ConflictSessionView> getConflicts() {
    return conflicts;
  }
}
