package net.zjvis.flint.data.hub.lib.calcite.schema.s3;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.lib.minio.MinioConnection;

@Getter
@EqualsAndHashCode
@ToString
public class S3ObjectDescriptor {
    private final MinioConnection minioConnection;
    private final String bucket;
    private final String objectKey;

    @Builder
    @Jacksonized
    public S3ObjectDescriptor(MinioConnection minioConnection, String bucket, String objectKey) {
        this.minioConnection = minioConnection;
        this.bucket = bucket;
        this.objectKey = objectKey;
    }
}
