package net.zjvis.flint.data.hub.lib.data.sql;

import lombok.Builder;
import net.zjvis.flint.data.hub.exception.NotSupportException;
import net.zjvis.flint.data.hub.lib.data.Record;
import net.zjvis.flint.data.hub.lib.data.Schema;
import net.zjvis.flint.data.hub.lib.data.Table;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Builder
public class TableExtractor implements Function<ResultSet, Table> {
    @Override
    public Table apply(ResultSet resultSet) {
        try {
            Schema schema = ResultSetExtractor.schemaFromResultSet(resultSet);
            List<Record> recordList = StreamSupport.stream(
                    ResultSetSpliterator.<Record>builder()
                            .resultSet(resultSet)
                            .dataExtractor(resultSetInner -> ResultSetExtractor.recordFromResultSet(resultSetInner, schema))
                            .build(),
                    false
            ).collect(Collectors.toList());
            return Table.builder()
                    .schema(schema)
                    .recordList(recordList)
                    .build();
        } catch (SQLException | NotSupportException e) {
            throw new RuntimeException(e);
        }
    }
}
