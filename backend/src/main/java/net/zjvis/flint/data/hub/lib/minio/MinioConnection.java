package net.zjvis.flint.data.hub.lib.minio;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@Getter
@EqualsAndHashCode
@ToString
public class MinioConnection {
    private final String endpoint;
    private final String accessKey;
    private final String accessSecret;

    @Builder
    @Jacksonized
    public MinioConnection(String endpoint, String accessKey, String accessSecret) {
        this.endpoint = endpoint;
        this.accessKey = accessKey;
        this.accessSecret = accessSecret;
    }
}
