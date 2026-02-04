package com.innowise.authService.exception;

public class LoginFailedException extends RuntimeException {

  public LoginFailedException(String message) {
    super(message);
  }
}
