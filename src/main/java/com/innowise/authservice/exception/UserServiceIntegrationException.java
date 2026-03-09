package com.innowise.authservice.exception;

import java.io.Serial;

public class UserServiceIntegrationException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = -4076405586837272117L;

  public UserServiceIntegrationException() {
    super("User service integration failed");
  }

  public UserServiceIntegrationException(String message) {
    super(message);
  }

  public UserServiceIntegrationException(String message, Throwable cause) {
    super(message, cause);
  }
}
