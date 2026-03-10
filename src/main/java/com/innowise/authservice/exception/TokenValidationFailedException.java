package com.innowise.authservice.exception;

import java.io.Serial;

public class TokenValidationFailedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 3837204445814445470L;

    public TokenValidationFailedException() {
        super("Token validation failed");
    }

    public TokenValidationFailedException(String message) {
        super(message);
    }

    public TokenValidationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
