package com.innowise.authservice.exception;

public class TokenValidationFailedException extends RuntimeException {

  public TokenValidationFailedException(String message) {
    super(message);
  }
}
