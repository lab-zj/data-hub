package net.zjvis.flint.data.hub.lib.calcite.executor.sql;

import net.zjvis.flint.data.hub.lib.calcite.connector.CalciteConnector;
import net.zjvis.flint.data.hub.lib.calcite.connector.csv.CsvS3;
import net.zjvis.flint.data.hub.lib.calcite.resource.S3Resource;
import net.zjvis.flint.data.hub.lib.data.Table;
import net.zjvis.flint.data.hub.lib.data.csv.CsvFileReader;
import net.zjvis.flint.data.hub.lib.data.csv.CsvFileWriter;
import net.zjvis.flint.data.hub.lib.minio.MinioConnection;
import net.zjvis.flint.data.hub.lib.minio.MinioManager;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.io.*;

public class CsvS3ToCsvS3TestVirtualFile extends AbstractSqlExecutorTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvS3ToCsvS3TestVirtualFile.class);
    private static final String BUCKET_NAME = "calcite-test";
    private static final String INPUT_OBJECT_KEY = "input.csv";
    private static final String OUTPUT_OBJECT_KEY = "output.csv";
    @Container
    private static GenericContainer<?> minioServer;
    private static MinioConnection minioConnection;
    private static MinioManager minioManager;
    private CsvS3 csvS3;

    @BeforeAll
    static void beforeAll() {
        int consolePort = 9001;
        minioServer = new GenericContainer<>(DockerImageName.parse("minio/minio:RELEASE.2022-09-07T22-25-02Z"))
                .withExposedPorts(9000, consolePort)
                .withCommand("server", "/data", "--console-address", String.format(":%s", consolePort));
        minioServer.start();
        String host = minioServer.getHost();
        LOGGER.info("minio web interface: http://{}:{}", host, minioServer.getMappedPort(consolePort));
        minioConnection = MinioConnection.builder()
                .endpoint(String.format("http://%s:%s", host, minioServer.getFirstMappedPort()))
                .accessKey("minioadmin")
                .accessSecret("minioadmin")
                .build();
        minioManager = MinioManager.builder()
                .minioConnection(minioConnection)
                .build();
    }

    @AfterAll
    static void afterAll() {
        if (null != minioServer) {
            minioServer.close();
            minioServer = null;
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        minioManager.bucketRemove(BUCKET_NAME, true);
        minioManager.bucketCreate(BUCKET_NAME, false);
        File tableFile = File.createTempFile("table_", ".csv");
        CsvFileWriter csvFileWriter = CsvFileWriter.builder().build();
        try (FileOutputStream fileOutputStream = new FileOutputStream(tableFile)) {
            csvFileWriter.write(super.table(), fileOutputStream);
        }
        minioManager.objectUpload(new FileInputStream(tableFile), BUCKET_NAME, INPUT_OBJECT_KEY);
        csvS3 = CsvS3.builder()
                .identifier("s_csv")
                .namedObject(
                        inputTableName(),
                        S3Resource.builder()
                                .minioConnection(minioConnection)
                                .bucket(BUCKET_NAME)
                                .objectKey(INPUT_OBJECT_KEY)
                                .build())
                .namedObject(
                        outputTableName(),
                        S3Resource.builder()
                                .minioConnection(minioConnection)
                                .bucket(BUCKET_NAME)
                                .objectKey(OUTPUT_OBJECT_KEY)
                                .build()
                ).build();
    }

    @Test
    void testSqlExecutor() throws IOException {
        super.testSqlExecute();
    }

    @Override
    protected CalciteConnector sourceConnector() {
        return csvS3;
    }

    @Override
    protected CalciteConnector sinkConnector() {
        return csvS3;
    }

    @Override
    protected Table extractResultTable() throws IOException {
        Table resultTable;
        try (InputStream inputStream = minioManager.objectGet(BUCKET_NAME, OUTPUT_OBJECT_KEY)) {
            CsvFileReader csvFileReader = CsvFileReader.builder().build();
            resultTable = csvFileReader.readAsTable(inputStream);
        }
        return resultTable;
    }
}
