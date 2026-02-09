package com.innowise.authservice.exception;

public class RefreshTokenRejectedException extends RuntimeException {

  public RefreshTokenRejectedException() {
    super("Refresh token is invalid");
  }
}
