package net.zjvis.flint.data.hub.exception;

public class NotSupportException extends Exception {
    private static final long serialVersionUID = 3680080369686231576L;

    public NotSupportException(String message) {
        super(message);
    }

    public NotSupportException(String message, Throwable cause) {
        super(message, cause);
    }
}
