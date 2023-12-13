package net.zjvis.flint.data.hub.lib.calcite.connector.jdbc;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.zjvis.flint.data.hub.lib.calcite.JooqManager;
import net.zjvis.flint.data.hub.lib.calcite.connector.CalciteConnector;
import net.zjvis.flint.data.hub.lib.calcite.datasource.CalciteDataSource;
import net.zjvis.flint.data.hub.lib.calcite.datasource.JdbcCalciteDataSource;
import net.zjvis.flint.data.hub.lib.data.Record;
import net.zjvis.flint.data.hub.lib.data.Schema;
import net.zjvis.flint.data.hub.lib.data.Type;
import org.jooq.DataType;
import org.jooq.SQLDialect;
import org.jooq.SelectFieldOrAsterisk;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@EqualsAndHashCode
@ToString
public abstract class JdbcConnector implements CalciteConnector {
    private transient JooqManager jooqManager;

    @Override
    public void setup(Schema schema, Object... operands) throws IOException {
        Preconditions.checkArgument(
                operands.length >= 2, "jdbc connector setup should provided 2 operand(s)");
        String tableName = extractTableName(operands);
        boolean overwrite = extractOverwrite(operands);
        createTable(tableName, overwrite, schema);
    }

    @Override
    public void insert(Record record, Object... operands) throws IOException {
        Preconditions.checkArgument(
                operands.length >= 1, "jdbc connector insert should more than 1 operand(s)");
        String tableName = extractTableName(operands);
        jooqManager()
                .insertInto(schemaName(), tableName, ImmutableSet.copyOf(record.nameList()), record.getValueList());
    }

    public void createTable(String tableName, boolean overwrite, Schema schema) throws IOException {
        // TODO use overwrite
        Map<String, DataType<?>> fields = IntStream.range(0, schema.columnSize())
                .boxed()
                .collect(Collectors.toMap(
                        schema::name,
                        index -> asDataType(schema.type(index))
                ));
        jooqManager()
                .createTable(schemaName(), tableName, fields);
    }

    public void select(
            String schemaName,
            String tableName,
            List<SelectFieldOrAsterisk> fieldList,
            Consumer<ResultSet> resultSetConsumer
    ) throws IOException {
        jooqManager()
                .select(schemaName, tableName, fieldList, resultSetConsumer);
    }

    protected String schemaName() {
        return null;
    }

    protected abstract SQLDialect dialect();

    protected DataSource dataSource() {
        CalciteDataSource dataSource = asDataSource();
        Preconditions.checkArgument(
                dataSource instanceof JdbcCalciteDataSource, "dataSource should be JdbcCalciteDataSource");
        return ((JdbcCalciteDataSource) dataSource).dataSource();
    }

    private JooqManager jooqManager() {
        if (null == jooqManager) {
            jooqManager = JooqManager.builder()
                    .dataSource(dataSource())
                    .dialect(dialect())
                    .build();
        }
        return jooqManager;
    }

    private String extractTableName(Object[] operands) {
        if (operands.length < 1 || !(operands[0] instanceof String)) {
            throw new IllegalArgumentException("tableName not found in operands");
        }
        return (String) operands[0];
    }

    private boolean extractOverwrite(Object[] operands) {
        if (operands.length < 2 || !(operands[1] instanceof Boolean)) {
            return false;
        }
        return (boolean) operands[1];
    }

    private DataType<?> asDataType(Type type) {
        switch (type) {
            case SHORT:
                return org.jooq.impl.SQLDataType.SMALLINT;
            case INTEGER:
                return org.jooq.impl.SQLDataType.INTEGER;
            case LONG:
                return org.jooq.impl.SQLDataType.BIGINT;
            case FLOAT:
                return org.jooq.impl.SQLDataType.FLOAT;
            case BOOLEAN:
                return org.jooq.impl.SQLDataType.BOOLEAN;
            case STRING:
                return org.jooq.impl.SQLDataType.VARCHAR;
            case DOUBLE:
                return org.jooq.impl.SQLDataType.DOUBLE;
            case DATE:
                return org.jooq.impl.SQLDataType.DATE;
            case TIME:
                return org.jooq.impl.SQLDataType.TIME;
            case DATE_TIME:
            case INSTANT:
                return org.jooq.impl.SQLDataType.TIMESTAMP;
            case LOB:
                return org.jooq.impl.SQLDataType.CLOB;
            case SKIP:
                return org.jooq.impl.SQLDataType.OTHER;
            default:
                throw new IllegalArgumentException(String.format("Unsupported type: %s", type));
        }
    }
}
