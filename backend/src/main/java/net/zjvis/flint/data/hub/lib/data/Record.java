package net.zjvis.flint.data.hub.lib.data;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Optional;

@Builder(toBuilder = true)
@Jacksonized
@ToString
@EqualsAndHashCode
public class Record {
    @Getter
    private Schema schema;
    @Getter
    @Singular("value")
    private List<Object> valueList;

    public Object value(int index) {
        return valueList.get(index);
    }

    public Optional<Object> value(String name) {
        int index = schema.getNameList()
                .indexOf(name);
        if (-1 == index) {
            return Optional.empty();
        }
        return Optional.ofNullable(value(index));
    }

    public List<String> nameList() {
        return schema.getNameList();
    }

    public List<Type> typeList() {
        return schema.getTypeList();
    }
}
