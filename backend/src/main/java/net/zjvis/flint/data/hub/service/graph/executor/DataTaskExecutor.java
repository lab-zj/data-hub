package net.zjvis.flint.data.hub.service.graph.executor;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.entity.graph.Task;
import net.zjvis.flint.data.hub.lib.calcite.executor.Executor;
import net.zjvis.flint.data.hub.service.executor.DataNodeExecutor;
import net.zjvis.flint.data.hub.service.graph.TaskRuntimeService;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode
@ToString
public class DataTaskExecutor implements TaskExecutor<Void> {

    private final Executor executor;

    @Builder
    @Jacksonized
    public DataTaskExecutor(
        Map<String, List<Map<String, String>>> meta,
        Map<String, String> outputFileInfo,
        Task task,
        TaskRuntimeService taskRuntimeService,
        boolean selected
    ) {
        executor = DataNodeExecutor.builder()
            .meta(meta)
            .outputFileInfo(outputFileInfo)
            .task(task)
            .taskRuntimeService(taskRuntimeService)
            .selected(selected)
            .build();
    }

    @Override
    public Void execute() {
        executor.execute();
        return null;
    }
}
