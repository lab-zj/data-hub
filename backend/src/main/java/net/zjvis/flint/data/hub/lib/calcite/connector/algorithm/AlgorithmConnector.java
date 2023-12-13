package net.zjvis.flint.data.hub.lib.calcite.connector.algorithm;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(property = "type", use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AlgorithmS3.class, name = "s3"),
        @JsonSubTypes.Type(value = AlgorithmMysql.class, name = "mysql"),
        @JsonSubTypes.Type(value = AlgorithmPostgresql.class, name = "postgresql"),
})
public interface AlgorithmConnector {
}
