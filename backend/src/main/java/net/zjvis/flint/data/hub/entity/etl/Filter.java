package net.zjvis.flint.data.hub.entity.etl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.util.DataTypeEnum;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.tools.RelBuilder;
import org.apache.calcite.util.TimestampString;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author AaronY
 * @version 1.0
 * @since 2022/12/7
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Filter extends BaseETL implements ETL {


    private List<String> projectList;

    @JsonProperty("conditions")

    private List<Condition> conditions;

    @Builder
    @Jacksonized
    public Filter(Params params,
                  @Singular("project") List<String> projectList,
                  @Singular("condition") List<Condition> conditions) {
        super(params);
        this.projectList = projectList;
        this.conditions = conditions;
    }


    @Override
    public Set<DataTypeEnum> acceptableType() {
        return Set.of(DataTypeEnum.TABLE, DataTypeEnum.CSV);
    }

    @Override
    public boolean isValid() {
        return this.isParamValid();
    }

    @Override
    public Function<RelBuilder, RelNode> apply(List<Pair<String, String>> inputInfo) {
        return relBuilder -> {
            inputInfo.forEach(pair -> relBuilder.scan(pair.getKey(), pair.getValue()));

            List<RexNode> filter = this.getConditions().stream().map(condition -> {
                Preconditions.checkState(!condition.getDimensions().isEmpty(), "Filter Dimension Setting is invalid.");
                return condition.parse(relBuilder);
            }).collect(Collectors.toList());

            relBuilder.filter(filter);

            relBuilder.project(this.getProjectList().stream().map(relBuilder::field).toArray(RexNode[]::new));
            return relBuilder.build();
        };
    }



    @Builder
    @Getter
    @Setter
    @Jacksonized
    @EqualsAndHashCode
    public static class Condition {
        @JsonProperty("dimension")
        @Singular("dimension")
        List<Dimension> dimensions;

        protected RexNode parse(RelBuilder relBuilder) {
            if (dimensions.size() > 1) {
                return relBuilder.or(
                        dimensions.stream()
                                .map(dimension -> wrapDimension(relBuilder, dimension)
                                ).toArray(RexNode[]::new));

            } else if (dimensions.size() == 1) {
                return wrapDimension(relBuilder, dimensions.get(0));
            }
            return null;
        }

        private static RexNode wrapDimension(RelBuilder relBuilder, Dimension dimension) {
            if (ObjectUtils.isNotEmpty(dimension.getValue())) {
                return relBuilder.call(
                        dimension.getOperator().toSqlStdOperator(),
                        relBuilder.field(dimension.getName()),
                        wrapValue(relBuilder, dimension)
                );
            } else {
                return relBuilder.call(
                        dimension.getOperator().toSqlStdOperator(),
                        relBuilder.field(dimension.getName())
                );
            }
        }

        private static boolean isDate(RexInputRef inputRef) {
            SqlTypeName sqlTypeName = inputRef.getType().getSqlTypeName();
            return sqlTypeName.equals(SqlTypeName.TIMESTAMP);
        }

        private static RexLiteral wrapValue(RelBuilder relBuilder, Dimension dimension) {
            if (null == dimension.getValue()) {
                return relBuilder.getRexBuilder().makeNullLiteral(relBuilder.field(dimension.getName()).getType());
            } else {
                return isDate(relBuilder.field(dimension.getName()))
                        ? relBuilder.getRexBuilder().makeTimestampLiteral(
                        TimestampString.fromMillisSinceEpoch(((Timestamp) dimension.getValue()).getTime()), 1)
                        : relBuilder.literal(dimension.getValue());
            }
        }


    }

    @Builder
    @Getter
    @Setter
    @Jacksonized
    @EqualsAndHashCode
    public static class Dimension {
        @Builder.Default
        private Integer index = 0;
        private String name;
        private SqlOperator operator;
        private Object value;

        private Object getCheckedValue() {
//            public RexLiteral literal(@Nullable Object value) {
//                final RexBuilder rexBuilder = cluster.getRexBuilder();
//                if (value == null) {
//                    final RelDataType type = getTypeFactory().createSqlType(SqlTypeName.NULL);
//                    return rexBuilder.makeNullLiteral(type);
//                } else if (value instanceof Boolean) {
//                    return rexBuilder.makeLiteral((Boolean) value);
//                } else if (value instanceof BigDecimal) {
//                    return rexBuilder.makeExactLiteral((BigDecimal) value);
//                } else if (value instanceof Float || value instanceof Double) {
//                    return rexBuilder.makeApproxLiteral(
//                            BigDecimal.valueOf(((Number) value).doubleValue()));
//                } else if (value instanceof Number) {
//                    return rexBuilder.makeExactLiteral(
//                            BigDecimal.valueOf(((Number) value).longValue()));
//                } else if (value instanceof String) {
//                    return rexBuilder.makeLiteral((String) value);
//                } else if (value instanceof Enum) {
//                    return rexBuilder.makeLiteral(value,
//                            getTypeFactory().createSqlType(SqlTypeName.SYMBOL));
//                } else {
//                    throw new IllegalArgumentException("cannot convert " + value
//                            + " (" + value.getClass() + ") to a constant");
//                }
//            }
            if (value instanceof Timestamp) {
                return ((Timestamp) value).getTime();
            } else {
                return this.value;
            }
        }
    }

}
