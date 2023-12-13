package net.zjvis.flint.data.hub.lib.calcite.connector.algorithm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@ToString(callSuper = true)
public class AlgorithmMysql implements AlgorithmConnector {

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

    @Builder
    @Jacksonized
    public AlgorithmMysql(
        String host,
        Integer port,
        String username,
        String password,
        String databaseName
    ) {
        this.host = host;
        this.port = null == port ? 5432 : port;
        this.username = username;
        this.password = password;
        this.databaseName = null == databaseName ? "mysql" : databaseName;
    }

}
