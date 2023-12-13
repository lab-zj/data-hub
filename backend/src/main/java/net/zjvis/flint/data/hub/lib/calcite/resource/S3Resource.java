package net.zjvis.flint.data.hub.lib.calcite.resource;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.zjvis.flint.data.hub.lib.minio.MinioConnection;
import net.zjvis.flint.data.hub.lib.minio.MinioException;
import net.zjvis.flint.data.hub.lib.minio.MinioManager;
import net.zjvis.flint.data.hub.lib.minio.MinioTagKey;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Map;

@Getter
@EqualsAndHashCode
public class S3Resource implements StreamResource {
    private final MinioConnection minioConnection;
    private final String bucket;
    private final String objectKey;
    private final CachedOutputStream cachedOutputStream;
    private transient MinioManager minioManager;

    @Builder
    public S3Resource(
            MinioConnection minioConnection,
            String bucket,
            String objectKey
    ) {
        this.minioConnection = minioConnection;
        this.bucket = bucket;
        this.objectKey = objectKey;
        try {
            cachedOutputStream = CachedOutputStream.builder()
                    .closeCallback(inputStream -> {
                        try {
                            minioManager().objectUpload(inputStream, bucket, objectKey, Map.of(
                                    MinioTagKey.CREATE_TIME.getKey(), String.valueOf(System.currentTimeMillis())
                            ));
                        } catch (MinioException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .build();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return minioManager().objectGet(bucket, objectKey);
    }

    @Override
    public OutputStream openOutputStream(boolean append) throws IOException {
        return cachedOutputStream;
    }

    @Override
    public String resourceLocator() {
        return String.format("%s/%s", bucket, objectKey);
    }

    private MinioManager minioManager() {
        if (null == minioManager) {
            minioManager = MinioManager.builder()
                    .minioConnection(minioConnection)
                    .build();
        }
        return minioManager;
    }
}
