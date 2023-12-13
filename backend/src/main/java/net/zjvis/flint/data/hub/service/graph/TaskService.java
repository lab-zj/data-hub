package net.zjvis.flint.data.hub.service.graph;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.zjvis.flint.data.hub.controller.graph.vo.TaskNodeInfoVO;
import net.zjvis.flint.data.hub.controller.graph.vo.TaskVO;
import net.zjvis.flint.data.hub.entity.graph.Task;
import net.zjvis.flint.data.hub.repository.graph.TaskRepository;
import net.zjvis.flint.data.hub.service.FilesystemService;
import net.zjvis.flint.data.hub.util.FileUtils;
import net.zjvis.flint.data.hub.util.TaskTypeEnum;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskService.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final TaskRepository taskRepository;

    public TaskService(
        TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskVO> batchGetTaskVOs(List<String> taskUuidList) {
        List<Task> taskList = taskRepository.findByTaskUuidIn(taskUuidList);
        List<TaskVO> taskVOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(taskList)) {
            taskVOList = taskList.stream().map(Task::toVO).collect(Collectors.toList());
        }
        return taskVOList;
    }

    public List<TaskNodeInfoVO> batchGetTaskNodesDetails(List<String> taskUuidList) {
        List<Task> taskList = taskRepository.findByTaskUuidIn(taskUuidList);
        List<TaskNodeInfoVO> taskNodeInfoVOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(taskList)) {
            taskList.forEach(
                task -> {
                    if (task.isData()) {
                        taskNodeInfoVOList.add(TaskNodeInfoVO.builder()
                            .taskId(task.getId())
                            .taskUuid(task.getTaskUuid())
                            .directory(
                                FilesystemService.isDirectory(task.getConfigurationReference()))
                            .type(FileUtils.getFileType(task.getConfigurationReference()))
                            .path(task.getConfigurationReference())
                            .name(FilenameUtils.getName(task.getConfigurationReference()))
                            .build());
                    } else if (task.isSQL()) {
                        taskNodeInfoVOList.add(TaskNodeInfoVO.builder()
                            .taskId(task.getId())
                            .type(TaskTypeEnum.SQL.getName())
                            .taskUuid(task.getTaskUuid())
                            .sql(task.getConfiguration())
                            .build());
                    } else if (task.isAlgorithm()) {
                        taskNodeInfoVOList.add(TaskNodeInfoVO.builder()
                            .taskId(task.getId())
                            .taskUuid(task.getTaskUuid())
                            .type(TaskTypeEnum.Algorithm.getName())
                            .inputParamTemplate(task.getInputParamTemplate())
                            .outputParamTemplate(task.getOutputParamTemplate())
                            .algorithm(task.getConfigurationReference())
                            .build());
                    }
                }
            );


        }
        return taskNodeInfoVOList;
    }

    public void batchDeleteTasks(List<String> taskUuidList) {
        taskRepository.deleteByTaskUuidIn(taskUuidList);
    }

    public TaskVO upsertTaskVO(TaskVO taskVO) {
        Task task = Task.fromVO(taskVO);
        task = task.toBuilder()
            .gmtCreate(task.getGmtCreate() == null ? LocalDateTime.now() : task.getGmtCreate())
            .gmtModify(LocalDateTime.now())
            .taskUuid(StringUtils.isEmpty(task.getTaskUuid()) ? UUID.randomUUID()
                .toString()
                .replaceAll("-", "")
                : task.getTaskUuid())
            .build();
        return taskRepository.save(task).toVO();
    }

    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }

    public Optional<Task> findByTaskUuid(String uuid) {
        return taskRepository.findByTaskUuid(uuid);
    }

    public Task save(Task task) {
        if (task.getId() == null) {
            task = task.toBuilder()
                .gmtCreate(LocalDateTime.now())
                .build();
        }
        task = task.toBuilder()
            .gmtModify(LocalDateTime.now())
            .build();
        return taskRepository.save(task);
    }
}
