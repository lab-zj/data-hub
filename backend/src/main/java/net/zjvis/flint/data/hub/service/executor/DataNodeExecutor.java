package net.zjvis.flint.data.hub.service.executor;

import static net.zjvis.flint.data.hub.util.Constant.OUTPUT_PRE_TASK_UUID;
import static net.zjvis.flint.data.hub.util.Constant.OUTPUT_VIRTUAL_FLAG;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.entity.graph.Task;
import net.zjvis.flint.data.hub.entity.graph.TaskRuntime;
import net.zjvis.flint.data.hub.lib.calcite.CalciteManager;
import net.zjvis.flint.data.hub.lib.calcite.connector.CalciteConnector;
import net.zjvis.flint.data.hub.lib.calcite.executor.AbstractCalciteExecutor;
import net.zjvis.flint.data.hub.lib.calcite.func.UserDefinedFunction;
import net.zjvis.flint.data.hub.service.graph.TaskRuntimeService;
import net.zjvis.flint.data.hub.util.JobStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DataNodeExecutor extends AbstractCalciteExecutor {

    public static final Logger LOGGER = LoggerFactory.getLogger(DataNodeExecutor.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectMapper YAML_OBJECT_MAPPER = new ObjectMapper(
        new YAMLFactory().disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID));

    private final Map<String, List<Map<String, String>>> meta;
    private final Map<String, String> outputFileInfo;
    private final Task task;
    private final TaskRuntimeService taskRuntimeService;
    private final boolean selected;

    @Builder
    @Jacksonized
    public DataNodeExecutor(List<CalciteConnector> sourceConnectorList,
        List<UserDefinedFunction> userDefinedFunctionList, CalciteConnector sinkConnector,
        Map<String, List<Map<String, String>>> meta,
        Map<String, String> outputFileInfo,
        Task task,
        TaskRuntimeService taskRuntimeService,
        boolean selected) {
        super(new ArrayList<>(), new ArrayList<>(), sinkConnector);
        this.meta = meta;
        this.outputFileInfo = outputFileInfo;
        this.task = task;
        this.taskRuntimeService = taskRuntimeService;
        this.selected = selected;
    }


    @Override
    protected Object[] operands() {
        return null;
    }

    @Override
    protected void doExecute(CalciteManager calciteManager, Consumer<ResultSet> consumer) {
        String taskUuid = task.getTaskUuid();
        try {
            LOGGER.info(
                "[DataNodeExecutor][{}] start, selected={}, config={}, meta={}, outputFileInfo={}",
                taskUuid,
                selected,
                OBJECT_MAPPER.writeValueAsString(task),
                OBJECT_MAPPER.writeValueAsString(meta),
                OBJECT_MAPPER.writeValueAsString(outputFileInfo));
            if (!selected || taskRuntimeService.isTaskCanceled(taskUuid)
                || taskRuntimeService.isTaskStopped(taskUuid)) {
                LOGGER.info("[DataNodeExecutor][{}] no need to execute", taskUuid);
                return;
            }
            taskRuntimeService.updateBuild(taskUuid, meta, JobStatusEnum.START.getName(), null,
                null, null, null);
            if (Boolean.parseBoolean(outputFileInfo.get(OUTPUT_VIRTUAL_FLAG))) {
                TaskRuntime preTaskRuntime = taskRuntimeService.findByTaskUuid(
                    outputFileInfo.get(OUTPUT_PRE_TASK_UUID)).orElseThrow(() -> new Exception(
                    String.format("Cannot find taskRuntime by taskUuid=%s",
                        outputFileInfo.get(OUTPUT_PRE_TASK_UUID))));
                // TODO: to be check, may error in one more input situation
                taskRuntimeService.updateBuild(taskUuid, meta, JobStatusEnum.FINISHED.getName(),
                    JobStatusEnum.SUCCESS.getName(), null,
                    preTaskRuntime.getCloudData(), null);

            } else {
                taskRuntimeService.updateBuild(taskUuid, meta, JobStatusEnum.FINISHED.getName(),
                    JobStatusEnum.SUCCESS.getName(),
                    OBJECT_MAPPER.writeValueAsString(
                        Arrays.asList(Map.of("default", task.getConfigurationReference()))), null);
            }
        } catch (Exception e) {
            LOGGER.error("[DataNodeExecutor][{}] failed, error={}", taskUuid,
                e.getMessage());
            try {
                taskRuntimeService.updateBuild(taskUuid, meta, JobStatusEnum.FINISHED.getName(),
                    JobStatusEnum.FAILED.getName(), null, e.getMessage());
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

}
