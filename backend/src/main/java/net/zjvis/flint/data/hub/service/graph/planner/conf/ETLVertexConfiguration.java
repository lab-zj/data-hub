package net.zjvis.flint.data.hub.service.graph.planner.conf;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.controller.graph.vo.PointVO;
import net.zjvis.flint.data.hub.entity.graph.Task;
import net.zjvis.flint.data.hub.lib.calcite.func.UserDefinedFunction;
import net.zjvis.flint.data.hub.service.graph.TaskRuntimeService;

import java.util.List;
import java.util.Map;

/**
 * @author AaronY
 * @version 1.0
 * @since 2023/10/12
 */
@Getter
@EqualsAndHashCode
@ToString
public class ETLVertexConfiguration implements Configuration {

    private final List<UserDefinedFunction> userDefinedFunctionList;
    //TODO remove
    private final List<PointVO> fromPointList;
    private final Map<String, List<Map<String, String>>> meta;
    private final Map<String, List<Map<String, String>>> outgoingMeta;
    private final Task task;
    private final TaskRuntimeService taskRuntimeService;
    private final boolean selected;

    @Builder
    @Jacksonized
    public ETLVertexConfiguration(
            @Singular("userDefinedFunction") List<UserDefinedFunction> userDefinedFunctionList,
            @Singular("fromPoint") List<PointVO> fromPointList,
            Map<String, List<Map<String, String>>> meta,
            Map<String, List<Map<String, String>>> outgoingMeta,
            Task task,
            TaskRuntimeService taskRuntimeService,
            boolean selected
    ) {
        this.fromPointList = fromPointList;
        this.userDefinedFunctionList = userDefinedFunctionList;
        this.meta = meta;
        this.outgoingMeta = outgoingMeta;
        this.task = task;
        this.taskRuntimeService = taskRuntimeService;
        this.selected = selected;
    }
}
