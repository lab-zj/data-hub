package net.zjvis.flint.data.hub.lib.minio;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@EqualsAndHashCode
@ToString
public class MinioFile {

    @Getter
    private final String objectKey;
    @Getter
    private final boolean isDirectory;
    @Getter
    private final long size;

    @Builder(toBuilder = true)
    @Jacksonized
    public MinioFile(
            String objectKey,
            boolean isDirectory,
            long size
    ) {
        this.objectKey = objectKey;
        this.isDirectory = isDirectory;
        this.size = size;
    }
}
