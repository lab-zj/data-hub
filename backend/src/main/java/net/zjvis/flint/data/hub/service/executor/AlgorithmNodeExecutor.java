package net.zjvis.flint.data.hub.service.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.controller.filesystem.PathTransformer;
import net.zjvis.flint.data.hub.entity.filesystem.database.MysqlVirtualFile;
import net.zjvis.flint.data.hub.entity.filesystem.database.PostgresqlVirtualFile;
import net.zjvis.flint.data.hub.entity.filesystem.database.S3VirtualFile;
import net.zjvis.flint.data.hub.entity.graph.Task;
import net.zjvis.flint.data.hub.entity.graph.TaskRuntime;
import net.zjvis.flint.data.hub.lib.calcite.CalciteManager;
import net.zjvis.flint.data.hub.lib.calcite.connector.CalciteConnector;
import net.zjvis.flint.data.hub.lib.calcite.connector.algorithm.AlgorithmMysql;
import net.zjvis.flint.data.hub.lib.calcite.connector.algorithm.AlgorithmPostgresql;
import net.zjvis.flint.data.hub.lib.calcite.connector.algorithm.AlgorithmS3;
import net.zjvis.flint.data.hub.lib.calcite.executor.AbstractCalciteExecutor;
import net.zjvis.flint.data.hub.lib.calcite.func.UserDefinedFunction;
import net.zjvis.flint.data.hub.lib.minio.MinioConnection;
import net.zjvis.flint.data.hub.lib.minio.MinioManager;
import net.zjvis.flint.data.hub.service.graph.JobService;
import net.zjvis.flint.data.hub.service.graph.TaskRuntimeService;
import net.zjvis.flint.data.hub.service.graph.TaskService;
import net.zjvis.flint.data.hub.util.FileUtils;
import net.zjvis.flint.data.hub.util.JobStatusEnum;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AlgorithmNodeExecutor extends AbstractCalciteExecutor {

    private final Logger LOGGER = LoggerFactory.getLogger(AlgorithmNodeExecutor.class);
    private final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final ObjectMapper YAML_MAPPER = new ObjectMapper(
        new YAMLFactory().disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID));
    private final String PARAM_IDENTIFIER = "base64_yaml";
    private final String KEY_ALGORITHM_SUCCESS = "success";
    private final String KEY_ALGORITHM_RESULT = "result";
    private final String KEY_ALGORITHM_MESSAGE = "message";
    private final String sinkTableName;
    private final Map<String, List<Map<String, String>>> meta;
    private final Map<String, List<Map<String, String>>> outgoingMeta;
    private final Task task;
    private final TaskRuntimeService taskRuntimeService;
    private final TaskService taskService;
    private final JobService jobService;
    private final boolean selected;
    private final OkHttpClient okHttpClient;
    private final String algorithmServerAddress;
    private final String algorithmConfiguration;
    private final PathTransformer pathTransformer;
    private final Long userId;
    private final MinioConnection minioConnection;
    private final String bucketName;

    @Builder
    @Jacksonized
    public AlgorithmNodeExecutor(
        @Singular("sourceConnector")
        List<CalciteConnector> sourceConnectorList,
        @Singular("userDefinedFunction")
        List<UserDefinedFunction> userDefinedFunctionList,
        CalciteConnector sinkConnector,
        String sinkTableName,
        OkHttpClient okHttpClient,
        Map<String, List<Map<String, String>>> meta,
        Map<String, List<Map<String, String>>> outgoingMeta,
        Task task,
        TaskRuntimeService taskRuntimeService,
        TaskService taskService,
        JobService jobService,
        boolean selected,
        String algorithmServerAddress,
        String algorithmConfiguration,
        PathTransformer pathTransformer,
        Long userId,
        MinioConnection minioConnection,
        String bucketName) {
        super(sourceConnectorList, userDefinedFunctionList, sinkConnector);
        this.sinkTableName = sinkTableName;
        this.okHttpClient = okHttpClient;
        this.meta = meta;
        this.outgoingMeta = outgoingMeta;
        this.task = task;
        this.taskRuntimeService = taskRuntimeService;
        this.taskService = taskService;
        this.jobService = jobService;
        this.selected = selected;
        this.algorithmServerAddress = algorithmServerAddress;
        this.algorithmConfiguration = algorithmConfiguration;
        this.pathTransformer = pathTransformer;
        this.userId = userId;
        this.minioConnection = minioConnection;
        this.bucketName = bucketName;
    }


    @Override
    protected Object[] operands() {
        return new String[]{sinkTableName};
    }

    @Override
    protected void doExecute(CalciteManager calciteManager, Consumer<ResultSet> consumer) {
        String taskUuid = task.getTaskUuid();
        try {
            LOGGER.info("[AlgorithmNodeExecutor][{}] start, meta={}, outgoingMeta={}", taskUuid,
                OBJECT_MAPPER.writeValueAsString(meta),
                OBJECT_MAPPER.writeValueAsString(outgoingMeta));
            if (!selected || taskRuntimeService.isTaskCanceled(taskUuid)
                || taskRuntimeService.isTaskStopped(taskUuid)) {
                LOGGER.info("[AlgorithmNodeExecutor][{}] no need to execute", taskUuid);
                return;
            }
            taskRuntimeService.updateBuild(taskUuid, meta, JobStatusEnum.START.getName(), null,
                null, null);

            // TODO: merge configuration and param
            Response response = invoke(
                mergeConfigurationAndParam(algorithmConfiguration, meta, outgoingMeta),
                algorithmServerAddress);
            String result = response.body().string();
            LOGGER.info("[AlgorithmNodeExecutor][{}] invoke finished, result={}", taskUuid, result);
            Map<String, Object> resultMap = OBJECT_MAPPER.readValue(result,
                new TypeReference<>() {
                });
            boolean invokeSuccess = (boolean) resultMap.get(KEY_ALGORITHM_SUCCESS);
            taskRuntimeService.updateBuild(taskUuid, meta, JobStatusEnum.FINISHED.getName(),
                invokeSuccess ? JobStatusEnum.SUCCESS.getName() : JobStatusEnum.FAILED.getName(),
                // TODO: only consider one output for now
                OBJECT_MAPPER.writeValueAsString(
                    List.of(Map.of("default", getOutputConnector(outgoingMeta)))),
                (String) resultMap.get(KEY_ALGORITHM_MESSAGE));
            if (!invokeSuccess) {
                LOGGER.info("[AlgorithmNodeExecutor][{}] invoke failed, update job status",
                    taskUuid);
                updateJobStatusWithFailed();
                // throw exception to stop later node executor
                throw new RuntimeException(
                    String.format("algorithm executed failed, taskUuid=%s", taskUuid));
            }
        } catch (Exception e) {
            LOGGER.error("[AlgorithmNodeExecutor][{}] failed, exception={}", taskUuid,
                e.toString());
            try {
                taskRuntimeService.updateBuild(taskUuid, meta, JobStatusEnum.FINISHED.getName(),
                    JobStatusEnum.FAILED.getName(),
                    null, e.getMessage());
                updateJobStatusWithFailed();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }

    private void updateJobStatus() {
        try {
            LOGGER.info(
                "[updateJobStatus] algorithm node executor failed, update job status, taskUuid={}",
                task.getTaskUuid());
            Optional<TaskRuntime> buildOptional = taskRuntimeService.findByTaskUuid(
                task.getTaskUuid());
            if (buildOptional.isEmpty()) {
                return;
            }
            jobService.checkJob(buildOptional.get().getJobId());
        } catch (Exception e) {
            LOGGER.error("[updateJobStatus] failed, taskUuid={}, exception={}", task.getTaskUuid(),
                e.toString());
        }
    }

    private void updateJobStatusWithFailed() throws Exception {
        Optional<TaskRuntime> buildOptional = taskRuntimeService.findByTaskUuid(
            task.getTaskUuid());
        if (buildOptional.isEmpty()) {
            return;
        }
        LOGGER.error("[updateJobStatusWithFailed] start, taskUuid={}", task.getTaskUuid());
        jobService.updateJob(buildOptional.get().getJobId(), JobStatusEnum.FINISHED.getName(),
            JobStatusEnum.FAILED.getName());
    }

    // TODO: Only consider one output connector for now
    private String getOutputConnector(Map<String, List<Map<String, String>>> outgoingParamListMap) {
        String outputConnector = null;
        for (Map.Entry<String, List<Map<String, String>>> paramMaps : outgoingParamListMap.entrySet()) {
            Task outgoingTask = taskService.findByTaskUuid(paramMaps.getKey()).orElse(null);
            outputConnector =
                outgoingTask == null ? null : outgoingTask.getConfigurationReference();
        }
        return outputConnector;
    }

    private Response invoke(String configuration, String algorithmServer) {
        LOGGER.info("[AlgorithmNodeExecutor]Algorithm server={}, configuration={}", algorithmServer,
            configuration);
        String base64Result = Base64.getEncoder().encodeToString(configuration.getBytes(
            StandardCharsets.UTF_8));
        LOGGER.info("[AlgorithmNodeExecutor] base64Result={}", base64Result);
        Request request = new Request.Builder()
            .url(algorithmServer)
            .post(
                RequestBody.create(
                    YAML_MAPPER.createObjectNode().put(PARAM_IDENTIFIER, base64Result).toString(),
                    MediaType.parse(org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
                )
            )
            .build();
        Call call = okHttpClient.newCall(request);
        try {
            return call.execute();
        } catch (IOException ioException) {
            LOGGER.error("[AlgorithmNodeExecutor] invoke failed, error={}",
                ioException.getMessage());
            throw new RuntimeException(ioException.getMessage());
        }
    }

    private String mergeConfigurationAndParam(String configuration,
        Map<String, List<Map<String, String>>> paramListMap,
        Map<String, List<Map<String, String>>> outgoingParamListMap
    )
        throws Exception {
        if (paramListMap == null || paramListMap.isEmpty()) {
            return configuration;
        }
        Map<String, Object> configurationMap = YAML_MAPPER.readValue(configuration,
            new TypeReference<>() {
            });
        Map<String, List<Map<String, String>>> connectorMapList =
            (Map<String, List<Map<String, String>>>) configurationMap.get("connector");
        if (connectorMapList == null || connectorMapList.isEmpty()) {
            LOGGER.info(
                "[mergeConfigurationAndParam] no connector found in configuration, configuration={}",
                configuration);
            return configuration;
        }
        List<Map<String, String>> inputConnectorMapList = connectorMapList.get("input");
        List<Map<String, String>> outputConnectorMapList = connectorMapList.get("output");

        handleInputConnector(inputConnectorMapList, paramListMap);
        LOGGER.info(
            "[mergeConfigurationAndParam] handle input connector finished, connectorMapList={}",
            OBJECT_MAPPER.writeValueAsString(connectorMapList));
        handleOutputConnector(outputConnectorMapList, outgoingParamListMap);
        LOGGER.info(
            "[mergeConfigurationAndParam] handle output connector finished, connectorMapList={}",
            OBJECT_MAPPER.writeValueAsString(connectorMapList));
        return
            YAML_MAPPER.writeValueAsString(configurationMap);
    }

    private void handleInputConnector(List<Map<String, String>> inputConnectorMapList,
        Map<String, List<Map<String, String>>> paramListMap) throws Exception {
        LOGGER.info("[handleInputConnector] start, inputConnectorMapList={}, paramListMap={}",
            OBJECT_MAPPER.writeValueAsString(inputConnectorMapList),
            OBJECT_MAPPER.writeValueAsString(paramListMap));
        for (Map.Entry<String, List<Map<String, String>>> paramMaps : paramListMap.entrySet()) {
            TaskRuntime taskRuntime = taskRuntimeService.findByTaskUuid(paramMaps.getKey())
                .orElseThrow(
                    () -> new Exception(
                        String.format(
                            "[handleInputConnector] cannot find task runtime by uuid=%s",
                            paramMaps.getKey()))
                );
            List<Map<String, String>> dataList = OBJECT_MAPPER.readValue(taskRuntime.getData(),
                new TypeReference<>() {
                });
            for (Map<String, String> paramMap : paramMaps.getValue()) {
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    dataList.forEach(
                        dataMap -> {
                            if (dataMap.containsKey(entry.getKey())) {
                                Map<String, String> inputMap;
                                try {
                                    inputMap = YAML_MAPPER.readValue(
                                        getDataFileContent(dataMap.get(entry.getKey())),
                                        new TypeReference<>() {
                                        });
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                                LOGGER.info("[handleInputConnector] get inputMap={}", inputMap);
                                inputConnectorMapList.set(0, inputMap);
                            }
                        }
                    );
                }
            }
        }
    }

    private void handleOutputConnector(List<Map<String, String>> outputConnectorMapList,
        Map<String, List<Map<String, String>>> outgoingParamListMap)
        throws Exception {
        LOGGER.info(
            "[handleOutputConnector] start, outputConnectorMapList={}, outgoingParamListMap={}",
            OBJECT_MAPPER.writeValueAsString(outputConnectorMapList),
            OBJECT_MAPPER.writeValueAsString(outgoingParamListMap));
        for (Map.Entry<String, List<Map<String, String>>> paramMaps : outgoingParamListMap.entrySet()) {
            Task outgoingTask = taskService.findByTaskUuid(paramMaps.getKey()).orElseThrow(
                () -> new Exception(
                    String.format(
                        "[handleOutputConnector] cannot find task by uuid=%s",
                        paramMaps.getKey()))
            );
            if (StringUtils.isEmpty(outgoingTask.getConfigurationReference())) {
                LOGGER.error(
                    "[handleOutputConnector] task configurationReference is empty, outgoingTask={}",
                    OBJECT_MAPPER.writeValueAsString(outgoingTask));
                return;
            }
            Map<String, String> outputMap;
            try {
                outputMap = YAML_MAPPER.readValue(
                    getDataFileContent(outgoingTask.getConfigurationReference()),
                    new TypeReference<>() {
                    });
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            LOGGER.info("[handleOutputConnector] get outputMap={}", outputMap);
            outputConnectorMapList.set(0, outputMap);
        }
    }

    private String getDataFileContent(String fileName) {
        LOGGER.info("[getDataFileContent] fileName={}", fileName);
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        String inputObjectKey = pathTransformer.userIndependentPath(userId, fileName);
        try {
            MinioManager minioManager = MinioManager.builder()
                .minioConnection(minioConnection)
                .build();
            String content = FileUtils.getMinioFileContent(minioManager, bucketName,
                inputObjectKey);
            switch (suffix) {
                case "my": {
                    MysqlVirtualFile mysqlVirtualFile = OBJECT_MAPPER.readValue(content,
                        new TypeReference<>() {
                        });

                    AlgorithmMysql algorithmMysql = AlgorithmMysql.builder()
                        .host(mysqlVirtualFile.getHost())
                        .port(Integer.valueOf(mysqlVirtualFile.getPort()))
                        .username(mysqlVirtualFile.getUsername())
                        .password(mysqlVirtualFile.getPassword())
                        .databaseName(mysqlVirtualFile.getDatabaseName())
                        .build();
                    return YAML_MAPPER.writeValueAsString(algorithmMysql);
                }
                case "ps": {
                    PostgresqlVirtualFile postgresqlVirtualFile = OBJECT_MAPPER.readValue(content,
                        new TypeReference<>() {
                        });

                    AlgorithmPostgresql algorithmPostgresql = AlgorithmPostgresql.builder()
                        .host(postgresqlVirtualFile.getHost())
                        .port(Integer.valueOf(postgresqlVirtualFile.getPort()))
                        .username(postgresqlVirtualFile.getUsername())
                        .password(postgresqlVirtualFile.getPassword())
                        .databaseName(postgresqlVirtualFile.getDatabaseName())
                        .schemaName(postgresqlVirtualFile.getSchemaName())
                        .ifWriteTableExists("replace")
                        .build();
                    return YAML_MAPPER.writeValueAsString(algorithmPostgresql);
                }
                case "s3": {
                    S3VirtualFile s3VirtualFile = OBJECT_MAPPER.readValue(content,
                        new TypeReference<>() {
                        });

                    AlgorithmS3 algorithmS3 = AlgorithmS3.builder()
                        .bucket(s3VirtualFile.getBucket())
                        .endpoint(s3VirtualFile.getNamedStreamResources().getEndpoint())
                        .accessKey(s3VirtualFile.getNamedStreamResources().getAccessKey())
                        .accessSecret(s3VirtualFile.getNamedStreamResources().getAccessSecret())
                        .build();
                    return YAML_MAPPER.writeValueAsString(algorithmS3);
                }
                default: {
                    String errorMsg = String.format("cannot support file type=%s", suffix);
                    LOGGER.error("[getDataFileContent] failed, error={}", errorMsg);
                    throw new Exception(errorMsg);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: only test for algorithm ocr, will be optimized and removed later
    private String mergeConfigurationAndParamForOCR(String configuration,
        Map<String, List<Map<String, String>>> paramListMap)
        throws Exception {
        if (paramListMap == null || paramListMap.isEmpty()) {
            return configuration;
        }
        Map<String, Object> configurationMap = YAML_MAPPER.readValue(configuration,
            new TypeReference<>() {
            });
        List<Map<String, String>> cacheMapList =
            (List<Map<String, String>>) configurationMap.get("cache");
        if (cacheMapList == null || cacheMapList.isEmpty()) {
            LOGGER.info(
                "[mergeConfigurationAndParamForOCR] no cache found in configuration, configuration={}",
                configuration);
            return configuration;
        }
        Map<String, String> cacheMap = cacheMapList.get(0);

        for (Map.Entry<String, List<Map<String, String>>> paramMaps : paramListMap.entrySet()) {
            TaskRuntime taskRuntime = taskRuntimeService.findByTaskUuid(paramMaps.getKey())
                .orElseThrow(
                    () -> new Exception(
                        String.format(
                            "[mergeConfigurationAndParamForOCR] cannot find task runtime by uuid=%s",
                            paramMaps.getKey()))
                );
            List<Map<String, String>> dataList = OBJECT_MAPPER.readValue(taskRuntime.getData(),
                new TypeReference<>() {
                });
            for (Map<String, String> paramMap : paramMaps.getValue()) {
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    if (cacheMap.containsKey(entry.getValue())) {
                        dataList.forEach(
                            dataMap -> {
                                if (dataMap.containsKey(entry.getKey())) {
                                    cacheMap.put(entry.getValue(), dataMap.get(entry.getKey()));
                                }
                            }
                        );

                    }
                }
            }
        }
        return
            YAML_MAPPER.writeValueAsString(configurationMap);
    }
}
