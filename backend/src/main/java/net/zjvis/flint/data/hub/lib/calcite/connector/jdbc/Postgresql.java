package net.zjvis.flint.data.hub.lib.calcite.connector.jdbc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.lib.calcite.datasource.CalciteDataSource;
import net.zjvis.flint.data.hub.lib.calcite.datasource.JdbcCalciteDataSource;
import org.jooq.SQLDialect;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Postgresql extends JdbcConnector {
    @Getter
    @JsonProperty
    private final String identifier;
    @JsonProperty
    private final String host;
    @JsonProperty
    private final int port;
    @JsonProperty
    private final String username;
    @JsonProperty
    private final String password;
    @JsonProperty
    private final String databaseName;
    @JsonProperty
    private final String schemaName;

    @Builder
    @Jacksonized
    public Postgresql(
            String identifier,
            String host,
            Integer port,
            String username,
            String password,
            String databaseName,
            String schemaName
    ) {
        this.identifier = identifier;
        this.host = host;
        this.port = null == port ? 5432 : port;
        this.username = username;
        this.password = password;
        this.schemaName = null == schemaName ? "public" : schemaName;
        this.databaseName = null == databaseName ? "postgres" : databaseName;
    }

    @Override
    protected String schemaName() {
        return schemaName;
    }

    @Override
    public CalciteDataSource asDataSource() {
        return JdbcCalciteDataSource.builder()
                .identifier(identifier)
                .host(host)
                .port(port)
                .databaseName(databaseName)
                .databaseSchema(schemaName)
                .driver(JdbcCalciteDataSource.Driver.Postgresql)
                .username(username)
                .password(password)
                .build();
    }

    @Override
    protected SQLDialect dialect() {
        return SQLDialect.POSTGRES;
    }
}
