package net.zjvis.flint.data.hub.lib.data.csv;

import lombok.Builder;
import net.zjvis.flint.data.hub.lib.data.Record;
import net.zjvis.flint.data.hub.lib.data.Schema;
import net.zjvis.flint.data.hub.lib.data.Table;
import net.zjvis.flint.data.hub.lib.data.Type;
import org.apache.commons.lang3.RandomStringUtils;
import tech.tablesaw.columns.Column;
import tech.tablesaw.io.csv.CsvWriteOptions;
import tech.tablesaw.io.csv.CsvWriter;

import java.io.OutputStream;
import java.util.List;
import java.util.stream.IntStream;

public class CsvFileWriter {
    private static final CsvWriter CSV_WRITER = new CsvWriter();
    private final boolean withHeader;
    private final char separator;
    private final String lineEnding;

    @Builder
    public CsvFileWriter(
            Boolean withHeader,
            Character separator,
            String lineEnding
    ) {
        this.withHeader = null == withHeader || withHeader;
        this.separator = null == separator ? ',' : separator;
        this.lineEnding = null == lineEnding ? "\n" : lineEnding;
    }

    public void write(Table logicalTable, OutputStream outputStream) {
        CsvWriteOptions csvFileOptions = CsvWriteOptions.builder(outputStream)
                .header(withHeader)
                .separator(separator)
                .lineEnd(lineEnding)
                .build();
        Schema schema = logicalTable.getSchema();
        List<Record> recordList = logicalTable.getRecordList();
        tech.tablesaw.api.Table table = tech.tablesaw.api.Table.create(String.format("temp-%s", RandomStringUtils.randomAlphabetic(8)))
                .addColumns(IntStream.range(0, schema.columnSize())
                        .mapToObj(index -> {
                            String name = schema.name(index);
                            Type type = schema.type(index);
                            Column<?> column = type.columnType().create(name);
                            recordList.stream()
                                    .map(record -> record.value(index))
                                    .forEach(column::appendObj);
                            return column;
                        }).toArray(Column[]::new));
        CSV_WRITER.write(table, csvFileOptions);
    }
}
