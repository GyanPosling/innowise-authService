package com.innowise.authService.exception;

public class RefreshTokenRejectedException extends RuntimeException {

  public RefreshTokenRejectedException() {
    super("Refresh token is invalid");
  }
}
