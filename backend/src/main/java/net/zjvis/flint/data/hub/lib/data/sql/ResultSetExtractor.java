package net.zjvis.flint.data.hub.lib.data.sql;

import lombok.Builder;
import net.zjvis.flint.data.hub.exception.NotSupportException;
import net.zjvis.flint.data.hub.lib.data.Record;
import net.zjvis.flint.data.hub.lib.data.Schema;
import net.zjvis.flint.data.hub.lib.data.Type;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@Builder
public class ResultSetExtractor implements Function<ResultSet, List<Record>> {
    public static Schema schemaFromResultSet(ResultSet resultSet) throws SQLException, NotSupportException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        Schema.SchemaBuilder builder = Schema.builder();
        for (int index = 0; index < columnCount; index++) {
            String columnLabel = resultSetMetaData.getColumnLabel(index + 1);
            builder.name(StringUtils.isNotBlank(columnLabel) ? columnLabel : resultSetMetaData.getColumnName(index + 1));
            builder.type(fromSqlType(resultSetMetaData.getColumnType(index + 1)));
        }
        return builder.build();
    }

    public static Record recordFromResultSet(ResultSet resultSet, Schema schema) {
        return Record.builder()
                .schema(schema)
                .valueList(IntStream.range(0, schema.getNameList().size())
                        .mapToObj(index -> extractValue(
                                schema.getTypeList().get(index),
                                resultSet,
                                index + 1
                        ))
                        .collect(Collectors.toList())
                ).build();
    }

    @Override
    public List<Record> apply(ResultSet resultSet) {
        try {
            Schema schema = schemaFromResultSet(resultSet);
            return StreamSupport.stream(
                    ResultSetSpliterator.<Record>builder()
                            .resultSet(resultSet)
                            .dataExtractor(resultSetInner -> recordFromResultSet(resultSetInner, schema))
                            .build(),
                    false
            ).collect(Collectors.toList());
        } catch (SQLException | NotSupportException e) {
            throw new RuntimeException(e);
        }
    }

    private static Type fromSqlType(int type) throws NotSupportException {
        if (Types.BOOLEAN == type) {
            return Type.BOOLEAN;
        }
        if (Types.TINYINT == type
                || Types.SMALLINT == type
                || Types.INTEGER == type) {
            return Type.INTEGER;
        }
        if (Types.BIGINT == type) {
            return Type.LONG;
        }
        if (Types.FLOAT == type
                || Types.REAL == type) {
            return Type.FLOAT;
        }
        if (Types.DOUBLE == type) {
            return Type.DOUBLE;
        }
        if (Types.CHAR == type
                || Types.VARCHAR == type
                || Types.LONGVARCHAR == type) {
            return Type.STRING;
        }
        if (Types.DATE == type) {
            return Type.DATE;
        }
        if (Types.TIME == type || Types.TIMESTAMP == type) {
            return Type.TIME;
        }
        if (Types.JAVA_OBJECT == type) {
            // compatible with mysql text type
            return Type.STRING;
        }
        throw new NotSupportException(String.format("type(%s) not yet support", type));
    }

    private static Object extractValue(Type type, ResultSet resultSet, int index) {
        try {
            switch (type) {
                case BOOLEAN:
                    return resultSet.getBoolean(index);
                case INTEGER:
                    return resultSet.getInt(index);
                case LONG:
                    return resultSet.getLong(index);
                case FLOAT:
                    return resultSet.getFloat(index);
                case DOUBLE:
                    return resultSet.getDouble(index);
                case STRING:
                    return resultSet.getString(index);
                case DATE:
                    return resultSet.getDate(index);
                case TIME:
                    return resultSet.getTime(index);
                case DATE_TIME:
                    return resultSet.getTimestamp(index);
                default:
                    throw new RuntimeException(
                            new NotSupportException(
                                    String.format("not support type(%s)", type)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
