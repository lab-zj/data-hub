package net.zjvis.flint.data.hub.service.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.entity.etl.ETL;
import net.zjvis.flint.data.hub.entity.graph.Task;
import net.zjvis.flint.data.hub.lib.calcite.CalciteManager;
import net.zjvis.flint.data.hub.lib.calcite.connector.CalciteConnector;
import net.zjvis.flint.data.hub.lib.calcite.executor.AbstractCalciteExecutor;
import net.zjvis.flint.data.hub.lib.calcite.func.UserDefinedFunction;
import net.zjvis.flint.data.hub.service.graph.TaskRuntimeService;
import net.zjvis.flint.data.hub.util.JobStatusEnum;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.tools.RelBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RelNodeExecutor extends AbstractCalciteExecutor {

    public static final Logger LOGGER = LoggerFactory.getLogger(RelNodeExecutor.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final String sinkTableName;
    private final Map<String, List<Map<String, String>>> meta;
    private final Task task;
    private final TaskRuntimeService taskRuntimeService;
    private final boolean selected;
    private final ETL etlTemplate;

    @Builder
    @Jacksonized
    public RelNodeExecutor(
            @Singular("sourceConnector")
            List<CalciteConnector> sourceConnectorList,
            @Singular("userDefinedFunction")
            List<UserDefinedFunction> userDefinedFunctionList,
            CalciteConnector sinkConnector,
            String sinkTableName,
            Map<String, List<Map<String, String>>> meta,
            Task task,
            ETL etlTemplate,
            TaskRuntimeService taskRuntimeService,
            boolean selected) {
        super(sourceConnectorList, userDefinedFunctionList, sinkConnector);
        this.sinkTableName = sinkTableName;
        this.meta = meta;
        this.task = task;
        this.etlTemplate = etlTemplate;
        this.taskRuntimeService = taskRuntimeService;
        this.selected = selected;
    }


    @Override
    protected Object[] operands() {
        return new Object[]{sinkTableName, false};
    }

    @Override
    protected void doExecute(CalciteManager calciteManager, Consumer<ResultSet> consumer)
            throws SQLException {
        RelBuilder relBuilder = calciteManager.constructRelBuilder(false);
        String taskUuid = task.getTaskUuid();
        List<Pair<String, String>> inputInfo = new ArrayList<>();
        meta.entrySet().stream().forEach(e -> {
            e.getValue().stream().forEach(io -> {
                inputInfo.addAll(io.keySet().stream().map(k -> Pair.of("schema_" + e.getKey(), k)).collect(Collectors.toList()));
            });
        });

        try {
            LOGGER.info("[RelNodeExecutor][{}] start, meta={}", taskUuid, OBJECT_MAPPER.writeValueAsString(meta));
            if (!selected || taskRuntimeService.isTaskCanceled(taskUuid) || taskRuntimeService.isTaskStopped(taskUuid)) {
                LOGGER.info("[RelNodeExecutor][{}] no need to execute", taskUuid);
                return;
            }
            taskRuntimeService.updateBuild(taskUuid, meta, JobStatusEnum.START.getName(), null, null, null);
            LOGGER.info("[RelNodeExecutor][{}] calcite start", taskUuid);
//            calciteManager.execute("select * from \"schema_9a32a78815eb45c18fac1d64a07b20f7\".\"default\"", consumer);
            calciteManager.execute(etlTemplate.apply(inputInfo).apply(relBuilder), consumer);
            LOGGER.info("[RelNodeExecutor][{}] calcite finished", taskUuid);
            // TODO: build data save the result file url path (only consider csv for now)
            taskRuntimeService.updateBuild(taskUuid, meta, JobStatusEnum.FINISHED.getName(),
                    JobStatusEnum.SUCCESS.getName(),
                    OBJECT_MAPPER.writeValueAsString(
                            List.of(Map.of("default", sinkTableName + ".csv"))),
                    null);
        } catch (SQLException e) {
            LOGGER.error("[RelNodeExecutor][{}] failed, sql exception={}", taskUuid, e.toString());
            try {
                taskRuntimeService.updateBuild(taskUuid, meta, JobStatusEnum.FINISHED.getName(),
                        JobStatusEnum.FAILED.getName(),
                        null, e.getMessage());
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
            throw e;
        } catch (Exception e) {
            LOGGER.error("[RelNodeExecutor][{}] failed, exception={}", taskUuid, e.toString());
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
