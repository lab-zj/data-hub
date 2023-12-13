package net.zjvis.flint.data.hub.service.graph.planner.conf;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.controller.graph.vo.PointVO;
import net.zjvis.flint.data.hub.entity.graph.Task;
import net.zjvis.flint.data.hub.lib.calcite.func.UserDefinedFunction;
import net.zjvis.flint.data.hub.service.graph.TaskRuntimeService;

import java.util.List;
import java.util.Map;

@Getter
@EqualsAndHashCode
@ToString
public class SqlVertexConfiguration implements Configuration {

    private final List<UserDefinedFunction> userDefinedFunctionList;
    private final List<PointVO> fromPointList;
    private final String selectSql;
    private final Map<String, List<Map<String, String>>> meta;
    private final Map<String, List<Map<String, String>>> outgoingMeta;
    private final Task task;
    private final TaskRuntimeService taskRuntimeService;
    private final boolean selected;

    @Builder
    @Jacksonized
    public SqlVertexConfiguration(
        @Singular("userDefinedFunction") List<UserDefinedFunction> userDefinedFunctionList,
        @Singular("fromPoint")
        List<PointVO> fromPointList,
        String selectSql,
        Map<String, List<Map<String, String>>> meta,
        Map<String, List<Map<String, String>>> outgoingMeta,
        Task task,
        TaskRuntimeService taskRuntimeService,
        boolean selected
    ) {
        this.fromPointList = fromPointList;
        this.userDefinedFunctionList = userDefinedFunctionList;
        this.selectSql = selectSql;
        this.meta = meta;
        this.outgoingMeta = outgoingMeta;
        this.task = task;
        this.taskRuntimeService = taskRuntimeService;
        this.selected = selected;
    }
}
