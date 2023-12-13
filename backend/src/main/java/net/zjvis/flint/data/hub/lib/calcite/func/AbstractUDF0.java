package net.zjvis.flint.data.hub.lib.calcite.func;

import lombok.Getter;

public abstract class AbstractUDF0<OutputType> implements UserDefinedFunction {
    @Getter
    private final String name;

    public AbstractUDF0(String name) {
        this.name = name;
    }

    public String methodName() {
        return "eval";
    }

    public abstract OutputType eval() throws Exception;
}
