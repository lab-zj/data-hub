package net.zjvis.flint.data.hub.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DataTypeEnum {

    INHERIT("inherit"),
    TABLE("table"),
    CSV("csv"),

        ;

    String desc;


}
