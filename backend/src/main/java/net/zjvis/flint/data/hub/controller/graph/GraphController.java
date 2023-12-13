package net.zjvis.flint.data.hub.controller.graph;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import java.util.Optional;
import net.zjvis.flint.data.hub.Code;
import net.zjvis.flint.data.hub.StandardResponse;
import net.zjvis.flint.data.hub.controller.graph.vo.GraphVO;
import net.zjvis.flint.data.hub.controller.graph.vo.JobVO;
import net.zjvis.flint.data.hub.controller.graph.vo.SqlTableVO;
import net.zjvis.flint.data.hub.entity.graph.BusinessGraph;
import net.zjvis.flint.data.hub.entity.graph.GraphMap;
import net.zjvis.flint.data.hub.entity.graph.Job;
import net.zjvis.flint.data.hub.service.account.AccountUtils;
import net.zjvis.flint.data.hub.service.graph.GraphMapService;
import net.zjvis.flint.data.hub.service.graph.GraphService;
import net.zjvis.flint.data.hub.service.graph.JobService;
import net.zjvis.flint.data.hub.util.JobStatusEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/graph")
public class GraphController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphController.class);
    private final GraphService graphService;
    private final JobService jobService;
    private final GraphMapService graphMapService;
    private final AccountUtils accountUtils;

    public GraphController(
        GraphService graphService,
        JobService jobService,
        GraphMapService graphMapService,
        AccountUtils accountUtils) {
        this.graphService = graphService;
        this.jobService = jobService;
        this.graphMapService = graphMapService;
        this.accountUtils = accountUtils;
    }


    @Operation(summary = "Execute graph job asynchronously")
    @PostMapping("/execute")
    public StandardResponse<String> executeGraphAsync(
        @Parameter(description = "To be executed graph job info")
        @RequestBody GraphVO graphVO) {
        try {
            LOGGER.info("[executeGraphAsync] start, graphVO={}", graphVO.toString());
            graphService.initGraphJob(graphVO);
            graphService.executeAsync(graphVO);
        } catch (Exception e) {
            LOGGER.error("[executeGraphAsync] failed, error={}", e);
            updateGraphJobStatus(graphVO);
            return StandardResponse.<String>builder().code(Code.BAD_REQUEST.getValue())
                .message(e.toString()).build();
        }
        return StandardResponse.<String>builder().data(graphVO.getGraphUuid()).build();
    }

    private void updateGraphJobStatus(GraphVO graphVO) {
        LOGGER.info(
            "[updateGraphJobStatus] executeGraphAsync failed, start to updateGraphJobStatus, graphVO={}",
            graphVO.toString());
        String graphUuid = graphVO.getGraphUuid();
        try {
            GraphMap graphMap = Optional.ofNullable(
                    graphMapService.queryGraphMapByGraphUuid(graphUuid))
                .orElseThrow(
                    () -> new Exception(
                        String.format(
                            "[updateGraphJobStatus]cannot find graph map by graph uuid=%s",
                            graphUuid)));
            Long jobId = graphMap.getJobId();
            jobService.checkJob(jobId);
        } catch (Exception e) {
            LOGGER.error("[updateGraphJobStatus] failed, graphVO={}, error={}", graphVO.toString(),
                e.toString());
        }

    }

    @Operation(summary = "Cancel graph job")
    @PostMapping("/{graphUuid}/cancel")
    public StandardResponse<JobVO> cancelGraph(
        @Parameter(description = "To be canceled graph job id")
        @PathVariable String graphUuid) {
        Job job;
        JobVO jobVO;
        try {
            LOGGER.info("[cancelGraph] start, graphUuid={}", graphUuid);
            GraphMap graphMap = Optional.ofNullable(
                    graphMapService.queryGraphMapByGraphUuid(graphUuid))
                .orElseThrow(
                    () -> new Exception(
                        String.format("cannot find graph map by graph uuid=%s", graphUuid)));
            Long jobId = graphMap.getJobId();
            job = jobService.findJobById(jobId)
                .orElseThrow(
                    () -> new Exception(
                        String.format("cannot find job by graph uuid=%s", graphUuid)));

            jobVO = jobService.cancelOrStopGraphJobWithBuild(job.getId(), JobStatusEnum.CANCELED);
        } catch (Exception e) {
            LOGGER.error("[cancelGraph] failed, error={}", e.getMessage());
            return StandardResponse.<JobVO>builder().code(Code.BAD_REQUEST.getValue())
                .message(e.getMessage()).build();
        }
        return StandardResponse.<JobVO>builder().data(jobVO
        ).build();
    }

    @Operation(summary = "Stop graph job")
    @PostMapping("/{graphUuid}/stop")
    public StandardResponse<JobVO> stopGraph(
        @Parameter(description = "To be stopped graph job id")
        @PathVariable String graphUuid) {
        Job job;
        JobVO jobVO;
        try {
            LOGGER.info("[stopGraph] start, graphUuid={}", graphUuid);
            GraphMap graphMap = Optional.ofNullable(
                    graphMapService.queryGraphMapByGraphUuid(graphUuid))
                .orElseThrow(
                    () -> new Exception(
                        String.format("cannot find graph map by graph uuid=%s", graphUuid)));
            Long jobId = graphMap.getJobId();
            job = jobService.findJobById(jobId)
                .orElseThrow(
                    () -> new Exception(
                        String.format("cannot find job by graph uuid=%s", graphUuid)));
            jobVO = jobService.cancelOrStopGraphJobWithBuild(job.getId(), JobStatusEnum.STOPPED);
        } catch (Exception e) {
            LOGGER.error("[stopGraph] failed, error={}", e.getMessage());
            return StandardResponse.<JobVO>builder().code(Code.BAD_REQUEST.getValue())
                .message(e.getMessage()).build();
        }
        return StandardResponse.<JobVO>builder().data(jobVO
        ).build();
    }

    @Operation(summary = "Get graph job status")
    @GetMapping("/{graphUuid}/status")
    public StandardResponse<JobVO> getGraphJob(
        @Parameter(description = "Graph uuid of queried graph job info")
        @PathVariable String graphUuid) {
        Job job;
        JobVO jobVO;
        try {
            GraphMap graphMap = Optional.ofNullable(
                    graphMapService.queryGraphMapByGraphUuid(graphUuid))
                .orElseThrow(
                    () -> new Exception(
                        String.format("cannot find graph map by graph uuid=%s", graphUuid)));
            Long jobId = graphMap.getJobId();
            job = jobService.findJobById(jobId)
                .orElseThrow(
                    () -> new Exception(
                        String.format("cannot find job by graph uuid=%s", graphUuid)));
            jobVO = jobService.checkJob(job.getId());
        } catch (Exception e) {
            LOGGER.error("[getGraphJob] failed, error={}", e.getMessage());
            return StandardResponse.<JobVO>builder().code(Code.BAD_REQUEST.getValue())
                .message(e.getMessage()).build();
        }
        return StandardResponse.<JobVO>builder().data(jobVO)
            .build();
    }

    @Operation(summary = "Get my graph list")
    @GetMapping("/all")
    public StandardResponse<List<BusinessGraph>> getAllMyGraph() {
        try {
            String userId = accountUtils.currentUserIdOptional().orElseThrow(
                () -> new Exception("User id is empty")
            );
            return StandardResponse.<List<BusinessGraph>>builder()
                .data(graphService.getGraphListByUserId(userId)).build();
        } catch (Exception e) {
            LOGGER.error("[getMyAllGraph] failed, error={}", e.getMessage());
            return StandardResponse.<List<BusinessGraph>>builder()
                .code(Code.BAD_REQUEST.getValue())
                .message(e.getMessage()).build();
        }

    }

    @Operation(summary = "Get graph list of project")
    @GetMapping("/project/all")
    public StandardResponse<List<BusinessGraph>> getAllMyGraphOfProject(
        @RequestParam Long projectId
    ) {
        try {
            String userId = accountUtils.currentUserIdOptional().orElseThrow(
                () -> new Exception("User id is empty")
            );
            return StandardResponse.<List<BusinessGraph>>builder()
                .data(graphService.findByProjectId(projectId, userId)).build();
        } catch (Exception e) {
            LOGGER.error("[getMyAllGraph] failed, error={}", e.getMessage());
            return StandardResponse.<List<BusinessGraph>>builder()
                .code(Code.BAD_REQUEST.getValue())
                .message(e.getMessage()).build();
        }

    }

    @Operation(summary = "Get graph info")
    @GetMapping("/{graphUuid}")
    public StandardResponse<BusinessGraph> getGraphInfo(
        @Parameter(description = "Graph uid of queried graph info")
        @PathVariable String graphUuid) {
        LOGGER.info("[getGraphJob] start, graphUuid={}", graphUuid);
        return StandardResponse.<BusinessGraph>builder()
            .data(graphService.findByGraphUuid(graphUuid))
            .build();
    }

    @Operation(summary = "Save graph info")
    @PostMapping("")
    public StandardResponse<BusinessGraph> saveGraphInfo(
        @Parameter(description = "To be saved graph info")
        @RequestBody BusinessGraph businessGraph) {
        try {
            LOGGER.info("[saveGraphInfo] start, graph info={}", businessGraph.toString());
            String userId = accountUtils.currentUserIdOptional().orElseThrow(
                () -> new Exception("User id is empty")
            );
            if (StringUtils.isEmpty(businessGraph.getUniversalUserId())) {
                businessGraph = businessGraph.toBuilder()
                    .universalUserId(userId)
                    .build();
            }
            return StandardResponse.<BusinessGraph>builder().data(
                graphService.save(businessGraph)).build();
        } catch (Exception e) {
            LOGGER.error("[saveGraphInfo] failed, error={}", e.getMessage());
            return StandardResponse.<BusinessGraph>builder()
                .code(Code.BAD_REQUEST.getValue())
                .message(e.getMessage()).build();
        }
    }

    @Operation(summary = "Batch delete graph")
    @DeleteMapping("/batch")
    public StandardResponse<String> batchDeleteGraphs(
        // TODO: Consider to delete logically with a flag
        @Parameter(description = "To be deleted graph uuid list")
        @RequestParam List<String> graphUuidList) {
        LOGGER.info("[batchDeleteGraphs] start, graphUuidList={}", graphUuidList.toString());
        graphService.batchDeleteGraphs(graphUuidList);
        return StandardResponse.<String>builder()
            .data("success")
            .build();
    }

    @Operation(summary = "Get available sql tables for selected nodes")
    @PostMapping("/tables")
    public StandardResponse<SqlTableVO> getTableList(
        @Parameter(description = "Graph info for queried tables")
        @RequestBody GraphVO graphVO) {
        try {
            LOGGER.info("[getTableList] start, graphVO={}", graphVO.toString());
            return StandardResponse.<SqlTableVO>builder().data(graphService.getTableList(graphVO))
                .build();
        } catch (Exception e) {
            LOGGER.error("[getTableList] failed, error={}", e.getMessage());
            return StandardResponse.<SqlTableVO>builder().code(Code.BAD_REQUEST.getValue())
                .message(e.getMessage()).build();
        }
    }
}
