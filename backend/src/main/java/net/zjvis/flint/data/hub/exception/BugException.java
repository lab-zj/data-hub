package net.zjvis.flint.data.hub.exception;

public class BugException extends RuntimeException {
    private static final long serialVersionUID = 267820398117258766L;

    public BugException(String message) {
        super(message);
    }

    public BugException(String message, Throwable cause) {
        super(message, cause);
    }
}
