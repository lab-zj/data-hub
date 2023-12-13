package net.zjvis.flint.data.hub.lib.data;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.columns.SkipColumnType;
import tech.tablesaw.columns.booleans.BooleanColumnType;
import tech.tablesaw.columns.dates.DateColumnType;
import tech.tablesaw.columns.datetimes.DateTimeColumnType;
import tech.tablesaw.columns.instant.InstantColumnType;
import tech.tablesaw.columns.numbers.*;
import tech.tablesaw.columns.strings.StringColumnType;
import tech.tablesaw.columns.strings.TextColumnType;
import tech.tablesaw.columns.times.TimeColumnType;

import java.sql.Types;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public enum Type {
    SHORT(ImmutableSet.of(ShortColumnType.instance()),
            ImmutableSet.of(Types.TINYINT, Types.SMALLINT)),
    INTEGER(ImmutableSet.of(IntColumnType.instance()),
            ImmutableSet.of(Types.INTEGER)),
    LONG(ImmutableSet.of(LongColumnType.instance()),
            ImmutableSet.of(Types.BIGINT)),
    FLOAT(ImmutableSet.of(FloatColumnType.instance()),
            ImmutableSet.of(Types.FLOAT, Types.REAL)),
    BOOLEAN(ImmutableSet.of(BooleanColumnType.instance()),
            ImmutableSet.of(Types.BOOLEAN)),
    STRING(ImmutableSet.of(StringColumnType.instance()),
            ImmutableSet.of(Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR)),
    DOUBLE(ImmutableSet.of(DoubleColumnType.instance()),
            ImmutableSet.of(Types.DOUBLE)),
    DATE(ImmutableSet.of(DateColumnType.instance()),
            ImmutableSet.of(Types.DATE)),
    TIME(ImmutableSet.of(TimeColumnType.instance()),
            ImmutableSet.of(Types.TIME)),
    DATE_TIME(ImmutableSet.of(DateTimeColumnType.instance()),
            ImmutableSet.of(Types.TIMESTAMP)),
    INSTANT(ImmutableSet.of(InstantColumnType.instance()),
            ImmutableSet.of(Types.TIMESTAMP_WITH_TIMEZONE)),
    LOB(ImmutableSet.of(TextColumnType.instance()),
            ImmutableSet.of(Types.CLOB)),
    SKIP(ImmutableSet.of(SkipColumnType.instance()),
            ImmutableSet.of(Types.OTHER));

    private static final Map<ColumnType, Type> columnTypeIndexing = Arrays.stream(Type.values())
            .flatMap(type -> type.getColumnTypes()
                    .stream()
                    .map(columnType -> Map.entry(columnType, type)))
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue));
    private static final Map<Integer, Type> sqlTypeIndexing = Arrays.stream(Type.values())
            .flatMap(type -> type.getSqlTypes()
                    .stream()
                    .map(sqlType -> Map.entry(sqlType, type)))
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue));
    @Getter
    private final Set<ColumnType> columnTypes;
    @Getter
    private final Set<Integer> sqlTypes;

    Type(Set<ColumnType> columnTypes, Set<Integer> sqlTypes) {
        this.columnTypes = columnTypes;
        this.sqlTypes = sqlTypes;
    }

    public ColumnType columnType() {
        return columnTypes.stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No column type found"));
    }

    public Integer sqlType() {
        return sqlTypes.stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No sql type found"));
    }

    @Deprecated
    public String castTypeSql(String name) {
        String type;
        switch (this) {
            case STRING:
                type = "varchar";
                break;
            case FLOAT:
                type = "FLOAT";
                break;
            case DOUBLE:
                type = "double";
                break;
            case INTEGER:
                type = "integer";
                break;
            case LONG:
                type = "BIGINT";
                break;
            case DATE_TIME:
                type = "timestamp";
                break;
            default:
                type = "varchar";
                break;
        }
        return String.format("CAST(\"%s\" AS %s)", name, type);
    }

    public static Optional<Type> fromColumnType(ColumnType columnType) {
        return Optional.ofNullable(columnTypeIndexing.get(columnType));
    }

    public static Optional<Type> fromSqlType(Integer sqlType) {
        return Optional.ofNullable(sqlTypeIndexing.get(sqlType));
    }
}