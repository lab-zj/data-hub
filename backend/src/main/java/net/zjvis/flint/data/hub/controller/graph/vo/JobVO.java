package net.zjvis.flint.data.hub.controller.graph.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@Getter
@Setter
@Builder(toBuilder = true)
@Jacksonized
@ToString
@EqualsAndHashCode
@Schema
public class JobVO {
    @Schema(description = "Generated by database to identify unique graph job")
    private Long id;
    @Schema(description = "Associated graph uuid")
    private String graphUuid;
    //private String param;
    @Schema(description = "Graph job status", example = "init , start , finished, canceled, stopped")
    private String status;
    @Schema(description = "Graph job result", example = "success, failed")
    private String result;
    @Schema(description = "Graph job start time")
    private Long gmtCreate;
    @Schema(description = "Graph job update time")
    private Long gmtModify;
    @Schema(description = "Associated build info")
    private List<TaskRuntimeVO> taskRuntimeVOList;

}
