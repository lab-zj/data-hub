package net.zjvis.flint.data.hub.entity.filesystem;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Preconditions;

@JsonTypeInfo(property = "type", use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MySqlResource.class, name = "mysql"),
        @JsonSubTypes.Type(value = PostgresqlResource.class, name = "postgresql"),
        @JsonSubTypes.Type(value = HttpResource.class, name = "http"),
})
public interface VirtualResource {
    default <T extends VirtualResource> T unwrap(Class<? extends T> clazz) {
        Preconditions.checkArgument(
                clazz.isInstance(this),
                String.format("virtualFile(class=%s) is not instance of %s", this.getClass(), clazz.getName())
        );
        return clazz.cast(this);
    }
}
