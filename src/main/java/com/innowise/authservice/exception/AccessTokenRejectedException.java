package com.innowise.authservice.exception;

import java.io.Serial;

public class AccessTokenRejectedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -7236934854028406515L;

    public AccessTokenRejectedException() {
        super("Access token is invalid");
    }

    public AccessTokenRejectedException(String message) {
        super(message);
    }

    public AccessTokenRejectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
