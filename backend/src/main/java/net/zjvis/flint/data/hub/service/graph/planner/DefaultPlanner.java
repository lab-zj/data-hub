package net.zjvis.flint.data.hub.service.graph.planner;

import static net.zjvis.flint.data.hub.util.Constant.OUTPUT_FILE_MAP_KEY;
import static net.zjvis.flint.data.hub.util.Constant.OUTPUT_PRE_TASK_UUID;
import static net.zjvis.flint.data.hub.util.Constant.OUTPUT_TABLE_MAP_KEY;
import static net.zjvis.flint.data.hub.util.Constant.OUTPUT_VIRTUAL_FLAG;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import net.zjvis.flint.data.hub.controller.filesystem.PathTransformer;
import net.zjvis.flint.data.hub.entity.filesystem.database.MysqlVirtualFile;
import net.zjvis.flint.data.hub.entity.filesystem.database.PostgresqlVirtualFile;
import net.zjvis.flint.data.hub.entity.filesystem.database.S3VirtualFile;
import net.zjvis.flint.data.hub.entity.graph.Task;
import net.zjvis.flint.data.hub.entity.graph.TaskRuntime;
import net.zjvis.flint.data.hub.exception.NotSupportException;
import net.zjvis.flint.data.hub.lib.calcite.connector.CalciteConnector;
import net.zjvis.flint.data.hub.lib.calcite.connector.csv.CsvS3;
import net.zjvis.flint.data.hub.lib.calcite.connector.jdbc.Mysql;
import net.zjvis.flint.data.hub.lib.calcite.connector.jdbc.Postgresql;
import net.zjvis.flint.data.hub.lib.calcite.func.StringLength;
import net.zjvis.flint.data.hub.lib.calcite.resource.S3Resource;
import net.zjvis.flint.data.hub.lib.minio.MinioConnection;
import net.zjvis.flint.data.hub.lib.minio.MinioManager;
import net.zjvis.flint.data.hub.service.account.AccountUtils;
import net.zjvis.flint.data.hub.service.graph.TableMapService;
import net.zjvis.flint.data.hub.service.graph.TaskRuntimeService;
import net.zjvis.flint.data.hub.service.graph.TaskService;
import net.zjvis.flint.data.hub.service.graph.executor.*;
import net.zjvis.flint.data.hub.service.graph.planner.conf.*;
import okhttp3.OkHttpClient;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

@Builder
public class DefaultPlanner implements Planner<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPlanner.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectMapper YAML_OBJECT_MAPPER = new ObjectMapper(
        new YAMLFactory().disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID));

    private final TaskRuntimeService taskRuntimeService;
    private final TaskService taskService;
    private final MinioConnection minioConnection;
    private final TableMapService tableMapService;
    private final AccountUtils accountUtils;
    private final String bucketName;
    private final OkHttpClient okHttpClient;
    private final PathTransformer pathTransformer;

    @Override
    public TaskExecutor<Void> executor(Configuration configuration) throws Exception {
        LOGGER.info("[executor] start, configuration={}",
            OBJECT_MAPPER.writeValueAsString(configuration));
        String userId = accountUtils.currentUserIdOptional().orElseThrow(
            () -> new Exception("User id is empty")
        );
        // TODO responsibility chain
        if (configuration instanceof SqlVertexConfiguration) {
            SqlVertexConfiguration sqlVertexConfiguration = (SqlVertexConfiguration) configuration;
            // TODO: output table name is unique for all source connector? && how to do if table exist
            String outputTableName = "output_" + TableMapService.getActiveTableNameCommon(
                sqlVertexConfiguration.getTask().getId());
            List<CalciteConnector> connectorList = getSourceConnector(
                sqlVertexConfiguration.getMeta());
            Map<String, String> outputFileInfo = new HashMap<>();
            return SqlTaskExecutor.builder()
                .meta(sqlVertexConfiguration.getMeta())
                .task(sqlVertexConfiguration.getTask())
                .taskRuntimeService(sqlVertexConfiguration.getTaskRuntimeService())
                // TODO: Test-demo: with this UDF
                .userDefinedFunction(StringLength.builder().build())
                //.userDefinedFunctionList((sqlVertexConfiguration).getUserDefinedFunctionList())
                .sourceConnectorList(connectorList)
                .selectSql(sqlVertexConfiguration.getSelectSql())
                // TODO: how to set sinkConnector with multiple input
                .sinkConnector(
                    getOutputConnector(outputTableName, sqlVertexConfiguration.getOutgoingMeta(),
                        outputFileInfo))
                .sinkTableName(outputTableName)
                .outputFileInfo(outputFileInfo)
                .selected(sqlVertexConfiguration.isSelected())
                .build();
        } else if (configuration instanceof DataVertexConfiguration) {
            DataVertexConfiguration dataVertexConfiguration = (DataVertexConfiguration) configuration;
            return DataTaskExecutor.builder()
                .meta(dataVertexConfiguration.getMeta())
                // TODO: only consider up to one input node for now
                .outputFileInfo(
                    constructOutputFileInfoFromInputInfo(dataVertexConfiguration.getMeta()))
                .task(dataVertexConfiguration.getTask())
                .taskRuntimeService(dataVertexConfiguration.getTaskRuntimeService())
                .selected(dataVertexConfiguration.isSelected())
                .build();
        } else if (configuration instanceof AlgorithmVertexConfiguration) {
            AlgorithmVertexConfiguration algorithmVertexConfiguration = (AlgorithmVertexConfiguration) configuration;
            return AlgorithmTaskExecutor.builder()
                .meta(algorithmVertexConfiguration.getMeta())
                .outgoingMeta(algorithmVertexConfiguration.getOutgoingMeta())
                .task(algorithmVertexConfiguration.getTask())
                .taskRuntimeService(algorithmVertexConfiguration.getTaskRuntimeService())
                .taskService(algorithmVertexConfiguration.getTaskService())
                .jobService(algorithmVertexConfiguration.getJobService())
                // TODO: Test-demo: with this UDF
                .userDefinedFunction(StringLength.builder().build())
                .sourceConnectorList(new ArrayList<>())
                .okHttpClient(okHttpClient)
                .algorithmServerAddress(algorithmVertexConfiguration.getAlgorithmServerAddress())
                .algorithmConfiguration(algorithmVertexConfiguration.getAlgorithmConfiguration())
                //.sinkConnector(getOutPutConnector(outputTableName))
                //.sinkTableName(outputTableName)
                .selected(algorithmVertexConfiguration.isSelected())
                .userId(Long.valueOf(userId))
                .minioConnection(minioConnection)
                .bucketName(bucketName)
                .pathTransformer(pathTransformer)
                .build();
        } else if (configuration instanceof ETLVertexConfiguration) {
            ETLVertexConfiguration etlVertexConfiguration = (ETLVertexConfiguration) configuration;
            String outputTableName = "output_" + TableMapService.getActiveTableNameCommon(
                etlVertexConfiguration.getTask().getId());
            List<CalciteConnector> connectorList = getSourceConnector(
                etlVertexConfiguration.getMeta());
            return ETLTaskExecutor.builder()
                .meta(etlVertexConfiguration.getMeta())
                .task(etlVertexConfiguration.getTask())
                .taskRuntimeService(etlVertexConfiguration.getTaskRuntimeService())
                .userDefinedFunction(StringLength.builder().build())
                .sourceConnectorList(connectorList)
                .sinkConnector(
                    getOutputConnector(outputTableName, etlVertexConfiguration.getOutgoingMeta(),
                        new HashMap<>()))
                .sinkTableName(outputTableName)
                .selected(etlVertexConfiguration.isSelected())
                .build();
        }
        throw new NotSupportException(
            String.format("Not support configuration type(%s)",
                configuration.getClass().getName()));
    }

    private CalciteConnector getOutputConnector(String outputTableName) throws Exception {
        String userId = accountUtils.currentUserIdOptional().orElseThrow(
            () -> new Exception("User id is empty")
        );
        String outputObjectKey = pathTransformer.userIndependentPath(Long.valueOf(userId),
            outputTableName + ".csv");
        LOGGER.info("[getListConnector] outputObjectKey={}", outputObjectKey);
        return CsvS3.builder().namedObject(
            outputTableName,
            S3Resource.builder()
                .minioConnection(minioConnection)
                .bucket(bucketName)
                .objectKey(outputObjectKey)
                .build()
        ).build();
    }

    // TODO: Only consider one output connector for now (And only can be S3)
    private CalciteConnector getOutputConnector(String outputTableName,
        Map<String, List<Map<String, String>>> outgoingMeta, Map<String, String> outputFileInfo)
        throws Exception {
        String userId = accountUtils.currentUserIdOptional().orElseThrow(
            () -> new Exception("User id is empty")
        );
        String outputObjectKey = pathTransformer.userIndependentPath(Long.valueOf(userId),
            outputTableName + ".csv");
        LOGGER.info("[getOutPutConnector] outputObjectKey={}", outputObjectKey);
        Task outgoingTask = null;
        for (Map.Entry<String, List<Map<String, String>>> paramMaps : outgoingMeta.entrySet()) {
            outgoingTask = taskService.findByTaskUuid(paramMaps.getKey()).orElse(null);
        }
        return getConnectorFromDataFileContent(Long.valueOf(userId), outgoingTask,
            outputTableName, outputObjectKey, outputFileInfo);
    }

    private List<CalciteConnector> getSourceConnector(Map<String, List<Map<String, String>>> meta)
        throws Exception {
        List<CalciteConnector> connectorList = new ArrayList<>();
        String userId = accountUtils.currentUserIdOptional().orElseThrow(
            () -> new Exception("User id is empty")
        );
        LOGGER.info("[getListConnector] meta={}",
            OBJECT_MAPPER.writeValueAsString(meta));
        meta.forEach(
            (k, v) -> {
                TaskRuntime taskRuntime = taskRuntimeService.findByTaskUuid(k).get();
                try {
                    // TODO: to be checked, type in list is ok for all situation
                    List<Map<String, String>> dataList = OBJECT_MAPPER.readValue(
                        taskRuntime.getData(),
                        new TypeReference<>() {
                        });
                    v.forEach(map -> {
                            Set<String> valueKeySet = map.keySet();
                            dataList.forEach(
                                dataMap -> {
                                    Set<String> dataKeySet = dataMap.keySet();
                                    Set<String> intersectionKeySet = Sets.intersection(valueKeySet,
                                        dataKeySet);
                                    if (!CollectionUtils.isEmpty(intersectionKeySet)) {
                                        intersectionKeySet.forEach(
                                            key -> {
                                                // TODO: optimize later, e.g. replaced constant with configuration
                                                // TODO: how to set nameObject
                                                String schemaName = "schema_"
                                                    + k;  // TODO: issue: user cannot know the taskId->cannot write correct schema in sql
                                                String inputTableName = key;
                                                String fileName = dataMap.get(key);
                                                String suffix = fileName.substring(
                                                    fileName.lastIndexOf(".") + 1);
                                                String inputObjectKey = pathTransformer.userIndependentPath(
                                                    Long.valueOf(userId), dataMap.get(key));
                                                try {
                                                    MinioManager minioManager = MinioManager.builder()
                                                        .minioConnection(minioConnection)
                                                        .build();
                                                    switch (suffix) {
                                                        case "my": {
                                                            String content = getContent(minioManager,
                                                                bucketName, inputObjectKey);
                                                            MysqlVirtualFile mysqlVirtualFile = OBJECT_MAPPER.readValue(
                                                                content, new TypeReference<>() {
                                                                });
                                                            Mysql mysql = Mysql.builder()
                                                                .identifier(schemaName)
                                                                .host(mysqlVirtualFile.getHost())
                                                                .port(Integer.valueOf(
                                                                    mysqlVirtualFile.getPort()))
                                                                .username(
                                                                    mysqlVirtualFile.getUsername())
                                                                .password(
                                                                    mysqlVirtualFile.getPassword())
                                                                .databaseName(
                                                                    mysqlVirtualFile.getDatabaseName())
                                                                .build();
                                                            connectorList.add(mysql);
                                                            break;
                                                        }
                                                        case "ps": {
                                                            String content = getContent(minioManager,
                                                                bucketName, inputObjectKey);
                                                            PostgresqlVirtualFile postgresqlVirtualFile = OBJECT_MAPPER.readValue(
                                                                content, new TypeReference<>() {
                                                                });
                                                            Postgresql postgresql = Postgresql.builder()
                                                                .identifier(schemaName)
                                                                .host(postgresqlVirtualFile.getHost())
                                                                .port(Integer.valueOf(
                                                                    postgresqlVirtualFile.getPort()))
                                                                .username(
                                                                    postgresqlVirtualFile.getUsername())
                                                                .password(
                                                                    postgresqlVirtualFile.getPassword())
                                                                .databaseName(
                                                                    postgresqlVirtualFile.getDatabaseName())
                                                                .schemaName(
                                                                    postgresqlVirtualFile.getSchemaName())
                                                                .build();
                                                            connectorList.add(postgresql);
                                                            break;
                                                        }
                                                        case "s3": {
                                                            String content = getContent(minioManager,
                                                                bucketName, inputObjectKey);
                                                            S3VirtualFile s3VirtualFile = OBJECT_MAPPER.readValue(
                                                                content, new TypeReference<>() {
                                                                });
                                                            CsvS3 csvS3 = CsvS3.builder()
                                                                .identifier(schemaName)
                                                                .namedObject(
                                                                    s3VirtualFile.getName(),
                                                                    S3Resource.builder()
                                                                        .minioConnection(
                                                                            s3VirtualFile.getNamedStreamResources())
                                                                        .bucket(
                                                                            s3VirtualFile.getBucket())
                                                                        .objectKey(
                                                                            s3VirtualFile.getObjectKey())
                                                                        .build())
                                                                .build();
                                                            connectorList.add(csvS3);
                                                            break;
                                                        }
                                                        default: {
                                                            CsvS3 csvS3 = CsvS3.builder()
                                                                .identifier(schemaName)
                                                                .namedObject(
                                                                    inputTableName,
                                                                    S3Resource.builder()
                                                                        .minioConnection(
                                                                            minioConnection)
                                                                        .bucket(bucketName)
                                                                        .objectKey(inputObjectKey)
                                                                        .build())
                                                                .build();
                                                            connectorList.add(csvS3);
                                                            break;
                                                        }
                                                    }
                                                } catch (IOException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        );
                                    }
                                }
                            );
                        }
                    );
                } catch (JsonProcessingException e) {
                    LOGGER.error("[getInputList] failed, error={}", e.getMessage());
                    throw new RuntimeException(e);
                }

            });
        return connectorList;
    }

    private String getContent(MinioManager minioManager, String bucketName, String inputObjectKey)
        throws IOException {
        File targetFile = File.createTempFile("tempFile", "txt");
        FileUtils.copyInputStreamToFile(minioManager.objectGet(bucketName, inputObjectKey),
            targetFile);
        return FileUtils.readFileToString(targetFile, StandardCharsets.UTF_8);
    }

    private List<CalciteConnector> getListConnector(Map<String, List<Map<String, String>>> meta,
        String outputTableName) throws Exception {
        List<CalciteConnector> connectorList = new ArrayList<>();
        String userId = accountUtils.currentUserIdOptional().orElseThrow(
            () -> new Exception("User id is empty")
        );
        String outputObjectKey = pathTransformer.userIndependentPath(Long.valueOf(userId),
            outputTableName + ".csv");
        LOGGER.info("[getListConnector] outputObjectKey={}, meta={}", outputObjectKey,
            OBJECT_MAPPER.writeValueAsString(meta));
        meta.forEach(
            (k, v) -> {
                TaskRuntime taskRuntime = taskRuntimeService.findByTaskUuid(k).get();
                try {
                    // TODO: to be checked, type in list is ok for all situation
                    List<Map<String, String>> dataList = OBJECT_MAPPER.readValue(
                        taskRuntime.getData(),
                        new TypeReference<>() {
                        });

                    v.forEach(map -> {
                            Set<String> valueKeySet = map.keySet();
                            dataList.forEach(
                                dataMap -> {
                                    Set<String> dataKeySet = dataMap.keySet();
                                    Set<String> intersectionKeySet = Sets.intersection(valueKeySet,
                                        dataKeySet);
                                    if (!CollectionUtils.isEmpty(intersectionKeySet)) {
                                        intersectionKeySet.forEach(
                                            key -> {
                                                // TODO: optimize later, e.g. replaced constant with configuration
                                                // TODO: how to set nameObject
                                                String schemaName = "schema_"
                                                    + k;  // TODO: issue: user cannot know the taskId->cannot write correct schema in sql
                                                String inputTableName = key;
                                                String inputObjectKey = pathTransformer.userIndependentPath(
                                                    Long.valueOf(userId), dataMap.get(key));
                                                CsvS3 csvS3 = CsvS3.builder()
                                                    .identifier(schemaName)
                                                    .namedObject(
                                                        inputTableName,
                                                        S3Resource.builder()
                                                            .minioConnection(
                                                                minioConnection)
                                                            .bucket(bucketName)
                                                            .objectKey(inputObjectKey)
                                                            .build())
                                                    .namedObject(
                                                        outputTableName,
                                                        S3Resource.builder()
                                                            .minioConnection(minioConnection)
                                                            .bucket(bucketName)
                                                            .objectKey(outputObjectKey)
                                                            .build()
                                                    ).build();
                                                connectorList.add(csvS3);
                                            }
                                        );

                                    }
                                }
                            );

                        }
                    );
                } catch (JsonProcessingException e) {
                    LOGGER.error("[getInputList] failed, error={}", e.getMessage());
                    throw new RuntimeException(e);
                }

            });
        return connectorList;
    }

    private CalciteConnector getConnectorFromDataFileContent(Long userId, Task outgoingTask,
        String outputTableName, String outputObjectKey, Map<String, String> outputFileInfo) {
        LOGGER.info(
            "[getConnectorFromDataFileContent] userId={}, default outputObjectKey={}, outgoingTask={}",
            userId, outputObjectKey, outgoingTask);
        if (outgoingTask == null || !outgoingTask.isData()) {
            outputFileInfo.put(OUTPUT_FILE_MAP_KEY,
                pathTransformer.graphOutputTempPath(userId) + outputTableName + ".csv");
            outputFileInfo.put(OUTPUT_TABLE_MAP_KEY,
                pathTransformer.graphOutputTempPath(userId) + outputTableName + ".csv");
            outputFileInfo.put(OUTPUT_VIRTUAL_FLAG, "false");
            return CsvS3.builder().namedObject(
                outputTableName,
                S3Resource.builder()
                    .minioConnection(minioConnection)
                    .bucket(bucketName)
                    .objectKey(pathTransformer.userIndependentPath(userId,
                        pathTransformer.graphOutputTempPath(userId) + outputTableName + ".csv"
                    ))
                    .build()
            ).build();
        }
        String fileName = outgoingTask.getConfigurationReference();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        String inputObjectKey = pathTransformer.userIndependentPath(userId, fileName);
        try {
            MinioManager minioManager = MinioManager.builder()
                .minioConnection(minioConnection)
                .build();
            String content = net.zjvis.flint.data.hub.util.FileUtils.getMinioFileContent(
                minioManager, bucketName,
                inputObjectKey);
            LOGGER.info("[getConnectorFromDataFileContent] fileName={}, content={}", fileName,
                content);
            outputFileInfo.put(OUTPUT_FILE_MAP_KEY, fileName);
            outputFileInfo.put(OUTPUT_VIRTUAL_FLAG, "true");
            switch (suffix) {
                case "s3": {
                    S3VirtualFile s3VirtualFile = OBJECT_MAPPER.readValue(content,
                        new TypeReference<>() {
                        });
                    outputFileInfo.put(OUTPUT_TABLE_MAP_KEY, s3VirtualFile.getObjectKey());
                    return CsvS3.builder().namedObject(
                        outputTableName,
                        S3Resource.builder()
                            .minioConnection(MinioConnection.builder()
                                .accessKey(s3VirtualFile.getNamedStreamResources().getAccessKey())
                                .accessSecret(
                                    s3VirtualFile.getNamedStreamResources().getAccessSecret())
                                .endpoint(s3VirtualFile.getNamedStreamResources().getEndpoint())
                                .build())
                            .bucket(s3VirtualFile.getBucket())
                            .objectKey(s3VirtualFile.getObjectKey())
                            .build()
                    ).build();
                }
                case "my": {
                    MysqlVirtualFile mysqlVirtualFile = OBJECT_MAPPER.readValue(content,
                        new TypeReference<>() {
                        });
                    outputFileInfo.put(OUTPUT_TABLE_MAP_KEY, outputTableName);
                    return Mysql.builder()
                        .identifier(outputTableName)
                        .host(mysqlVirtualFile.getHost())
                        .port(Integer.valueOf(mysqlVirtualFile.getPort()))
                        .username(mysqlVirtualFile.getUsername())
                        .password(mysqlVirtualFile.getPassword())
                        .databaseName(mysqlVirtualFile.getDatabaseName())
                        .build();
                }
                case "ps": {
                    PostgresqlVirtualFile postgresqlVirtualFile = OBJECT_MAPPER.readValue(content,
                        new TypeReference<>() {
                        });
                    outputFileInfo.put(OUTPUT_TABLE_MAP_KEY, outputTableName);
                    return Postgresql.builder()
                        .identifier(outputTableName)
                        .host(postgresqlVirtualFile.getHost())
                        .port(Integer.valueOf(postgresqlVirtualFile.getPort()))
                        .username(postgresqlVirtualFile.getUsername())
                        .password(postgresqlVirtualFile.getPassword())
                        .databaseName(postgresqlVirtualFile.getDatabaseName())
                        .schemaName(postgresqlVirtualFile.getSchemaName())
                        .build();
                }
                default: {
                    String errorMsg = String.format("cannot support file type=%s", suffix);
                    LOGGER.error("[getConnectorFromDataFileContent] failed, error={}", errorMsg);
                    throw new Exception(errorMsg);
                }
            }
        } catch (Exception e) {
            LOGGER.error("[getConnectorFromDataFileContent] failed, exception={}", e.toString());
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> constructOutputFileInfoFromInputInfo(
        Map<String, List<Map<String, String>>> meta) {
        LOGGER.info("[constructOutputFileInfoFromInputInfo] meta={}", meta);
        Map<String, String> outputFileInfo = new HashMap<>();
        outputFileInfo.put(OUTPUT_VIRTUAL_FLAG, "false");
        if (meta != null && !meta.keySet().isEmpty()) {
            outputFileInfo.put(OUTPUT_VIRTUAL_FLAG, "true");
            String firstTaskUuid = meta.keySet().iterator().next();
            outputFileInfo.put(OUTPUT_PRE_TASK_UUID, firstTaskUuid);
        }
        return outputFileInfo;
    }


}