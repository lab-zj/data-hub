package net.zjvis.flint.data.hub.lib.calcite;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.zjvis.flint.data.hub.lib.calcite.datasource.S3CalciteDataSource;
import net.zjvis.flint.data.hub.lib.calcite.func.StringLength;
import net.zjvis.flint.data.hub.lib.calcite.schema.s3.S3ObjectDescriptor;
import net.zjvis.flint.data.hub.lib.data.Record;
import net.zjvis.flint.data.hub.lib.data.Schema;
import net.zjvis.flint.data.hub.lib.data.Table;
import net.zjvis.flint.data.hub.lib.data.Type;
import net.zjvis.flint.data.hub.lib.data.sql.TableExtractor;
import net.zjvis.flint.data.hub.lib.minio.MinioConnection;
import net.zjvis.flint.data.hub.lib.minio.MinioException;
import net.zjvis.flint.data.hub.lib.minio.MinioManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

public class CalciteManagerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CalciteManagerTest.class);
    private static final String BUCKET_NAME = CalciteManagerTest.class.getSimpleName().toLowerCase();
    private static final String DATA_OBJECT_KEY = "data.csv";
    private static final String SCHEMA_NAME = "s_s3";
    private static final String DATA_TABLE_NAME = "t_data";
    @Container
    private static GenericContainer<?> minioServer;
    private static MinioConnection minioConnection;
    private static Table dataTable;

    @BeforeAll
    static void beforeAll() throws MinioException {
        int consolePort = 9001;
        minioServer = new GenericContainer<>(DockerImageName.parse("minio/minio:RELEASE.2022-09-07T22-25-02Z"))
                .withExposedPorts(9000, consolePort)
                .withCommand("server", "/csvFormatData", "--console-address", String.format(":%s", consolePort));
        minioServer.start();
        String host = minioServer.getHost();
        LOGGER.info("minio web interface: http://{}:{}", host, minioServer.getMappedPort(consolePort));
        minioConnection = MinioConnection.builder()
                .endpoint(String.format("http://%s:%s", host, minioServer.getFirstMappedPort()))
                .accessKey("minioadmin")
                .accessSecret("minioadmin")
                .build();
        MinioManager minioManager = MinioManager.builder()
                .minioConnection(minioConnection)
                .build();
        minioManager.bucketRemove(BUCKET_NAME, true);
        minioManager.bucketCreate(BUCKET_NAME, false);
        dataTable = Table.builder()
                .schema(Schema.builder()
                        .name("year")
                        .type(Type.LONG)
                        .name("manufacturer")
                        .type(Type.STRING)
                        .name("model")
                        .type(Type.STRING)
                        .name("description")
                        .type(Type.STRING)
                        .name("price")
                        .type(Type.DOUBLE)
                        .build())
                .record(Record.builder()
                        .value(1996)
                        .value("Jeep")
                        .value("Grand Cherokee")
                        .value("some description")
                        .value(4799.00)
                        .build())
                .build();
        String csvFormatData = csvFormatData(dataTable);
        minioManager.objectUpload(
                new ByteArrayInputStream(csvFormatData.getBytes(StandardCharsets.UTF_8)),
                BUCKET_NAME,
                DATA_OBJECT_KEY);
    }

    @AfterAll
    static void afterAll() {
        if (null != minioServer) {
            minioServer.close();
            minioServer = null;
        }
    }

    @Test
    void testQuery() throws SQLException {
        CalciteManager calciteManager = constructCalciteManager();
        String selectSql = constructSelectSql(dataTable);
        Table table = calciteManager.query(selectSql, TableExtractor.builder().build());
        // TODO assertion
        LOGGER.info("table: {}", table);
        Set<String> tableNames = calciteManager.schema(SCHEMA_NAME).getTableNames();
        LOGGER.info("table names = {}", tableNames);
    }

    @Test
    void testExecute() throws SQLException {
        CalciteManager calciteManager = constructCalciteManager();
        String selectSql = constructSelectSql(dataTable);
        calciteManager.execute(
                selectSql,
                resultSet -> {
                    Table table = TableExtractor.builder().build()
                            .apply(resultSet);
                    // TODO assertion
                    LOGGER.info("table: {}", table);
                });
    }

    private static String constructSelectSql(Table dataTable) {
        Schema schema = dataTable.getSchema();
        return String.format(
                "select %s, \"%s\"(\"description\") as \"descriptionLength\" from \"%s\".\"%s\"",
                IntStream.range(0, schema.columnSize())
                        .mapToObj(index -> schema.type(index).castTypeSql(schema.name(index)))
                        .collect(Collectors.joining(", ")),
                StringLength.NAME, SCHEMA_NAME, DATA_TABLE_NAME);
    }

    private static CalciteManager constructCalciteManager() {
        return CalciteManager.builder()
                .dataSource(S3CalciteDataSource.builder()
                        .identifier(SCHEMA_NAME)
                        .namedObject(CalciteManagerTest.DATA_TABLE_NAME, S3ObjectDescriptor.builder()
                                .minioConnection(minioConnection)
                                .bucket(BUCKET_NAME)
                                .objectKey(DATA_OBJECT_KEY)
                                .build())
                        .build())
                .userDefinedFunction(StringLength.builder().build())
                .build();
    }

    private static String csvFormatData(Table dataTable) {
        Schema schema = dataTable.getSchema();
        int columnSize = schema.columnSize();
        String header = IntStream.range(0, columnSize)
                .mapToObj(schema::name)
                .collect(Collectors.joining(","));
        String content = dataTable.getRecordList()
                .stream()
                .map(record -> record.getValueList()
                        .stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(",")))
                .collect(Collectors.joining("\n"));
        return String.format("%s\n%s", header, content);
    }
}
