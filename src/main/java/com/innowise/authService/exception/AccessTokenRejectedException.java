package com.innowise.authService.exception;

public class AccessTokenRejectedException extends RuntimeException {

  public AccessTokenRejectedException() {
    super("Access token is invalid");
  }
}
