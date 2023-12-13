package net.zjvis.flint.data.hub.lib.calcite.datasource;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;

@JsonTypeInfo(property = "type", use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = JdbcCalciteDataSource.class, name = "jdbc"),
        @JsonSubTypes.Type(value = S3CalciteDataSource.class, name = "s3"),
        @JsonSubTypes.Type(value = CassandraCalciteDataSource.class, name = "cassandra"),
})
public interface CalciteDataSource {
    String identifier();

    Schema calciteSchema(SchemaPlus parentSchema, String name);
}
