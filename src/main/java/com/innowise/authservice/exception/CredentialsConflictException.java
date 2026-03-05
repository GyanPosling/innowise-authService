package com.innowise.authservice.exception;

import java.io.Serial;

public class CredentialsConflictException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 7241488733902557597L;

  public CredentialsConflictException() {
    super("Credentials already exist");
  }

  public CredentialsConflictException(String message) {
    super(message);
  }

  public CredentialsConflictException(String message, Throwable cause) {
    super(message, cause);
  }

  public CredentialsConflictException(String field, String value) {
    super("Credentials already exist for " + field + ": " + value);
  }
}
