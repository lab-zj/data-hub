package net.zjvis.flint.data.hub.lib.calcite;

import com.google.common.collect.ImmutableSet;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@EqualsAndHashCode
@ToString
public class JooqManager {
    private final DataSource dataSource;
    private final SQLDialect dialect;
    private transient DSLContext dslContextInstance;

    @Builder
    public JooqManager(DataSource dataSource, SQLDialect dialect) {
        this.dataSource = dataSource;
        this.dialect = dialect;
    }

    public void createTable(String schemaName, String tableName, Map<String, DataType<?>> fields) {
        DSLContext dslContext = dslContextInstance();
        try (CreateTableColumnStep createTableColumnStep = dslContext
                .createTableIfNotExists(
                        null != schemaName ? DSL.quotedName(schemaName, tableName) : DSL.quotedName(tableName))
                .columns(fields.entrySet()
                        .stream()
                        .map(entry -> DSL.field(DSL.quotedName(entry.getKey()), entry.getValue()))
                        .collect(Collectors.toList()))
        ) {
            createTableColumnStep.execute();
        }
    }

    public void batchInsertInto(
            String schemaName,
            String tableName,
            ImmutableSet<String> fieldNameList,
            Iterable<List<Object>> records
    ) {
        DSLContext dslContext = dslContextInstance();
        try (InsertValuesStepN<Record> insertIntoSchema = dslContext
                .insertInto(
                        DSL.table(
                                null != schemaName ? DSL.quotedName(schemaName, tableName) : DSL.quotedName(tableName)),
                        fieldNameList.stream()
                                .map(fieldName -> DSL.field(DSL.quotedName(fieldName)))
                                .collect(Collectors.toList()))) {
            InsertValuesStepN<Record> insertValues = null;
            for (List<Object> record : records) {
                insertValues = insertIntoSchema.values(record);
            }
            // records may be empty
            if (null != insertValues) {
                insertValues.execute();
            }
        }
    }

    public void insertInto(
            String schemaName,
            String tableName,
            ImmutableSet<String> fieldNameList,
            List<Object> fieldValueList
    ) {
        batchInsertInto(schemaName, tableName, fieldNameList, Collections.singletonList(fieldValueList));
    }

    public void select(
            String schemaName,
            String tableName,
            List<SelectFieldOrAsterisk> fieldList,
            Consumer<ResultSet> resultSetConsumer
    ) {
        DSL.using(configuration())
                .transaction(tx -> {
                    try (
                            SelectSelectStep<Record> select = tx.dsl().select(fieldList)
                    ) {
                        SelectJoinStep<Record> from = select
                                .from(DSL.table(null != schemaName
                                        ? DSL.quotedName(schemaName, tableName)
                                        : DSL.quotedName(tableName)));
                        try (ResultSet resultSet = from.fetchResultSet()) {
                            resultSetConsumer.accept(resultSet);
                        } catch (SQLException e) {
                            throw new IOException(e);
                        }
                    }
                });
    }

    private DSLContext dslContextInstance() {
        if (null == dslContextInstance) {
            dslContextInstance = DSL.using(configuration());
        }
        return dslContextInstance;
    }

    private Configuration configuration() {
        return new DefaultConfiguration()
                .set(dataSource)
                .set(dialect);
    }
}
