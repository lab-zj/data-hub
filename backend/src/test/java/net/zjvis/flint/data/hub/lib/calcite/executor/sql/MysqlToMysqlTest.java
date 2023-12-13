package net.zjvis.flint.data.hub.lib.calcite.executor.sql;

import net.zjvis.flint.data.hub.lib.calcite.connector.jdbc.JdbcConnector;
import net.zjvis.flint.data.hub.lib.calcite.connector.jdbc.Mysql;
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

public class MysqlToMysqlTest extends AbstractJdbcToJdbcTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MysqlToMysqlTest.class);
    @Container
    private static GenericContainer<?> mysqlServer;
    private static String host;
    private static int port;
    private static String username;
    private static String password;
    private static String databaseName;

    @BeforeAll
    static void beforeAll() {
        password = RandomStringUtils.randomAlphanumeric(8);
        databaseName = "calcite_test";
        mysqlServer = new GenericContainer<>(DockerImageName.parse("mysql:8.0.34-oracle"))
                .withExposedPorts(3306)
                .withEnv("MYSQL_ROOT_PASSWORD", password)
                .withEnv("MYSQL_DATABASE", databaseName);
        mysqlServer.start();
        host = mysqlServer.getHost();
        port = mysqlServer.getFirstMappedPort();
        username = "root";
        LOGGER.info("mysql server started at {}:{}@{}:{}", username, password, host, port);
    }

    @AfterAll
    static void afterAll() {
        if (null != mysqlServer) {
            mysqlServer.close();
            mysqlServer = null;
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
        return Mysql.builder()
                .identifier("s_mysql")
                .host(host)
                .port(port)
                .username(username)
                .password(password)
                .databaseName(databaseName)
                .build();
    }
}
