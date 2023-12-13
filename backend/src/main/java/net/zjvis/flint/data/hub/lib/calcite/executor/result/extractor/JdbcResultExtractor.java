package net.zjvis.flint.data.hub.lib.calcite.executor.result.extractor;

import lombok.Builder;
import net.zjvis.flint.data.hub.lib.data.Table;
import net.zjvis.flint.data.hub.lib.data.sql.TableExtractor;

import java.sql.ResultSet;

@Builder
public class JdbcResultExtractor implements ResultExtractor<ResultSet, Table> {
    @Override
    public Table apply(ResultSet resultSet) {
        return TableExtractor.builder()
                .build()
                .apply(resultSet);
    }
}