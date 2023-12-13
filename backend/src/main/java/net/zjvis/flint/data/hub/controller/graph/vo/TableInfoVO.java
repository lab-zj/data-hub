package net.zjvis.flint.data.hub.controller.graph.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Getter
@Schema
@Builder
public class TableInfoVO {
    @Schema(description = "Sql table info: showName")
    String showName;
    @Schema(description = "Sql table info: value")
    String value;
}
