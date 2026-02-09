package com.innowise.authservice.exception;

public class LoginFailedException extends RuntimeException {

  public LoginFailedException(String message) {
    super(message);
  }
}
