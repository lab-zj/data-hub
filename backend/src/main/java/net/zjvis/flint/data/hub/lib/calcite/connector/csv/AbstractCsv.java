package net.zjvis.flint.data.hub.lib.calcite.connector.csv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.zjvis.flint.data.hub.lib.calcite.connector.CalciteConnector;
import net.zjvis.flint.data.hub.lib.calcite.resource.StreamResource;
import net.zjvis.flint.data.hub.lib.data.Record;
import net.zjvis.flint.data.hub.lib.data.Schema;
import net.zjvis.flint.data.hub.lib.data.Type;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.io.csv.CsvWriteOptions;
import tech.tablesaw.io.csv.CsvWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@EqualsAndHashCode
@ToString
public abstract class AbstractCsv implements CalciteConnector {
    @Getter
    @JsonProperty
    private final String identifier;
    @Getter
    @JsonProperty
    private final Map<String, StreamResource> namedStreamResources;

    public AbstractCsv(
            String identifier,
            Map<String, StreamResource> namedStreamResources
    ) {
        this.identifier = identifier;
        this.namedStreamResources = namedStreamResources;
    }

    @Override
    public void setup(Schema schema, Object... operands) throws IOException {
        Preconditions.checkArgument(
                operands.length >= 1, "Csv setup should provided 1 operand(s)");
        String tableName = extractTableName(operands);
        StreamResource streamResource = namedStreamResources.get(tableName);
        Preconditions.checkNotNull(streamResource, "streamResource not found by name(%s)", tableName);
        Table table = constructTable(schema, Collections.emptyList());
        writeToStreamResource(streamResource, table, true, false);
    }

    @Override
    public void insert(Record record, Object... operands) throws IOException {
        Preconditions.checkArgument(
                operands.length >= 1, "Csv setup should provided 1 operand(s)");
        String tableName = extractTableName(operands);
        StreamResource streamResource = namedStreamResources.get(tableName);
        Preconditions.checkNotNull(streamResource, "streamResource not found by name(%s)", tableName);
        Schema schema = record.getSchema();
        Table table = constructTable(schema, Collections.singletonList(record));
        writeToStreamResource(streamResource, table, false, true);
    }

    private Table constructTable(Schema schema, List<Record> recordList) {
        return Table.create(identifier)
                .addColumns(IntStream.range(0, schema.columnSize())
                        .mapToObj(index -> {
                            Type type = schema.type(index);
                            String name = schema.name(index);
                            Column<?> column = type.columnType()
                                    .create(name);
                            if (null != recordList) {
                                recordList.forEach(record -> column.appendObj(record.value(index)));
                            }
                            return column;
                        }).toArray(Column[]::new)
                );
    }

    private void writeToStreamResource(
            StreamResource streamResource,
            Table table,
            boolean withHeader,
            boolean append
    ) throws IOException {
        try (OutputStream outputStream = streamResource.openOutputStream(append)) {
            CsvWriter csvWriter = new CsvWriter();
            csvWriter.write(
                    table,
                    CsvWriteOptions.builder(outputStream)
                            .header(withHeader)
                            .build());
        }
    }

    private String extractTableName(Object[] operands) {
        if (operands.length < 1 || !(operands[0] instanceof String)) {
            throw new IllegalArgumentException("tableName not found in operands");
        }
        return (String) operands[0];
    }
}
