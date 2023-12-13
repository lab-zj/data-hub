package net.zjvis.flint.data.hub.lib.data.csv;

import lombok.Builder;
import net.zjvis.flint.data.hub.lib.data.Record;
import net.zjvis.flint.data.hub.lib.data.Schema;
import net.zjvis.flint.data.hub.lib.data.Type;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.InputStream;
import java.util.Iterator;
import java.util.stream.Collectors;

public class CsvFileReader {
    private final boolean withHeader;
    private final char separator;
    private final String lineEnding;

    @Builder(toBuilder = true)
    public CsvFileReader(Boolean withHeader, Character separator, String lineEnding) {
        this.withHeader = null == withHeader || withHeader;
        this.separator = null == separator ? ',' : separator;
        this.lineEnding = null == lineEnding ? "\n" : lineEnding;
    }

    public Iterable<Record> read(InputStream inputStream) {
        Table table = doRead(inputStream);
        Schema schema = constructSchema(table);
        Iterator<Row> iterator = table.iterator();
        return () -> new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Record next() {
                Row row = iterator.next();
                return Record.builder()
                        .schema(schema)
                        .valueList(schema.getNameList()
                                .stream()
                                .map(row::getObject)
                                .collect(Collectors.toList()))
                        .build();
            }
        };
    }

    public net.zjvis.flint.data.hub.lib.data.Table readAsTable(InputStream inputStream) {
        Table table = doRead(inputStream);
        Schema schema = constructSchema(table);
        return net.zjvis.flint.data.hub.lib.data.Table.builder()
                .schema(schema)
                .recordList(table.stream()
                        .map(row -> Record.builder()
                                .schema(schema)
                                .valueList(schema.getNameList()
                                        .stream()
                                        .map(row::getObject)
                                        .collect(Collectors.toList()))
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private Table doRead(InputStream inputStream) {
        CsvReadOptions csvReadOptions = CsvReadOptions.builder(inputStream)
                .header(withHeader)
                .allowDuplicateColumnNames(false)
                .separator(separator)
                .lineEnding(lineEnding)
                .ignoreZeroDecimal(false)
                .build();
        return Table.read().csv(csvReadOptions);
    }

    private static Schema constructSchema(Table table) {
        return Schema.builder()
                .nameList(table.columnNames())
                .typeList(table.types()
                        .stream()
                        .map(Type::fromColumnType)
                        .map(optional
                                -> optional.orElseThrow(() -> new IllegalArgumentException("unknown column type")))
                        .collect(Collectors.toList()))
                .build();
    }
}
