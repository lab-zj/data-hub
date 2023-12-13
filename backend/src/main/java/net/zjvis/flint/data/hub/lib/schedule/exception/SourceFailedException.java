package net.zjvis.flint.data.hub.lib.schedule.exception;

public class SourceFailedException extends Exception {
    private static final long serialVersionUID = -419661001167248075L;

    public SourceFailedException(String message) {
        super(message);
    }

    public SourceFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
