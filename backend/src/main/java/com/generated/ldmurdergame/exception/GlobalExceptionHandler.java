package com.generated.ldmurdergame.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Map<String, String>> handleApiException(ApiException exception) {
    return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<Map<String, Object>> handleConflict(ConflictException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
        "message", exception.getMessage(),
        "conflicts", exception.getConflicts()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidation(
      MethodArgumentNotValidException exception) {
    String message = exception.getBindingResult().getFieldErrors().stream()
        .findFirst()
        .map(error -> error.getField() + " " + error.getDefaultMessage())
        .orElse("请求参数不合法");
    return ResponseEntity.badRequest().body(Map.of("message", message));
  }
}
