package com.innowise.authService.exception;

public class AuthUserNotFoundException extends RuntimeException {

  public AuthUserNotFoundException(String field, String value) {
    super("Auth user not found by " + field + ": " + value);
  }
}
