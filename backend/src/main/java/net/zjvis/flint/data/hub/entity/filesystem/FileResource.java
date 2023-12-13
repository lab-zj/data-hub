package net.zjvis.flint.data.hub.entity.filesystem;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(property = "@class", use = JsonTypeInfo.Id.CLASS)
public interface FileResource {
}
