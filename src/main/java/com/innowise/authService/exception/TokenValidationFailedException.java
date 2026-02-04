package com.innowise.authService.exception;

public class TokenValidationFailedException extends RuntimeException {

  public TokenValidationFailedException(String message) {
    super(message);
  }
}
