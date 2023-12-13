package net.zjvis.flint.data.hub.lib.data;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter
@ToString
@EqualsAndHashCode
public class Table {
    private final Schema schema;
    private final List<Record> recordList;

    @Builder
    @Jacksonized
    public Table(Schema schema, @Singular("record") List<Record> recordList) {
        this.schema = schema;
        this.recordList = recordList;
    }
}
