package net.zjvis.flint.data.hub.lib.calcite.schema.s3;

import net.zjvis.flint.data.hub.lib.calcite.datasource.S3CalciteDataSource;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaFactory;
import org.apache.calcite.schema.SchemaPlus;

import java.util.Map;

public class S3SchemaFactory implements SchemaFactory {
    public static final S3SchemaFactory INSTANCE = new S3SchemaFactory();
    private static final String BUCKET_PLACEHOLDER = "bucket";
    private static final String ENDPOINT_PLACEHOLDER = "endpoint";
    private static final String ACCESS_KEY_PLACEHOLDER = "access_key";
    private static final String SECRET_KEY_PLACEHOLDER = "secret_key";

    @Override
    public Schema create(SchemaPlus schemaPlus, String name, Map<String, Object> operand) {
        S3CalciteDataSource s3CalciteDataSource = S3CalciteDataSource.builder()
                .identifier(name)
                // TODO add objects
                .build();
        return s3CalciteDataSource.calciteSchema(schemaPlus, name);
    }
}
