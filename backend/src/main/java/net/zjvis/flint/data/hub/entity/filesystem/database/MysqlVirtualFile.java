package net.zjvis.flint.data.hub.entity.filesystem.database;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.controller.filesystem.vo.VirtualFileVO;
import net.zjvis.flint.data.hub.lib.calcite.connector.CalciteConnector;
import net.zjvis.flint.data.hub.lib.calcite.connector.jdbc.Mysql;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.LinkOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

@Getter
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class MysqlVirtualFile implements VirtualFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(MysqlVirtualFile.class);
    private final String host;
    private final String port;
    private final String username;
    private final String password;
    private final String databaseName;

    @Jacksonized
    @Builder
    public MysqlVirtualFile(String host, String port, String username, String password, String databaseName) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.databaseName = databaseName;
    }

    @Override
    public Pair<Boolean, String> connectionTest() {
        try {
            Class.forName(com.mysql.jdbc.Driver.class.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try (Connection connection = DriverManager.getConnection(
                String.format("jdbc:mysql://%s:%s/%s", host, port, databaseName),
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
        return Mysql.builder()
                .identifier(identifier)
                .host(this.getHost())
                .port(Integer.valueOf(this.getPort()))
                .username(this.getUsername())
                .password(this.getPassword())
                .databaseName(this.getDatabaseName())
                .build();
    }

    @Override
    public Object previewData(String type) {

        throw new RuntimeException("not implement yet.");
    }

}
