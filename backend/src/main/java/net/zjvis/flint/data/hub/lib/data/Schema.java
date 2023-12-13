package net.zjvis.flint.data.hub.lib.data;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Optional;

@Builder(toBuilder = true)
@Getter
@Jacksonized
@ToString
@EqualsAndHashCode
public class Schema {
    @Singular("name")
    private List<String> nameList;
    @Singular("type")
    private List<Type> typeList;

    public Type type(int index) {
        return typeList.get(index);
    }

    public String name(int index) {
        return nameList.get(index);
    }

    public Optional<Type> type(String name) {
        int index = nameList.indexOf(name);
        if (-1 == index) {
            return Optional.empty();
        }
        return Optional.ofNullable(type(index));
    }

    public int columnSize() {
        return nameList.size();
    }
}