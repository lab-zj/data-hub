package net.zjvis.flint.data.hub.lib.calcite.func;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.apache.calcite.sql.type.SqlTypeName;

@EqualsAndHashCode
@ToString
public class FieldMeta {
    @Getter
    private final String fieldName;
    @Getter
    private final SqlTypeName sqlTypeName;

    @Builder
    @Jacksonized
    public FieldMeta(String fieldName, SqlTypeName sqlTypeName) {
        this.fieldName = fieldName;
        this.sqlTypeName = sqlTypeName;
    }
}
