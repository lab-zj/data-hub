package net.zjvis.flint.data.hub.service.graph.executor;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.controller.filesystem.PathTransformer;
import net.zjvis.flint.data.hub.entity.graph.Task;
import net.zjvis.flint.data.hub.lib.calcite.connector.CalciteConnector;
import net.zjvis.flint.data.hub.lib.calcite.executor.Executor;
import net.zjvis.flint.data.hub.lib.calcite.func.UserDefinedFunction;
import net.zjvis.flint.data.hub.lib.minio.MinioConnection;
import net.zjvis.flint.data.hub.service.executor.AlgorithmNodeExecutor;
import net.zjvis.flint.data.hub.service.graph.JobService;
import net.zjvis.flint.data.hub.service.graph.TaskRuntimeService;
import net.zjvis.flint.data.hub.service.graph.TaskService;
import okhttp3.OkHttpClient;

@EqualsAndHashCode
@ToString
public class AlgorithmTaskExecutor implements TaskExecutor<Void> {

    private final Executor executor;

    @Builder
    @Jacksonized
    public AlgorithmTaskExecutor(
        @Singular("sourceConnector")
        List<CalciteConnector> sourceConnectorList,
        @Singular("userDefinedFunction") List<UserDefinedFunction> userDefinedFunctionList,
        CalciteConnector sinkConnector,
        String sinkTableName,
        OkHttpClient okHttpClient,
        String algorithmServerAddress,
        String algorithmConfiguration,
        Map<String, List<Map<String, String>>> meta,
        Map<String, List<Map<String, String>>> outgoingMeta,
        Task task,
        TaskRuntimeService taskRuntimeService,
        TaskService taskService,
        JobService jobService,
        boolean selected,
        PathTransformer pathTransformer,
        Long userId,
        MinioConnection minioConnection,
        String bucketName

    ) {
        executor = AlgorithmNodeExecutor.builder()
            .sourceConnectorList(sourceConnectorList)
            .userDefinedFunctionList(userDefinedFunctionList)
            .okHttpClient(okHttpClient)
            .algorithmServerAddress(algorithmServerAddress)
            .algorithmConfiguration(algorithmConfiguration)
            .sinkConnector(sinkConnector)
            .sinkTableName(sinkTableName)
            .meta(meta)
            .outgoingMeta(outgoingMeta)
            .task(task)
            .taskRuntimeService(taskRuntimeService)
            .taskService(taskService)
            .jobService(jobService)
            .selected(selected)
            .pathTransformer(pathTransformer)
            .userId(userId)
            .minioConnection(minioConnection)
            .bucketName(bucketName)
            .build();
    }

    @Override
    public Void execute() {
        executor.execute();
        return null;
    }
}
