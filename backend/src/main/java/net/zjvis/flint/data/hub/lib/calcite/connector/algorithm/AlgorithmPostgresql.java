package net.zjvis.flint.data.hub.lib.calcite.connector.algorithm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@ToString(callSuper = true)
public class AlgorithmPostgresql implements AlgorithmConnector {

    @JsonProperty
    private final String host;
    @JsonProperty
    private final int port;
    @JsonProperty
    private final String username;
    @JsonProperty
    private final String password;
    @JsonProperty("database_name")
    private final String databaseName;
    @JsonProperty("schema_name")
    private final String schemaName;
    @JsonProperty("if_write_table_exists")
    private final String ifWriteTableExists;

    @Builder
    @Jacksonized
    public AlgorithmPostgresql(
        String host,
        Integer port,
        String username,
        String password,
        String databaseName,
        String schemaName,
        String ifWriteTableExists
    ) {
        this.host = host;
        this.port = null == port ? 5432 : port;
        this.username = username;
        this.password = password;
        this.schemaName = null == schemaName ? "public" : schemaName;
        this.databaseName = null == databaseName ? "postgres" : databaseName;
        this.ifWriteTableExists = null ==  ifWriteTableExists ? "replace" : ifWriteTableExists;
    }

}
