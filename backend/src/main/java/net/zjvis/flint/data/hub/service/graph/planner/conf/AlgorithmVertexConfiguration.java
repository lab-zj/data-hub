package net.zjvis.flint.data.hub.service.graph.planner.conf;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.controller.graph.vo.PointVO;
import net.zjvis.flint.data.hub.entity.graph.Task;
import net.zjvis.flint.data.hub.lib.calcite.func.UserDefinedFunction;
import net.zjvis.flint.data.hub.service.graph.JobService;
import net.zjvis.flint.data.hub.service.graph.TaskRuntimeService;
import net.zjvis.flint.data.hub.service.graph.TaskService;
import okhttp3.OkHttpClient;

@Getter
@EqualsAndHashCode
@ToString
public class AlgorithmVertexConfiguration implements Configuration {

    private final List<UserDefinedFunction> userDefinedFunctionList;
    private final List<PointVO> fromPointList;
    private final OkHttpClient okHttpClient;
    private final String algorithmServerAddress;
    private final String algorithmConfiguration;
    private final Map<String, List<Map<String, String>>> meta;
    private final Map<String, List<Map<String, String>>> outgoingMeta;
    private final Task task;
    private final TaskRuntimeService taskRuntimeService;
    private final TaskService taskService;
    private final JobService jobService;
    private final boolean selected;

    @Builder
    @Jacksonized
    public AlgorithmVertexConfiguration(
        @Singular("userDefinedFunction") List<UserDefinedFunction> userDefinedFunctionList,
        @Singular("fromPoint")
        List<PointVO> fromPointList,
        OkHttpClient okHttpClient,
        String algorithmServerAddress,
        String algorithmConfiguration,
        Map<String, List<Map<String, String>>> meta,
        Map<String, List<Map<String, String>>> outgoingMeta,
        Task task,
        TaskRuntimeService taskRuntimeService,
        TaskService taskService,
        JobService jobService,
        boolean selected
    ) {
        this.fromPointList = fromPointList;
        this.userDefinedFunctionList = userDefinedFunctionList;
        this.okHttpClient = okHttpClient;
        this.algorithmServerAddress = algorithmServerAddress;
        this.algorithmConfiguration = algorithmConfiguration;
        this.meta = meta;
        this.outgoingMeta = outgoingMeta;
        this.task = task;
        this.taskRuntimeService = taskRuntimeService;
        this.taskService = taskService;
        this.jobService = jobService;
        this.selected = selected;
    }
}
