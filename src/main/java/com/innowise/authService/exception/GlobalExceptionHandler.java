package com.innowise.authService.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      errors.put(error.getField(), error.getDefaultMessage());
    }
    return ResponseEntity.badRequest()
        .body(ValidationErrorResponse.builder().errors(errors).build());
  }

  @ExceptionHandler({
      CredentialsConflictException.class
  })
  public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ErrorResponse.builder().message(ex.getMessage()).build());
  }

  @ExceptionHandler({
      AuthUserNotFoundException.class
  })
  public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse.builder().message(ex.getMessage()).build());
  }

  @ExceptionHandler({
      LoginFailedException.class,
      RefreshTokenRejectedException.class,
      AccessTokenRejectedException.class,
      BadCredentialsException.class,
      AccessDeniedException.class
  })
  public ResponseEntity<ErrorResponse> handleUnauthorized(RuntimeException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ErrorResponse.builder().message(ex.getMessage()).build());
  }

  @ExceptionHandler(TokenValidationFailedException.class)
  public ResponseEntity<ErrorResponse> handleValidation(RuntimeException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.builder().message(ex.getMessage()).build());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.builder().message("Unexpected error").build());
  }
}
