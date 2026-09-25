package com.generated.ldmurdergame.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Map<String, String>> handleApiException(ApiException exception) {
    return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
  }

  @ExceptionHandler(ScheduleConflictException.class)
  public ResponseEntity<Map<String, Object>> handleConflict(ScheduleConflictException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Map.of("message", exception.getMessage(), "conflicts", exception.getConflicts()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException exception) {
    FieldError fieldError = exception.getBindingResult().getFieldError();
    String message = fieldError != null ? fieldError.getDefaultMessage() : "请求参数校验未通过";
    return ResponseEntity.badRequest().body(Map.of("message", message));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleUnexpected(Exception exception) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("message", "服务暂不可用：" + exception.getMessage()));
  }
}
