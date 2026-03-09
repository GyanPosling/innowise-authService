package com.innowise.authservice.exception;

import java.io.Serial;


public class RefreshTokenRejectedException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 832901783975184421L;

  public RefreshTokenRejectedException() {
    super("Refresh token is invalid");
  }

  public RefreshTokenRejectedException(String message) {
    super(message);
  }

  public RefreshTokenRejectedException(String message, Throwable cause) {
    super(message, cause);
  }
}
