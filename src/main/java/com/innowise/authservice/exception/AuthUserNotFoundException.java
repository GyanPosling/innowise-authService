package com.innowise.authservice.exception;

import java.io.Serial;

public class AuthUserNotFoundException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 7416843454775376300L;

  public AuthUserNotFoundException() {
    super("Auth user not found");
  }

  public AuthUserNotFoundException(String message) {
    super(message);
  }

  public AuthUserNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public AuthUserNotFoundException(String field, String value) {
    super("Auth user not found by " + field + ": " + value);
  }
}
