package net.zjvis.flint.data.hub.lib.calcite.datasource;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.lib.calcite.schema.s3.S3ObjectDescriptor;
import net.zjvis.flint.data.hub.lib.calcite.schema.s3.S3Schema;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.util.Util;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Map;

@Getter
@EqualsAndHashCode
@ToString
public class S3CalciteDataSource implements CalciteDataSource {
    private final String identifier;
    private final Map<String, S3ObjectDescriptor> namedObjects;

    @Builder
    @Jacksonized
    public S3CalciteDataSource(
            String identifier,
            @Singular("namedObject")
            Map<String, S3ObjectDescriptor> namedObjects
    ) {
        this.identifier = identifier;
        this.namedObjects = namedObjects;
    }

    @Override
    public String identifier() {
        return Util.first(identifier, RandomStringUtils.randomAlphabetic(8));
    }

    @Override
    public Schema calciteSchema(SchemaPlus parentSchema, String identifier) {
        return S3Schema.builder()
                .namedObjects(namedObjects)
                .build();
    }
}
