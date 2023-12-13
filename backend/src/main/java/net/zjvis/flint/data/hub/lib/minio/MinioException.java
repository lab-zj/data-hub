package net.zjvis.flint.data.hub.lib.minio;

import java.io.IOException;

public class MinioException extends IOException {
    public MinioException(String message) {
        super(message);
    }

    public MinioException(String message, Throwable cause) {
        super(message, cause);
    }

    public MinioException(Throwable cause) {
        super(cause);
    }
}
