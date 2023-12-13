package net.zjvis.flint.data.hub.lib.calcite.func;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.Function;
import org.apache.calcite.schema.impl.AggregateFunctionImpl;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;
import org.apache.calcite.schema.impl.TableFunctionImpl;
import org.apache.calcite.sql.type.SqlOperandTypeInference;
import org.apache.calcite.sql.type.SqlReturnTypeInference;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface UserDefinedFunction extends Serializable {
    public enum Type {
        SCALAR,
        TABLE,
        AGGREGATE,
    }

    String getName();

    String methodName();

    SqlReturnTypeInference returnType(RelDataTypeFactory relDataTypeFactory);

    SqlOperandTypeInference operandType(RelDataTypeFactory relDataTypeFactory);

    default Type type() {
        return Type.SCALAR;
    }

    default Function createFunction() {
        Type type = type();
        switch (type) {
            case SCALAR:
                return ScalarFunctionImpl.create(getClass(), methodName());
            case TABLE:
                return TableFunctionImpl.create(getClass(), methodName());
            case AGGREGATE:
                return AggregateFunctionImpl.create(getClass());
            default:
                throw new UnsupportedOperationException(String.format("Unsupported function type(%s)", type));
        }
    }
}
