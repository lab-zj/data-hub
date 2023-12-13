package net.zjvis.flint.data.hub.lib.calcite.executor.sql;

import net.zjvis.flint.data.hub.lib.calcite.connector.jdbc.JdbcConnector;
import net.zjvis.flint.data.hub.lib.calcite.connector.jdbc.Postgresql;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;

public class PostgresqlToPostgresqlTest extends AbstractJdbcToJdbcTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresqlToPostgresqlTest.class);
    @Container
    private static GenericContainer<?> postgresqlServer;
    private static String host;
    private static int port;
    private static String username;
    private static String password;
    private static String databaseName;
    private static String schemaName;

    @BeforeAll
    static void beforeAll() {
        password = RandomStringUtils.randomAlphanumeric(8);
        postgresqlServer = new GenericContainer<>(DockerImageName.parse("postgres:15.2-alpine3.17"))
                .withExposedPorts(5432)
                .withEnv("POSTGRES_PASSWORD", password);
        postgresqlServer.start();
        host = postgresqlServer.getHost();
        port = postgresqlServer.getFirstMappedPort();
        username = "postgres";
        databaseName = "postgres";
        schemaName = "public";
        LOGGER.info("postgresql server started at {}:{}@{}:{}", username, password, host, port);
    }

    @AfterAll
    static void afterAll() {
        if (null != postgresqlServer) {
            postgresqlServer.close();
            postgresqlServer = null;
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        super.setUp();
    }

    @Test
    void testSqlExecutor() throws IOException {
        super.testSqlExecute();
    }

    @Override
    protected JdbcConnector constructJdbcConnector() {
        return Postgresql.builder()
                .identifier("s_postgresql")
                .host(host)
                .port(port)
                .username(username)
                .password(password)
                .databaseName(databaseName)
                .schemaName(schemaName)
                .build();
    }
}
