package net.zjvis.flint.data.hub.lib.calcite.executor.sql;

import com.google.common.base.Preconditions;
import net.zjvis.flint.data.hub.lib.calcite.connector.CalciteConnector;
import net.zjvis.flint.data.hub.lib.calcite.connector.jdbc.JdbcConnector;
import net.zjvis.flint.data.hub.lib.calcite.datasource.CalciteDataSource;
import net.zjvis.flint.data.hub.lib.calcite.datasource.JdbcCalciteDataSource;
import net.zjvis.flint.data.hub.lib.data.Record;
import net.zjvis.flint.data.hub.lib.data.Table;
import net.zjvis.flint.data.hub.lib.data.sql.TableExtractor;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.impl.DSL;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public abstract class AbstractJdbcToJdbcTest extends AbstractSqlExecutorTest {
    private JdbcConnector jdbcConnector;

    void setUp() throws IOException {
        jdbcConnector = constructJdbcConnector();
        CalciteDataSource calciteDataSource = jdbcConnector.asDataSource();
        Preconditions.checkArgument(
                calciteDataSource instanceof JdbcCalciteDataSource,
                "calciteDataSource should be JdbcCalciteDataSource");
        Table table = table();
        String inputTableName = super.inputTableName();
        jdbcConnector.createTable(inputTableName, false, table.getSchema());
        // TODO batch insert
        for (Record record : table.getRecordList()) {
            jdbcConnector.insert(record, inputTableName);
        }
    }

    @Override
    protected CalciteConnector sourceConnector() {
        return jdbcConnector;
    }

    @Override
    protected CalciteConnector sinkConnector() {
        return jdbcConnector;
    }

    @Override
    protected Table extractResultTable() throws IOException {
        AtomicReference<Table> tableReference = new AtomicReference<>();
        List<SelectFieldOrAsterisk> fieldList = super.expectedSchema()
                .getNameList()
                .stream()
                .map(fieldName -> DSL.field(DSL.quotedName(fieldName)))
                .collect(Collectors.toList());
        jdbcConnector.select(
                schemaName(),
                super.outputTableName(),
                fieldList,
                resultSet -> tableReference.set(TableExtractor.builder()
                        .build()
                        .apply(resultSet))
        );
        return tableReference.get();
    }

    protected abstract JdbcConnector constructJdbcConnector();

    protected String schemaName() {
        return null;
    }
}
