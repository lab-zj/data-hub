package net.zjvis.flint.data.hub.lib.calcite.schema.table.stream.resource.csv;

import lombok.Builder;
import net.zjvis.flint.data.hub.lib.calcite.resource.StreamResource;
import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.file.CsvEnumerator;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.linq4j.*;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.apache.calcite.schema.QueryableTable;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Schemas;
import org.apache.calcite.schema.TranslatableTable;
import org.apache.calcite.util.ImmutableIntList;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;

public class StreamResourceCsvTranslatableTable extends StreamResourceCsvTable
        implements QueryableTable, TranslatableTable {
    @Builder
    public StreamResourceCsvTranslatableTable(
            StreamResource streamResource,
            @Nullable RelProtoDataType protoRowType
    ) {
        super(streamResource, protoRowType);
    }

    /**
     * Returns an enumerable over a given projection of the fields.
     */
    @SuppressWarnings("unused") // called from generated code
    public Enumerable<Object> project(final DataContext root, final int[] fields) {
        final AtomicBoolean cancelFlag = DataContext.Variable.CANCEL_FLAG.get(root);
        return new AbstractEnumerable<>() {
            @Override
            public Enumerator<Object> enumerator() {
                JavaTypeFactory typeFactory = root.getTypeFactory();
                return new CsvEnumerator<>(
                        StreamResourceCsvTranslatableTable.super.extractSource(streamResource),
                        cancelFlag,
                        getFieldTypes(typeFactory),
                        ImmutableIntList.of(fields)
                );
            }
        };
    }

    @Override
    public Expression getExpression(SchemaPlus schema, String tableName, Class clazz) {
        return Schemas.tableExpression(schema, getElementType(), tableName, clazz);
    }

    @Override
    public Type getElementType() {
        return Object[].class;
    }

    @Override
    public <T> Queryable<T> asQueryable(QueryProvider queryProvider,
                                        SchemaPlus schema, String tableName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RelNode toRel(
            RelOptTable.ToRelContext context,
            RelOptTable relOptTable) {
        // Request all fields.
        final int fieldCount = relOptTable.getRowType().getFieldCount();
        final int[] fields = CsvEnumerator.identityList(fieldCount);
        return new StreamResourceCsvTableScan(context.getCluster(), relOptTable, this, fields);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
