package net.zjvis.flint.data.hub.controller.graph.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Getter
@Setter
@Builder
@Jacksonized
@ToString
@EqualsAndHashCode
@Schema
public class PointVO {

    @Schema(description = "The vertex id(equals task uuid)")
    String uuid;
    @Schema(description = "The param key of output param list")
    String param;
}
