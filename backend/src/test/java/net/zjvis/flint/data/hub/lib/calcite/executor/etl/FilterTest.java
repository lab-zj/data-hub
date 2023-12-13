package net.zjvis.flint.data.hub.lib.calcite.executor.etl;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.zjvis.flint.data.hub.entity.etl.BaseETL;
import net.zjvis.flint.data.hub.entity.etl.ETL;
import net.zjvis.flint.data.hub.entity.etl.Filter;
import net.zjvis.flint.data.hub.entity.etl.SqlOperator;
import net.zjvis.flint.data.hub.lib.calcite.connector.csv.CsvFile;
import net.zjvis.flint.data.hub.lib.calcite.executor.RelExecutor;
import net.zjvis.flint.data.hub.lib.data.LogicalTable;
import net.zjvis.flint.data.hub.lib.data.Table;
import net.zjvis.flint.data.hub.lib.data.Type;
import net.zjvis.flint.data.hub.service.executor.RelNodeExecutor;
import net.zjvis.flint.data.hub.service.graph.planner.conf.ETLVertexConfiguration;
import net.zjvis.flint.data.hub.util.DataItem;
import net.zjvis.flint.data.hub.util.DataTypeEnum;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * @author AaronY
 * @version 1.0
 * @since 2023/10/23
 */
public class FilterTest {

    private AbstractETLTest etlTest;
    private LogicalTable csvTableInput;


    private File directory;
    private File csvFileInput;
    private File csvFileOutPut;
    private File csvFileOutPut2;

    private static final String inputSchemaName = RandomStringUtils.randomAlphabetic(8);

    @BeforeEach
    void setUp() throws IOException {
        directory = Files.createTempDirectory("test-csv").toFile();

        csvTableInput = AbstractETLTest.randomLogicalTable(List.of(Type.FLOAT, Type.INTEGER, Type.DOUBLE));
        csvFileInput = File.createTempFile("input", ".csv", directory);
        csvFileOutPut = File.createTempFile("output", ".csv", directory);
        csvFileOutPut2 = File.createTempFile("output2", ".csv", directory);
        AbstractETLTest.writeCsvFile(csvFileInput, csvTableInput);

        etlTest = new AbstractETLTest(){

            @Override
            protected ETL etl() {
                String colA = csvTableInput.getRandomColName();
                String colB = csvTableInput.getRandomColName();
                String colC = csvTableInput.getRandomColName();
                Object valA = csvTableInput.getRandomColValue(colA);
                Object valB = csvTableInput.getRandomColValue(colB);
                Object valC = csvTableInput.getRandomColValue(colC);
                return Filter.builder()
                        .project(colA)
                        .project(colB)
                        .project(colC)
                        .condition(Filter.Condition.builder()
                                .dimension(Filter.Dimension.builder()
                                        .name(colA)
                                        .operator(SqlOperator.GREATER_THAN)
                                        .value(valA)
                                        .build())
                                .dimension(Filter.Dimension.builder()
                                        .name(colC)
                                        .operator(SqlOperator.LESS_THAN)
                                        .value(valC)
                                        .build())
                                .build())
                        .condition(Filter.Condition.builder()
                                .dimension(Filter.Dimension.builder()
                                        .name(colB)
                                        .operator(SqlOperator.EQUALS)
                                        .value(valB)
                                        .build())
                                .build())
                        .params(BaseETL.Params.baseParamBuilder()
                                .input(DataItem.builder()
                                        .type(DataTypeEnum.CSV)
                                        .name(csvFileInput.getName())
                                        .build())
                                .output(DataItem.builder()
                                        .type(DataTypeEnum.CSV)
                                        .name(csvFileOutPut.getName())
                                        .build())
                                .build())
                        .build();
            }

            @Override
            protected RelExecutor<Table> initExecutor() {
                return RelExecutor.<Table>builder()
                        .connection(CsvFile.builder()
                                .identifier(inputSchemaName)
                                .namedFilePath(RandomStringUtils.randomAlphabetic(4), csvFileInput.getAbsolutePath())
                                .build())
                        .relBuildProcess(etl().apply(List.of(Pair.of(inputSchemaName, FilenameUtils.getBaseName(csvFileInput.getName())))))
                        .outputCalciteConnector(CsvFile.builder()
                                .identifier(RandomStringUtils.randomAlphabetic(8))
                                .namedFilePath("t_output", csvFileOutPut.getAbsolutePath())
                                .build())
                        .sinkTableName("t_output")//TODO weird
                        .build();
            }
        };
    }

    @Test
    void testSerialization_YAML() throws IOException {
        etlTest.testYamlSerialization();
    }

    @Test
    void testExecution() throws IOException {
        etlTest.testExecution();
        System.out.println(csvFileOutPut.getAbsolutePath());
    }

}
