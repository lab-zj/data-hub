package net.zjvis.flint.data.hub.lib.calcite.connector.csv;

import com.google.common.base.Preconditions;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.lib.calcite.connector.CalciteConnector;
import net.zjvis.flint.data.hub.lib.calcite.datasource.CalciteDataSource;
import net.zjvis.flint.data.hub.lib.calcite.datasource.S3CalciteDataSource;
import net.zjvis.flint.data.hub.lib.calcite.resource.S3Resource;
import net.zjvis.flint.data.hub.lib.calcite.resource.StreamResource;
import net.zjvis.flint.data.hub.lib.calcite.schema.s3.S3ObjectDescriptor;

import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class CsvS3 extends AbstractCsv implements CalciteConnector {
    @Builder
    @Jacksonized
    public CsvS3(
            String identifier,
            @Singular("namedObject")
            Map<String, S3Resource> namedStreamResources
    ) {
        super(identifier,
                namedStreamResources.entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue
                        )));
    }

    @Override
    public CalciteDataSource asDataSource() {
        return S3CalciteDataSource.builder()
                .identifier(getIdentifier())
                .namedObjects(getNamedStreamResources()
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                CsvS3::extractS3ObjectDescriptor
                        )))
                .build();
    }

    private static S3ObjectDescriptor extractS3ObjectDescriptor(Map.Entry<String, StreamResource> entry) {
        StreamResource streamResource = entry.getValue();
        Preconditions.checkArgument(streamResource instanceof S3Resource);
        S3Resource s3Resource = (S3Resource) streamResource;
        return S3ObjectDescriptor.builder()
                .minioConnection(s3Resource.getMinioConnection())
                .bucket(s3Resource.getBucket())
                .objectKey(s3Resource.getObjectKey())
                .build();
    }
}
