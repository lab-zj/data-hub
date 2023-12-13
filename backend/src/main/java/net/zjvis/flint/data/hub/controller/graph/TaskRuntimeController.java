package net.zjvis.flint.data.hub.controller.graph;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import net.zjvis.flint.data.hub.Code;
import net.zjvis.flint.data.hub.StandardResponse;
import net.zjvis.flint.data.hub.controller.graph.vo.TaskRuntimeVO;
import net.zjvis.flint.data.hub.entity.graph.TaskRuntime;
import net.zjvis.flint.data.hub.service.graph.TaskRuntimeService;
import net.zjvis.flint.data.hub.util.JobStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/graph/task")
public class TaskRuntimeController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskRuntimeController.class);
    private final TaskRuntimeService taskRuntimeService;

    public TaskRuntimeController(TaskRuntimeService taskRuntimeService) {
        this.taskRuntimeService = taskRuntimeService;
    }

    @Operation(summary = "Init tasks by specifying task id list")
    @PostMapping("/runtime/batch/init")
    public StandardResponse<String> batchInitTask(
            @Parameter(description = "To be inited task id list")
            @RequestParam List<String> taskUuidList) {
        LOGGER.info("[batchInitTask] start, taskUuidList={}", taskUuidList.toString());
        return StandardResponse.<String>builder()
                .data(taskRuntimeService.batchUpdateBuildStatus(taskUuidList, JobStatusEnum.INIT)).build();
    }

    @Operation(summary = "Get task runtime info")
    @GetMapping("/{taskUuid}/runtime")
    public StandardResponse<TaskRuntimeVO> getTaskBuildInfo(
            @Parameter(description = "Task id of queried task build info")
            @PathVariable String taskUuid) {
        try {
            LOGGER.info("[getTaskBuildInfo] start, taskUuid={}", taskUuid);
            TaskRuntime taskRuntime = taskRuntimeService.findByTaskUuid(taskUuid).orElseThrow(
                    () -> new Exception(String.format("cannot find build by task uuid=%s", taskUuid)));
            return StandardResponse.<TaskRuntimeVO>builder()
                    .data(taskRuntime.toVO())
                    .build();
        } catch (Exception e) {
            LOGGER.error("[getTaskBuildInfo] failed, error={}", e.getMessage());
            return StandardResponse.<TaskRuntimeVO>builder().code(Code.BAD_REQUEST.getValue())
                    .message(e.getMessage()).build();
        }
    }
}
