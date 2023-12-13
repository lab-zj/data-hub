package net.zjvis.flint.data.hub.service.graph.executor;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.entity.graph.Task;
import net.zjvis.flint.data.hub.lib.calcite.connector.CalciteConnector;
import net.zjvis.flint.data.hub.lib.calcite.executor.Executor;
import net.zjvis.flint.data.hub.lib.calcite.func.UserDefinedFunction;
import net.zjvis.flint.data.hub.service.executor.SqlNodeExecutor;
import net.zjvis.flint.data.hub.service.graph.TaskRuntimeService;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode
@ToString
public class SqlTaskExecutor implements TaskExecutor<Void> {

    private final Executor executor;

    @Builder
    @Jacksonized
    public SqlTaskExecutor(
        @Singular("sourceConnector")
        List<CalciteConnector> sourceConnectorList,
        @Singular("userDefinedFunction") List<UserDefinedFunction> userDefinedFunctionList,
        CalciteConnector sinkConnector,
        String sinkTableName,
        Map<String, String> outputFileInfo,
        String selectSql,
        Map<String, List<Map<String, String>>> meta,
        Task task,
        TaskRuntimeService taskRuntimeService,
        boolean selected
    ) {
        executor = SqlNodeExecutor.builder()
            .sourceConnectorList(sourceConnectorList)
            .userDefinedFunctionList(userDefinedFunctionList)
            .selectSql(selectSql)
            .sinkConnector(sinkConnector)
            .sinkTableName(sinkTableName)
            .outputFileInfo(outputFileInfo)
            .meta(meta)
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
