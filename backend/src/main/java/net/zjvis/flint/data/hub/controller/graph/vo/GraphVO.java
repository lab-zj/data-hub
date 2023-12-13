package net.zjvis.flint.data.hub.controller.graph.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@ToString
@EqualsAndHashCode
@Getter
@Schema
public class GraphVO {

    @Schema(description = "Graph uuid")
    private final String graphUuid;
    @Schema(description = "Graph job task uuid list")
    private final List<String> vertexIdList;
    @Schema(description = "Graph job edge list")
    private final List<EdgeVO> edgeList;
    @Schema(description = "Graph job partially selected vertex uuid list")
    private final List<String> selectedVertexIdList;
    @Schema(description = "Current vertex id")
    private final String currentVertexId;


    @Builder
    @Jacksonized
    public GraphVO(List<String> vertexIdList, List<EdgeVO> edgeList, String graphUuid,
        List<String> selectedVertexIdList, String currentVertexId) {
        this.vertexIdList = vertexIdList;
        this.edgeList = edgeList;
        this.graphUuid = graphUuid;
        this.selectedVertexIdList = selectedVertexIdList;
        this.currentVertexId = currentVertexId;
    }
}
