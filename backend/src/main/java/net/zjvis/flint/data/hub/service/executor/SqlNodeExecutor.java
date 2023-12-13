package net.zjvis.flint.data.hub.service.executor;

import static net.zjvis.flint.data.hub.util.Constant.OUTPUT_FILE_MAP_KEY;
import static net.zjvis.flint.data.hub.util.Constant.OUTPUT_TABLE_MAP_KEY;
import static net.zjvis.flint.data.hub.util.Constant.OUTPUT_VIRTUAL_FLAG;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.entity.graph.Task;
import net.zjvis.flint.data.hub.lib.calcite.CalciteManager;
import net.zjvis.flint.data.hub.lib.calcite.connector.CalciteConnector;
import net.zjvis.flint.data.hub.lib.calcite.executor.AbstractCalciteExecutor;
import net.zjvis.flint.data.hub.lib.calcite.func.UserDefinedFunction;
import net.zjvis.flint.data.hub.service.graph.TaskRuntimeService;
import net.zjvis.flint.data.hub.util.JobStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SqlNodeExecutor extends AbstractCalciteExecutor {

    public static final Logger LOGGER = LoggerFactory.getLogger(SqlNodeExecutor.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final String sinkTableName;
    private final Map<String, String> outputFileInfo;
    private final String selectSql;
    private final Map<String, List<Map<String, String>>> meta;
    private final Task task;
    private final TaskRuntimeService taskRuntimeService;
    private final boolean selected;

    @Builder
    @Jacksonized
    public SqlNodeExecutor(
        @Singular("sourceConnector")
        List<CalciteConnector> sourceConnectorList,
        @Singular("userDefinedFunction")
        List<UserDefinedFunction> userDefinedFunctionList,
        CalciteConnector sinkConnector,
        String sinkTableName,
        Map<String, String> outputFileInfo,
        String selectSql,
        Map<String, List<Map<String, String>>> meta,
        Task task,
        TaskRuntimeService taskRuntimeService,
        boolean selected) {
        super(sourceConnectorList, userDefinedFunctionList, sinkConnector);
        this.sinkTableName = sinkTableName;
        this.outputFileInfo = outputFileInfo;
        this.selectSql = selectSql;
        this.meta = meta;
        this.task = task;
        this.taskRuntimeService = taskRuntimeService;
        this.selected = selected;
    }


    @Override
    protected Object[] operands() {
        return new Object[]{sinkTableName, true};
    }

    @Override
    protected void doExecute(CalciteManager calciteManager, Consumer<ResultSet> consumer)
        throws SQLException {
        String taskUuid = task.getTaskUuid();
        try {
            LOGGER.info("[SqlNodeExecutor][{}] start, meta={}, outputFileInfo={}", taskUuid,
                OBJECT_MAPPER.writeValueAsString(meta),
                OBJECT_MAPPER.writeValueAsString(outputFileInfo));
            if (!selected || taskRuntimeService.isTaskCanceled(taskUuid)
                || taskRuntimeService.isTaskStopped(taskUuid)) {
                LOGGER.info("[SqlNodeExecutor][{}] no need to execute", taskUuid);
                return;
            }
            taskRuntimeService.updateBuild(taskUuid, meta, JobStatusEnum.START.getName(), null,
                null, null);
            LOGGER.info("[SqlNodeExecutor][{}] calcite start", taskUuid);
            calciteManager.execute(selectSql, consumer);
            LOGGER.info("[SqlNodeExecutor][{}] calcite finished", taskUuid);
            // TODO: build data save the result file url path (only consider csv for now)
            String outputInfo;
            if (Boolean.parseBoolean(outputFileInfo.get(OUTPUT_VIRTUAL_FLAG))) {
                outputInfo = outputFileInfo.get(OUTPUT_FILE_MAP_KEY) + ":" + outputFileInfo.get(
                    OUTPUT_TABLE_MAP_KEY);
                taskRuntimeService.updateBuild(taskUuid, meta, JobStatusEnum.FINISHED.getName(),
                    JobStatusEnum.SUCCESS.getName(), null,
                    OBJECT_MAPPER.writeValueAsString(
                        List.of(Map.of("default", outputInfo))),
                    null);
            } else {
                outputInfo = outputFileInfo.get(OUTPUT_FILE_MAP_KEY);
                taskRuntimeService.updateBuild(taskUuid, meta, JobStatusEnum.FINISHED.getName(),
                    JobStatusEnum.SUCCESS.getName(),
                    OBJECT_MAPPER.writeValueAsString(
                        List.of(Map.of("default", outputInfo))),
                    null);
            }
        } catch (SQLException e) {
            LOGGER.error("[SqlNodeExecutor][{}] failed, sql exception={}", taskUuid, e.toString());
            try {
                taskRuntimeService.updateBuild(taskUuid, meta, JobStatusEnum.FINISHED.getName(),
                    JobStatusEnum.FAILED.getName(),
                    null, e.getMessage());
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
            throw e;
        } catch (Exception e) {
            LOGGER.error("[SqlNodeExecutor][{}] failed, exception={}", taskUuid, e.toString());
            try {
                taskRuntimeService.updateBuild(taskUuid, meta, JobStatusEnum.FINISHED.getName(),
                    JobStatusEnum.FAILED.getName(),
                    null, e.getMessage());
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }
}
