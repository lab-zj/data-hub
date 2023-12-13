package net.zjvis.flint.data.hub.lib.calcite.connector;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import net.zjvis.flint.data.hub.lib.calcite.connector.csv.CsvFile;
import net.zjvis.flint.data.hub.lib.calcite.connector.csv.CsvS3;
import net.zjvis.flint.data.hub.lib.calcite.connector.jdbc.Mysql;
import net.zjvis.flint.data.hub.lib.calcite.connector.jdbc.Postgresql;
import net.zjvis.flint.data.hub.lib.calcite.datasource.CalciteDataSource;
import net.zjvis.flint.data.hub.lib.data.Record;
import net.zjvis.flint.data.hub.lib.data.Schema;

import java.io.IOException;

@JsonTypeInfo(property = "type", use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CsvFile.class, name = "csv_file"),
        @JsonSubTypes.Type(value = Mysql.class, name = "mysql"),
        @JsonSubTypes.Type(value = Postgresql.class, name = "postgresql"),
        @JsonSubTypes.Type(value = CsvS3.class, name = "s3"),
})
public interface CalciteConnector {
    String getIdentifier();
    CalciteDataSource asDataSource();

    default void setup(Schema schema, Object... operands) throws IOException {
        throw new RuntimeException(String.format("setup not support for class(%s)", getClass().getName()));
    }

    // TODO batch insert
    default void insert(Record record, Object... operands) throws IOException {
        throw new RuntimeException(String.format("insert not support for class(%s)", getClass().getName()));
    }
}
