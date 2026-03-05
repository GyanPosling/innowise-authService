package com.innowise.authservice.exception;

import java.io.Serial;

public class LoginFailedException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = -3921244369728493474L;

  public LoginFailedException() {
    super("Login failed");
  }

  public LoginFailedException(String message) {
    super(message);
  }

  public LoginFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
