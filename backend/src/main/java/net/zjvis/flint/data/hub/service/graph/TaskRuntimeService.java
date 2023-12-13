package net.zjvis.flint.data.hub.service.graph;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.zjvis.flint.data.hub.controller.filesystem.PathTransformer;
import net.zjvis.flint.data.hub.controller.graph.vo.TableInfoVO;
import net.zjvis.flint.data.hub.entity.filesystem.database.MysqlVirtualFile;
import net.zjvis.flint.data.hub.entity.filesystem.database.PostgresqlVirtualFile;
import net.zjvis.flint.data.hub.entity.filesystem.database.S3VirtualFile;
import net.zjvis.flint.data.hub.entity.filesystem.database.VirtualFile;
import net.zjvis.flint.data.hub.entity.graph.Task;
import net.zjvis.flint.data.hub.entity.graph.TaskRuntime;
import net.zjvis.flint.data.hub.lib.calcite.CalciteManager;
import net.zjvis.flint.data.hub.lib.calcite.connector.CalciteConnector;
import net.zjvis.flint.data.hub.lib.calcite.connector.csv.CsvS3;
import net.zjvis.flint.data.hub.lib.calcite.connector.jdbc.Mysql;
import net.zjvis.flint.data.hub.lib.calcite.connector.jdbc.Postgresql;
import net.zjvis.flint.data.hub.lib.calcite.resource.S3Resource;
import net.zjvis.flint.data.hub.lib.minio.MinioConnection;
import net.zjvis.flint.data.hub.lib.minio.MinioManager;
import net.zjvis.flint.data.hub.repository.graph.TaskRepository;
import net.zjvis.flint.data.hub.repository.graph.TaskRuntimeRepository;
import net.zjvis.flint.data.hub.service.account.AccountUtils;
import net.zjvis.flint.data.hub.util.JobStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class TaskRuntimeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskRuntimeService.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final TaskRuntimeRepository taskRuntimeRepository;

    private final TaskRepository taskRepository;

    private final  PathTransformer pathTransformer;

    private final AccountUtils accountUtils;

    private final MinioConnection minioConnection;
    private final String bucketName;

    public TaskRuntimeService(
            MinioConnection minioConnection,
            @Value("${application.s3.minio.bucket.filesystem}") String bucketName,
            TaskRuntimeRepository taskRuntimeRepository,
            TaskRepository taskRepository,
            PathTransformer pathTransformer,
            AccountUtils accountUtils) {
        this.taskRuntimeRepository = taskRuntimeRepository;
        this.taskRepository = taskRepository;
        this.pathTransformer = pathTransformer;
        this.accountUtils = accountUtils;
        this.minioConnection = minioConnection;
        this.bucketName = bucketName;
    }

    public List<TaskRuntime> queryByTaskUuidOrderByGmtModifyDesc(String taskUuid) {
        return taskRuntimeRepository.findByTaskUuidOrderByGmtModifyDesc(taskUuid);
    }

    public TaskRuntime saveBuild(TaskRuntime taskRuntime) {
        return taskRuntimeRepository.save(taskRuntime);
    }

    public Optional<TaskRuntime> findByTaskUuid(String taskUuid) {
        return taskRuntimeRepository.findByTaskUuid(taskUuid);
    }

    public List<TaskRuntime> findByJobId(Long jobId) {
        return taskRuntimeRepository.findByJobId(jobId);
    }

    public String batchUpdateBuildStatus(List<String> taskUuidList, JobStatusEnum jobStatusEnum) {
        List<TaskRuntime> updateTaskRuntimeList = taskRuntimeRepository.findAllByTaskUuidIn(
                taskUuidList).stream()
            .map(build -> build.toBuilder().gmtModify(
                LocalDateTime.now()).status(jobStatusEnum.getName()).build())
            .collect(Collectors.toList());
        List<TaskRuntime> updatedTaskRuntimeList = taskRuntimeRepository.saveAll(
            updateTaskRuntimeList);
        LOGGER.info("[batchUpdateBuildStatus] finished, result={}",
            updatedTaskRuntimeList.toString());
        return "success";
    }

    public List<TaskRuntime> batchSave(List<TaskRuntime> taskRuntimeList) {
        return taskRuntimeRepository.saveAll(taskRuntimeList);
    }

    public Optional<TaskRuntime> updateBuild(String taskUuid,
        Map<String, List<Map<String, String>>> param,
        String status,
        String result, String data, String message)
        throws JsonProcessingException {
        Optional<TaskRuntime> buildOptional = findByTaskUuid(taskUuid);
        if (buildOptional.isEmpty()) {
            String errorMessage = String.format("cannot find build by taskUuid=%s", taskUuid);
            LOGGER.error("[updatedBuild] failed, error={}", errorMessage);
            throw new RuntimeException(errorMessage);
        }
        TaskRuntime updatedTaskRuntime = buildOptional.get().toBuilder()
            .param(OBJECT_MAPPER.writeValueAsString(param))
            //.code(200)
            .status(status)
            .result(result)
            .data(data)
            .message(message)
            .gmtModify(LocalDateTime.now())
            .build();
        return Optional.ofNullable(saveBuild(updatedTaskRuntime));
    }

    public Optional<TaskRuntime> updateBuild(String taskUuid,
        Map<String, List<Map<String, String>>> param,
        String status,
        String result, String data, String cloudData, String message)
        throws JsonProcessingException {
        Optional<TaskRuntime> buildOptional = findByTaskUuid(taskUuid);
        if (buildOptional.isEmpty()) {
            String errorMessage = String.format("cannot find build by taskUuid=%s", taskUuid);
            LOGGER.error("[updatedBuild] failed, error={}", errorMessage);
            throw new RuntimeException(errorMessage);
        }
        TaskRuntime updatedTaskRuntime = buildOptional.get().toBuilder()
            .param(OBJECT_MAPPER.writeValueAsString(param))
            //.code(200)
            .status(status)
            .result(result)
            .data(data)
            .cloudData(cloudData)
            .message(message)
            .gmtModify(LocalDateTime.now())
            .build();
        return Optional.ofNullable(saveBuild(updatedTaskRuntime));
    }

    public void batchDeleteBuildsByTaskUuids(List<String> taskUuidList) {
        taskRuntimeRepository.deleteAllByTaskUuidIn(taskUuidList);
    }

    public boolean isTaskCanceled(String taskUuid) throws Exception {
        TaskRuntime taskRuntime = findByTaskUuid(taskUuid).orElseThrow(
            () -> new Exception(String.format("cannot find build by task uuid=%s", taskUuid)));
        return JobStatusEnum.CANCELED.name().equals(taskRuntime.getStatus());
    }

    public boolean isTaskStopped(String taskUuid) throws Exception {
        TaskRuntime taskRuntime = findByTaskUuid(taskUuid).orElseThrow(
            () -> new Exception(String.format("cannot find build by task uuid=%s", taskUuid)));
        return JobStatusEnum.STOPPED.name().equals(taskRuntime.getStatus());
    }

    public List<TableInfoVO> getTableNamesByTaskUuid(String taskUUID) throws Exception {
        String userId = accountUtils.currentUserIdOptional().orElseThrow(
                () -> new Exception("User id is empty")
        );
        String schemaNameCommon = "schema_" + taskUUID;
        List<TableInfoVO> tableNameList = new ArrayList<>();
        try {
            Optional<TaskRuntime> taskRuntimeOptional = findByTaskUuid(taskUUID);
            Task task = taskRepository.findByTaskUuid(taskUUID).orElseThrow(
                    () -> new Exception(String.format("cannot find task by id=%s", taskUUID))
            );
            AtomicReference<List<Map<String, String>>> dataList = new AtomicReference<>(new ArrayList<>());
            taskRuntimeOptional.ifPresentOrElse(
                    taskRuntime -> {
                        try {
                            dataList.set(OBJECT_MAPPER.readValue(taskRuntime.getData(), new TypeReference<>() {
                            }));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    () -> {
                        if (task.isData()) {
                            dataList.get().add(Map.of("default", task.getConfigurationReference()));
                        }
                    }
            );
            dataList.get().forEach(dataMap -> {
                dataMap.keySet().forEach(key -> {
                    List<CalciteConnector> connectorList = new ArrayList<>();
                    String schemaName = "schema_" + key;
                    String suffix = dataMap.get(key).substring(dataMap.get(key).lastIndexOf(".") + 1);
                    String inputObjectKey = pathTransformer.userIndependentPath(Long.valueOf(userId), dataMap.get(key));
                    try {
                        MinioManager minioManager = MinioManager.builder()
                                .minioConnection(minioConnection)
                                .build();
                        switch (suffix) {
                            case "my":
                            case "ps":
                            case "s3": {
                                VirtualFile virtualFile = OBJECT_MAPPER.readValue(
                                        minioManager.objectGet(bucketName, inputObjectKey),
                                        new TypeReference<>() {
                                        });
                                connectorList.add(virtualFile.toConnector(schemaName));
                                break;
                            }
                            case "csv": {
                                CsvS3 csvS3 = CsvS3.builder()
                                        .identifier(schemaName)
                                        .namedObject(
                                                key,
                                                S3Resource.builder()
                                                        .minioConnection(minioConnection)
                                                        .bucket(bucketName)
                                                        .objectKey(inputObjectKey)
                                                        .build())
                                        .build();
                                connectorList.add(csvS3);
                                break;
                            }
                            default: {
                                LOGGER.info("[getTableList] not supported connector type: {}", suffix);
                            }
                        }
                        if (connectorList.isEmpty()) {
                            return;
                        }
                        CalciteManager calciteManager = CalciteManager.builder()
                                .dataSourceList(connectorList.stream()
                                        .map(CalciteConnector::asDataSource)
                                        .collect(Collectors.toList()))
                                .build();
                        Set<String> tableNames = calciteManager.schema(schemaName).getTableNames();
                        tableNames.forEach(
                                name -> tableNameList.add(TableInfoVO.builder()
                                        .showName(String.format("%s.%s.%s", task.getName(), key, name))
                                        .value(String.format("\"%s\".\"%s\"", schemaNameCommon, name))
                                        .build())
                        );
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            });
            return tableNameList;
        } catch (Exception e) {
            LOGGER.error("[getTableList] failed, error={}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
