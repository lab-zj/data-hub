package net.zjvis.flint.data.hub.controller.graph;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import net.zjvis.flint.data.hub.StandardResponse;
import net.zjvis.flint.data.hub.controller.graph.vo.TaskNodeInfoVO;
import net.zjvis.flint.data.hub.controller.graph.vo.TaskVO;
import net.zjvis.flint.data.hub.service.graph.TaskRuntimeService;
import net.zjvis.flint.data.hub.service.graph.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/graph/task")
public class TaskController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskController.class);
    private final TaskService taskService;
    private final TaskRuntimeService taskRuntimeService;

    public TaskController(TaskService taskService, TaskRuntimeService taskRuntimeService) {
        this.taskService = taskService;
        this.taskRuntimeService = taskRuntimeService;
    }

    @Operation(summary = "Upsert task node")
    @PostMapping("")
    public StandardResponse<TaskVO> upsertTask(
        @Parameter(description = "To be upserted task node info")
        @RequestBody TaskVO taskVO) {
        LOGGER.info("[upsertTask] start, taskVO={}", taskVO.toString());
        return StandardResponse.<TaskVO>builder()
            .data(taskService.upsertTaskVO(taskVO))
            .build();
    }

    @Operation(summary = "Batch get info of task nodes")
    @GetMapping("/batch")
    public StandardResponse<List<TaskVO>> batchGetTasks(
        @Parameter(description = "To be queried task node uuid list")
        @RequestParam List<String> taskUuidList) {
        LOGGER.info("[batchGetTasks] start, taskUuidList={}", taskUuidList.toString());
        return StandardResponse.<List<TaskVO>>builder()
            .data(taskService.batchGetTaskVOs(taskUuidList))
            .build();
    }

    @Operation(summary = "Batch delete task nodes")
    @DeleteMapping("/batch")
    public StandardResponse<String> batchDeleteTasks(
        // TODO: Consider to delete logically with a flag
        @Parameter(description = "To be deleted task node uuid list")
        @RequestParam List<String> taskUuidList) {
        LOGGER.info("[batchDeleteTasks] start, taskUuidList={}", taskUuidList.toString());
        taskService.batchDeleteTasks(taskUuidList);
        taskRuntimeService.batchDeleteBuildsByTaskUuids(taskUuidList);
        return StandardResponse.<String>builder()
            .data("success")
            .build();
    }

    @Operation(summary = "Batch get info of task node details")
    @GetMapping("/details/batch")
    public StandardResponse<List<TaskNodeInfoVO>> batchGetTaskNodesDetails(
        @Parameter(description = "To be queried task node id list")
        @RequestParam List<String> taskUuidList) {
        LOGGER.info("[batchGetTaskNodesDetails] start, taskUuidList={}", taskUuidList.toString());
        return StandardResponse.<List<TaskNodeInfoVO>>builder()
            .data(taskService.batchGetTaskNodesDetails(taskUuidList))
            .build();
    }
}
