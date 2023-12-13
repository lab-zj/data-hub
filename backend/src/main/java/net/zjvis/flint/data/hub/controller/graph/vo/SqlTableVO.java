package net.zjvis.flint.data.hub.controller.graph.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Getter
@Schema
@Builder
public class SqlTableVO {
    @Schema(description = "Graph node sql table list")
    Map<String, List<TableInfoVO>> tableList;


}
