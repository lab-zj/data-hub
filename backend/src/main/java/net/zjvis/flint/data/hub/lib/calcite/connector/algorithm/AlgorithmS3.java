package net.zjvis.flint.data.hub.lib.calcite.connector.algorithm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@ToString(callSuper = true)
public class AlgorithmS3 implements AlgorithmConnector {

    @JsonProperty
    private final String bucket;
    @JsonProperty
    private final String endpoint;
    @JsonProperty("access_key")
    private final String accessKey;
    @JsonProperty("access_secret")
    private final String accessSecret;


    @Builder
    @Jacksonized
    public AlgorithmS3(
        String bucket,
        String endpoint,
        String accessKey,
        String accessSecret
    ) {
        this.bucket = bucket;
        this.endpoint = endpoint;
        this.accessKey = accessKey;
        this.accessSecret = accessSecret;
    }

}
