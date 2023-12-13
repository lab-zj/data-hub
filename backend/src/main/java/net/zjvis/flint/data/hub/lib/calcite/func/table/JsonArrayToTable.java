package net.zjvis.flint.data.hub.lib.calcite.func.table;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.lib.calcite.func.AbstractUDF2;
import net.zjvis.flint.data.hub.lib.calcite.func.FieldMeta;
import net.zjvis.flint.data.hub.lib.calcite.func.RowMeta;
import org.apache.calcite.DataContext;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.Statistic;
import org.apache.calcite.schema.Statistics;
import org.apache.calcite.sql.SqlCall;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.type.*;
import org.apache.calcite.util.ImmutableBitSet;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@EqualsAndHashCode(callSuper = true)
@ToString
public class JsonArrayToTable extends AbstractUDF2<String, String, ScannableTable> {
    public static final String NAME = "jsonArrayToTable";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Builder
    @Jacksonized
    public JsonArrayToTable() {
        super(NAME);
    }

    @Override
    public SqlReturnTypeInference returnType(RelDataTypeFactory relDataTypeFactory) {
        // TODO
        return new TableFunctionReturnTypeInference(
                null,
                null,
                false
        );
    }

    @Override
    public SqlOperandTypeInference operandType(RelDataTypeFactory relDataTypeFactory) {
        return InferTypes.explicit(ImmutableList.of(
                relDataTypeFactory.createSqlType(SqlTypeName.VARCHAR),
                relDataTypeFactory.createSqlType(SqlTypeName.VARCHAR)
        ));
    }

    @Override
    public ScannableTable eval(String dataJson, String rowMetaJson) throws JsonProcessingException {
        if (null == dataJson) {
            return null;
        }
        List<Map<String, Object>> rows = OBJECT_MAPPER.readValue(dataJson, new TypeReference<>() {
        });
        RowMeta rowMeta = OBJECT_MAPPER.readValue(rowMetaJson, new TypeReference<>() {
        });
        List<String> fieldNameList = rowMeta.getFieldMetaList()
                .stream()
                .map(FieldMeta::getFieldName)
                .collect(Collectors.toList());
        return new ScannableTable() {
            public Enumerable<@Nullable Object[]> scan(DataContext root) {
                return Linq4j.asEnumerable(rows.stream()
                        .map(row -> fieldNameList.stream()
                                .map(row::get)
                                .toArray())
                        .collect(Collectors.toList()));
            }

            public RelDataType getRowType(RelDataTypeFactory typeFactory) {
                RelDataTypeFactory.Builder builder = typeFactory.builder();
                for (FieldMeta fieldMeta : rowMeta.getFieldMetaList()) {
                    builder.add(fieldMeta.getFieldName(), fieldMeta.getSqlTypeName());
                }
                return builder.build();
            }

            public Statistic getStatistic() {
                List<FieldMeta> fieldMetaList = rowMeta.getFieldMetaList();
                List<String> primaryKeyFieldNameList = rowMeta.getPrimaryKeyFieldNameList();
                List<ImmutableBitSet> keys = IntStream.range(0, fieldMetaList.size())
                        .filter(index -> primaryKeyFieldNameList.contains(fieldMetaList.get(index).getFieldName()))
                        .mapToObj(ImmutableBitSet::of)
                        .collect(Collectors.toList());
                return Statistics.of(rows.size(), keys);
            }

            public Schema.TableType getJdbcTableType() {
                return Schema.TableType.TABLE;
            }

            public boolean isRolledUp(String column) {
                return false;
            }

            public boolean rolledUpColumnValidInsideAgg(
                    String column, SqlCall call, @Nullable SqlNode parent, @Nullable CalciteConnectionConfig config) {
                return false;
            }
        };
    }
}
