package net.zjvis.flint.data.hub.entity.filesystem.database;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.controller.filesystem.vo.VirtualFileVO;
import net.zjvis.flint.data.hub.lib.calcite.connector.CalciteConnector;
import net.zjvis.flint.data.hub.lib.calcite.connector.jdbc.Postgresql;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

@Getter
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostgresqlVirtualFile implements VirtualFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresqlVirtualFile.class);
    private final String host;
    private final String port;
    private final String username;
    private final String password;
    private final String databaseName;
    private final String schemaName;

    @Jacksonized
    @Builder
    public PostgresqlVirtualFile(
            String host, String port,
            String username,
            String password,
            String databaseName,
            String schemaName) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.databaseName = databaseName;
        this.schemaName = schemaName;
    }

    @Override
    public Pair<Boolean, String> connectionTest() {
        try {
            Class.forName(com.mysql.jdbc.Driver.class.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try (Connection connection = DriverManager.getConnection(
                String.format("jdbc:postgresql://%s:%s/%s?currentSchema=%s", host, port, databaseName, schemaName),
                username,
                password
        )) {
            boolean success = connection.isValid(Long.valueOf(TimeUnit.SECONDS.toSeconds(5)).intValue());
            return Pair.of(success, null);
        } catch (SQLException e) {
            LOGGER.warn(String.format("connection test of virtualFile(%s) failed: %s", this, e.getMessage()), e);
            return Pair.of(false, e.getMessage());
        }
    }

    @Override
    public CalciteConnector toConnector(String identifier) {
        return Postgresql.builder()
                .identifier(identifier)
                .host(this.getHost())
                .port(Integer.valueOf(this.getPort()))
                .username(this.getUsername())
                .password(this.getPassword())
                .databaseName(this.getDatabaseName())
                .schemaName(this.getSchemaName())
                .build();
    }

    @Override
    public Object previewData(String type) {
        throw new RuntimeException("not implement yet.");
    }

}
