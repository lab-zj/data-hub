package net.zjvis.flint.data.hub.lib.schedule.graph;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

@EqualsAndHashCode
public class EdgeSupplier implements Supplier<Edge>, Serializable {
    private static final long serialVersionUID = -5972476258152829231L;
    @EqualsAndHashCode.Exclude
    private final transient Set<String> edgeNameSet;
    private final String namePrefix;

    @Builder
    public EdgeSupplier(String namePrefix) {
        this.namePrefix = namePrefix;
        edgeNameSet = new HashSet<>();
    }

    @Override
    public Edge get() {
        String name;
        do {
            name = RandomStringUtils.randomAlphabetic(8);
        } while (edgeNameSet.contains(name));
        edgeNameSet.add(name);
        return Edge.builder()
                .name(String.format("%s%s", namePrefix, name))
                .build();
    }
}
