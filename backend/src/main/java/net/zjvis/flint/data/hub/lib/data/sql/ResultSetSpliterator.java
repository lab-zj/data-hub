package net.zjvis.flint.data.hub.lib.data.sql;

import lombok.Builder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;

public class ResultSetSpliterator<DataType> extends Spliterators.AbstractSpliterator<DataType> {
    private final ResultSet resultSet;
    private final Function<ResultSet, DataType> dataExtractor;

    @Builder
    public ResultSetSpliterator(ResultSet resultSet, Function<ResultSet, DataType> dataExtractor) {
        super(Long.MAX_VALUE, Spliterator.ORDERED);
        this.resultSet = resultSet;
        this.dataExtractor = dataExtractor;
    }

    @Override
    public boolean tryAdvance(Consumer<? super DataType> action) {
        try {
            if (!resultSet.next()) {
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        action.accept(dataExtractor.apply(resultSet));
        return true;
    }
}
