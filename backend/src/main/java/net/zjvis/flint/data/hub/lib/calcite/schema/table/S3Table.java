package net.zjvis.flint.data.hub.lib.calcite.schema.table;

import net.zjvis.flint.data.hub.lib.calcite.resource.S3Resource;
import net.zjvis.flint.data.hub.lib.calcite.schema.table.stream.resource.csv.StreamResourceCsvScannableTable;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.checkerframework.checker.nullness.qual.Nullable;

// TODO implement with TranslatableTable
public class S3Table extends StreamResourceCsvScannableTable {
    public S3Table(S3Resource s3Resource, @Nullable RelProtoDataType protoRowType) {
        super(s3Resource, protoRowType);
    }
}
