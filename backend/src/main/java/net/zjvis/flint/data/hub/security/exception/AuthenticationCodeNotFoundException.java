package net.zjvis.flint.data.hub.security.exception;

import org.springframework.security.core.AuthenticationException;

public class AuthenticationCodeNotFoundException extends AuthenticationException {
    public AuthenticationCodeNotFoundException(String msg) {
        super(msg);
    }

    public AuthenticationCodeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
