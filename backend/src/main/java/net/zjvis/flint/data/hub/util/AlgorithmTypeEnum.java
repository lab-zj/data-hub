package net.zjvis.flint.data.hub.util;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum AlgorithmTypeEnum {
    CUSTOM("custom"),
    BUILTIN("built-in"),
    ;

    private String name;

    AlgorithmTypeEnum(String name) {
        this.name = name;
    }
}
