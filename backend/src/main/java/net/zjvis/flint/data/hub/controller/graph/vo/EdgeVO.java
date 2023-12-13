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
public class EdgeVO {

    @Schema(description = "The start vertex of edge")
    PointVO from;
    @Schema(description = "The end vertex of edge")
    PointVO to;
}
