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
public class Mysql extends JdbcConnector {
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

    @Builder
    @Jacksonized
    public Mysql(
            String identifier,
            String host,
            Integer port,
            String username,
            String password,
            String databaseName
    ) {
        this.identifier = identifier;
        this.host = host;
        this.port = null == port ? 5432 : port;
        this.username = username;
        this.password = password;
        this.databaseName = null == databaseName ? "mysql" : databaseName;
    }

    @Override
    public CalciteDataSource asDataSource() {
        return JdbcCalciteDataSource.builder()
                .identifier(identifier)
                .host(host)
                .port(port)
                .databaseName(databaseName)
                .driver(JdbcCalciteDataSource.Driver.Mysql)
                .username(username)
                .password(password)
                .build();
    }

    @Override
    protected SQLDialect dialect() {
        return SQLDialect.MYSQL;
    }
}
