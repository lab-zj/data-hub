package net.zjvis.flint.data.hub.lib.calcite.func;

import lombok.Getter;

public abstract class AbstractUDF2<InputType1, InputType2, OutputType> implements UserDefinedFunction {
    @Getter
    private final String name;

    public AbstractUDF2(String name) {
        this.name = name;
    }

    public String methodName() {
        return "eval";
    }

    public abstract OutputType eval(InputType1 input1, InputType2 input2) throws Exception;
}
