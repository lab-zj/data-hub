package net.zjvis.flint.data.hub.controller.filesystem.vo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.lib.data.Type;


@Getter
@ToString
@EqualsAndHashCode
public class ColumnVO {
    private final String name;
    private final Type type;

    @Builder
    @Jacksonized
    public ColumnVO(String name, Type type) {
        this.name = name;
        this.type = type;
    }
}
