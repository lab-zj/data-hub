package net.zjvis.flint.data.hub.lib.calcite.schema.s3;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import net.zjvis.flint.data.hub.lib.calcite.resource.S3Resource;
import net.zjvis.flint.data.hub.lib.calcite.schema.table.stream.resource.csv.StreamResourceCsvScannableTable;
import net.zjvis.flint.data.hub.lib.minio.MinioConnection;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;

import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class S3Schema extends AbstractSchema {
    private final MinioConnection minioConnection;
    private final Map<String, S3ObjectDescriptor> namedObjects;

    @Builder
    public S3Schema(
            @Deprecated
            MinioConnection minioConnection,
            @Singular("namedObject")
            Map<String, S3ObjectDescriptor> namedObjects
    ) {
        this.minioConnection = minioConnection;
        this.namedObjects = namedObjects;
    }

    protected Map<String, Table> getTableMap() {
        return namedObjects.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> constructTable(entry.getValue())
                ));
    }

    private Table constructTable(S3ObjectDescriptor s3ObjectDescriptor) {
        S3Resource s3Resource = S3Resource.builder()
                .minioConnection(s3ObjectDescriptor.getMinioConnection())
                .bucket(s3ObjectDescriptor.getBucket())
                .objectKey(s3ObjectDescriptor.getObjectKey())
                .build();
        return StreamResourceCsvScannableTable.builder()
                .streamResource(s3Resource)
                // TODO use operands
                .protoRowType(null)
                .build();
    }
}
