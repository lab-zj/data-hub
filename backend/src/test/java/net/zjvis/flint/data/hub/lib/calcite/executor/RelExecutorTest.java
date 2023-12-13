package net.zjvis.flint.data.hub.lib.calcite.executor;

import net.zjvis.flint.data.hub.lib.calcite.connector.csv.CsvFile;
import net.zjvis.flint.data.hub.lib.calcite.connector.jdbc.Postgresql;
import net.zjvis.flint.data.hub.lib.calcite.func.StringLength;
import net.zjvis.flint.data.hub.lib.data.Table;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.JoinRelType;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.tools.RelBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class RelExecutorTest {
    private File inputCsvFile;
    private File outputCsvFile;

    @BeforeEach
    void setUp() throws IOException {
        inputCsvFile = File.createTempFile("input_", ".csv");
        outputCsvFile = File.createTempFile("output_", ".csv");
        try (FileOutputStream fileOutputStream = new FileOutputStream(inputCsvFile)) {
            IOUtils.write(
                    "year,manufacturer,model,description,price\n" +
                            "1996,Jeep,Grand Cherokee,some description,4799.00\n" +
                            "1998,asdasd,1231,fgbgfgfg,4099.00\n" +
                            "1997,SDFSF,2222,some DSSF34,3200.00",
                    fileOutputStream,
                    StandardCharsets.UTF_8
            );
        }
    }

    @AfterEach
    void tearDown() {
        if (null != inputCsvFile) {
            FileUtils.deleteQuietly(inputCsvFile);
            inputCsvFile = null;
        }
        if (null != outputCsvFile) {
            FileUtils.deleteQuietly(outputCsvFile);
            outputCsvFile = null;
        }
    }

    //csv 2 csv
    @Test
    void testExecute() throws IOException {
        String schemaName = "csv";
        String tableName = FilenameUtils.getBaseName(inputCsvFile.getAbsolutePath());
        StringLength stringLength = StringLength.builder().build();
        Function<RelBuilder, RelNode> relBuilderProcess = relBuilder -> {
            RelBuilder scanResult = relBuilder.scan(schemaName, tableName);
            return scanResult.project(scanResult.field("year"),
                            scanResult.field("manufacturer"),
                            scanResult.alias(
                                    scanResult.call(
                                            AbstractCalciteExecutor.sqlUserDefinedFunction(
                                                    relBuilder.getTypeFactory(),
                                                    stringLength
                                            ),
                                            scanResult.field("description")),
                                    "descriptionLength"))
                    .filter(scanResult.equals(
                            scanResult.field("year"),
                            scanResult.literal("1996")
                    )).build();
        };
        Executor executor = RelExecutor.<Table>builder()
                .connection(CsvFile.builder()
                        .identifier(schemaName)
                        .namedFilePath("t_input", inputCsvFile.getAbsolutePath())
                        .build())
                .userDefinedFunction(stringLength)
                .relBuildProcess(relBuilderProcess)
                .outputCalciteConnector(CsvFile.builder()
                        .identifier("output")
                        .namedFilePath("t_output", outputCsvFile.getAbsolutePath())
                        .build())
                .sinkTableName("t_output")//TODO weird
                .build();
        executor.execute();
        System.out.println(FileUtils.readFileToString(outputCsvFile, StandardCharsets.UTF_8));
    }


    //pg 2 pg
    @Test
    void testExecute2() throws IOException {
        String schemaName = "pg";
        StringLength stringLength = StringLength.builder().build();
        Function<RelBuilder, RelNode> relBuilderProcess = relBuilder -> {
            RelBuilder scanResult = relBuilder.scan(schemaName, "t_input");
            return scanResult.project(scanResult.field("year"),
                            scanResult.field("manufacturer"),
                            scanResult.alias(
                                    scanResult.call(
                                            AbstractCalciteExecutor.sqlUserDefinedFunction(
                                                    relBuilder.getTypeFactory(),
                                                    stringLength
                                            ),
                                            scanResult.field("description")),
                                    "descriptionLength"))
                    .filter(scanResult.equals(
                            scanResult.field("year"),
                            scanResult.literal("1996")
                    )).build();
        };
        Executor executor = RelExecutor.<Table>builder()
                .connection(Postgresql.builder()
                        .identifier(schemaName)
                        .host("10.105.20.64")
                        .port(32433)
                        .username("postgres")
                        .password("N3dSMzhtS")
                        .databaseName("crawler_ay")
                        .schemaName("public")
                        .build())
                .userDefinedFunction(stringLength)
                .relBuildProcess(relBuilderProcess)
                .outputCalciteConnector(Postgresql.builder()
                        .identifier("pg_out")
                        .host("10.105.20.64")
                        .port(32433)
                        .username("postgres")
                        .password("N3dSMzhtS")
                        .databaseName("crawler_ay")
                        .schemaName("public")
                        .build())
                .sinkTableName("ttt_ouput")
                .build();
        executor.execute();
        System.out.println(FileUtils.readFileToString(outputCsvFile, StandardCharsets.UTF_8));
    }

    //csv + pg -> pg
    @Test
    void testJoin() throws IOException {
        String tableName = FilenameUtils.getBaseName(inputCsvFile.getAbsolutePath());
        StringLength stringLength = StringLength.builder().build();
        Function<RelBuilder, RelNode> relBuilderProcess = relBuilder -> {
            relBuilder.scan("csv", tableName);
            relBuilder.scan("pg", "t_input");

            RexNode condition = relBuilder.call(SqlStdOperatorTable.EQUALS,
                    relBuilder.field(2, 0, "manufacturer"),
                    relBuilder.field(2, 1, "manufacturer")
            );

            relBuilder.join(JoinRelType.INNER, condition);

            return relBuilder.build();
        };
        Executor executor = RelExecutor.<Table>builder()
                .connection(CsvFile.builder()
                        .identifier("csv")
                        .namedFilePath("LOCAL", inputCsvFile.getAbsolutePath())
                        .build())
                .connection(Postgresql.builder()
                        .identifier("pg")
                        .host("10.105.20.64")
                        .port(32433)
                        .username("postgres")
                        .password("N3dSMzhtS")
                        .databaseName("crawler_ay")
                        .schemaName("public")
                        .build())
                .userDefinedFunction(stringLength)
                .relBuildProcess(relBuilderProcess)
                .outputCalciteConnector(Postgresql.builder()
                        .identifier("output")
                        .host("10.105.20.64")
                        .port(32433)
                        .username("postgres")
                        .password("N3dSMzhtS")
                        .databaseName("crawler_ay")
                        .schemaName("public")
                        .build())
                .sinkTableName("tt2_ouput")
                .build();
        executor.execute();
        System.out.println(FileUtils.readFileToString(outputCsvFile, StandardCharsets.UTF_8));
    }
}
