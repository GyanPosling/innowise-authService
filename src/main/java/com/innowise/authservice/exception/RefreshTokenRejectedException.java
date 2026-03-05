package com.innowise.authservice.exception;

import java.io.Serial;

public class RefreshTokenRejectedException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 5955428268128301892L;

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
