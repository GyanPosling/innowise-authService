package com.innowise.authservice.exception;

import java.io.Serial;

public class CredentialsConflictException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = -3978034227206313862L;

  public CredentialsConflictException() {
    super("Credentials already exist");
  }

  public CredentialsConflictException(String details) {
    super("Credentials already exist for " + details);
  }

  public CredentialsConflictException(String message, Throwable cause) {
    super(message, cause);
  }
}
