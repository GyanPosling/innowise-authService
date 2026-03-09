package com.innowise.authservice.exception;

import java.io.Serial;

public class AuthUserNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -4250942221637388986L;

    public AuthUserNotFoundException() {
        super("Auth user not found");
    }

    public AuthUserNotFoundException(String details) {
        super("Auth user not found by " + details);
    }

    public AuthUserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
