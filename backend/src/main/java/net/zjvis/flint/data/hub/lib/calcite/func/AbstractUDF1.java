package net.zjvis.flint.data.hub.lib.calcite.func;

import lombok.Getter;

public abstract class AbstractUDF1<InputType, OutputType> implements UserDefinedFunction {
    @Getter
    private final String name;

    public AbstractUDF1(String name) {
        this.name = name;
    }

    public String methodName() {
        return "eval";
    }

    public abstract OutputType eval(InputType input) throws Exception;
}
