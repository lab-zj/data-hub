package net.zjvis.flint.data.hub.controller.graph.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Getter
@Setter
@Builder(toBuilder = true)
@Jacksonized
@ToString
@EqualsAndHashCode
@Schema(description = "Node info")
public class TaskNodeInfoVO {
    @Schema(description = "Related task id")
    private Long taskId;
    @Schema(description = "Related task uuid")
    private String taskUuid;
    @Schema(description = "Data node type (csv、txt、png、mp4、unknown)")
    private String type;
    @Schema(description = "Data node directory flag (true / false)")
    private boolean directory;
    @Schema(description = "Data node file path")
    private String path;
    @Schema(description = "Data node file name")
    private String name;
    @Schema(description = "Algorithm node input config param info")
    private String inputParamTemplate;
    @Schema(description = "Algorithm node output config param info")
    private String outputParamTemplate;
    @Schema(description = "Sql node info")
    private String sql;
    @Schema(description = "Algorithm node info")
    private String algorithm;
}
