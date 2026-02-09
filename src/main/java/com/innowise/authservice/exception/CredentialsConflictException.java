package com.innowise.authservice.exception;

public class CredentialsConflictException extends RuntimeException {

  public CredentialsConflictException(String field, String value) {
    super("Credentials already exist for " + field + ": " + value);
  }
}
