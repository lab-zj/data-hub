package net.zjvis.flint.data.hub.service.graph.planner.conf;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@Getter
@EqualsAndHashCode
@ToString
public class EmptyVertexConfiguration implements Configuration {
    @Builder
    @Jacksonized
    public EmptyVertexConfiguration() {
    }
}
