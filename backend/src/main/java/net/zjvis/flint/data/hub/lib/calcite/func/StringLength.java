package net.zjvis.flint.data.hub.lib.calcite.func;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.sql.type.*;

import java.util.Collections;

@EqualsAndHashCode(callSuper = true)
@ToString
public class StringLength extends AbstractUDF1<String, Integer> {
    public static final String NAME = "lengthOfString";

    @Builder
    @Jacksonized
    public StringLength() {
        super(NAME);
    }

    @Override
    public SqlReturnTypeInference returnType(RelDataTypeFactory relDataTypeFactory) {
        return ReturnTypes.INTEGER;
    }

    @Override
    public SqlOperandTypeInference operandType(RelDataTypeFactory relDataTypeFactory) {
        return InferTypes.explicit(Collections.singletonList(
                relDataTypeFactory.createSqlType(SqlTypeName.VARCHAR)
        ));
    }

    @Override
    public Integer eval(String str) {
        return str.length();
    }
}
