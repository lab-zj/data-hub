package net.zjvis.flint.data.hub.lib.calcite.func;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@EqualsAndHashCode
@ToString
public class RowMeta {
    @Getter
    private final List<String> primaryKeyFieldNameList;
    @Getter
    private final List<FieldMeta> fieldMetaList;

    @Builder
    @Jacksonized
    public RowMeta(
            @Singular("primaryKeyFieldName") List<String> primaryKeyFieldNameList,
            @Singular("fieldMeta") List<FieldMeta> fieldMetaList
    ) {
        this.primaryKeyFieldNameList = primaryKeyFieldNameList;
        this.fieldMetaList = fieldMetaList;
    }
}
