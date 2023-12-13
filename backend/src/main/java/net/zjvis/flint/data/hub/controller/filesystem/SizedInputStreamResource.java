package net.zjvis.flint.data.hub.controller.filesystem;

import lombok.Builder;
import org.springframework.core.io.InputStreamResource;

import java.io.IOException;
import java.io.InputStream;

public class SizedInputStreamResource extends InputStreamResource {
    private final long objectSize;

    @Builder
    public SizedInputStreamResource(InputStream inputStream, long objectSize) {
        super(inputStream, "stream resource");
        this.objectSize = objectSize;
    }

    @Override
    public long contentLength() throws IOException {
        return objectSize;
    }
}