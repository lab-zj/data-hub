package net.zjvis.flint.data.hub.lib.calcite.executor.sql;

import net.zjvis.flint.data.hub.lib.calcite.connector.CalciteConnector;
import net.zjvis.flint.data.hub.lib.calcite.executor.Executor;
import net.zjvis.flint.data.hub.lib.calcite.executor.SqlExecutor;
import net.zjvis.flint.data.hub.lib.calcite.func.StringLength;
import net.zjvis.flint.data.hub.lib.data.Record;
import net.zjvis.flint.data.hub.lib.data.Schema;
import net.zjvis.flint.data.hub.lib.data.Table;
import net.zjvis.flint.data.hub.lib.data.Type;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.stream.Collectors;

public abstract class AbstractSqlExecutorTest {
    private Table table;

    void testSqlExecute() throws IOException {
        final CalciteConnector source = sourceConnector();
        final String inputTableName = "t_input";
        final CalciteConnector sink = sinkConnector();
        final String outputTableName = "t_output";
        Executor executor = SqlExecutor.builder()
                .sourceConnector(source)
                .userDefinedFunction(StringLength.builder().build())
                .selectSql(String.format(
                        "select *, \"%s\"(\"description\") as \"%s\" from \"%s\".\"%s\"",
                        StringLength.NAME, descriptionLengthFieldName(), source.getIdentifier(), inputTableName))
                .sinkConnector(sink)
                .sinkTableName(outputTableName)
                .build();
        executor.execute();
        final Table resultTable = extractResultTable();
        final Table sourceTable = table();
        Schema expectedSchema = expectedSchema();
        Assertions.assertEquals(
                expectedSchema,
                resultTable.getSchema()
        );
        Assertions.assertEquals(
                sourceTable.getRecordList()
                        .stream()
                        .map(record -> record.toBuilder()
                                .schema(expectedSchema)
                                .value(String.valueOf(record.value("description").orElseThrow()).length())
                                .build())
                        .collect(Collectors.toList()),
                resultTable.getRecordList()
        );
    }

    protected Table table() {
        if (null == table) {
            table = generateTable();
        }
        return table;
    }

    protected Table generateTable() {
        Schema schema = Schema.builder()
                .name("year")
                .type(Type.INTEGER)
                .name("manufacturer")
                .type(Type.STRING)
                .name("model")
                .type(Type.STRING)
                .name("description")
                .type(Type.STRING)
                .name("price")
                .type(Type.DOUBLE)
                .build();
        return Table.builder()
                .schema(schema)
                .record(Record.builder()
                        .schema(schema)
                        .value(1996)
                        .value("Jeep")
                        .value("Grand Cherokee")
                        .value("some description of Jeep Grand Cherokee")
                        .value(4799.00)
                        .build())
                .record(Record.builder()
                        .schema(schema)
                        .value(1997)
                        .value("Ford")
                        .value("E350")
                        .value("some description of Ford E350")
                        .value(3000.00)
                        .build())
                .build();
    }

    protected abstract CalciteConnector sourceConnector();

    protected abstract CalciteConnector sinkConnector();

    protected abstract Table extractResultTable() throws IOException;

    protected String inputTableName() {
        return "t_input";
    }

    protected String outputTableName() {
        return "t_output";
    }

    protected String descriptionLengthFieldName() {
        return "descriptionLength";
    }

    protected Schema expectedSchema() {
        return table()
                .getSchema()
                .toBuilder()
                .name(descriptionLengthFieldName())
                .type(Type.INTEGER)
                .build();
    }
}
