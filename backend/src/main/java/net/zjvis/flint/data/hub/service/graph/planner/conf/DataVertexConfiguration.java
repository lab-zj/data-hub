package net.zjvis.flint.data.hub.service.graph.planner.conf;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.entity.graph.Task;
import net.zjvis.flint.data.hub.service.graph.TaskRuntimeService;

import java.util.List;
import java.util.Map;

@Getter
@EqualsAndHashCode
@ToString
public class DataVertexConfiguration implements Configuration {

    private final Map<String, List<Map<String, String>>> meta;
    private final Task task;
    private final TaskRuntimeService taskRuntimeService;
    private final boolean selected;

    @Builder
    @Jacksonized
    public DataVertexConfiguration(
        Map<String, List<Map<String, String>>> meta,
        Task task,
        TaskRuntimeService taskRuntimeService,
        boolean selected) {
        this.meta = meta;
        this.task = task;
        this.taskRuntimeService = taskRuntimeService;
        this.selected = selected;
    }

}
