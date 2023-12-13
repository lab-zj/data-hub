package net.zjvis.flint.data.hub.lib.calcite.schema.table.stream.resource.csv;

import lombok.Builder;
import net.zjvis.flint.data.hub.lib.calcite.resource.StreamResource;
import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.file.CsvEnumerator;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.util.ImmutableIntList;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class StreamResourceCsvScannableTable extends StreamResourceCsvTable implements ScannableTable {
    @Builder
    public StreamResourceCsvScannableTable(
            StreamResource streamResource,
            @Nullable RelProtoDataType protoRowType
    ) {
        super(streamResource, protoRowType);
    }

    @Override
    public Enumerable<Object[]> scan(DataContext root) {
        JavaTypeFactory typeFactory = root.getTypeFactory();
        final List<RelDataType> fieldTypes = getFieldTypes(typeFactory);
        final List<Integer> fields = ImmutableIntList.identity(fieldTypes.size());
        final AtomicBoolean cancelFlag = DataContext.Variable.CANCEL_FLAG.get(root);
        return new AbstractEnumerable<>() {
            @Override
            public Enumerator<Object[]> enumerator() {
                return new CsvEnumerator<>(
                        extractSource(streamResource),
                        cancelFlag,
                        false,
                        null,
                        CsvEnumerator.arrayConverter(fieldTypes, fields, false));
            }
        };
    }
}
