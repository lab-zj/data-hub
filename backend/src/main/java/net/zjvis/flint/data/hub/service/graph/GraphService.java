package net.zjvis.flint.data.hub.service.graph;

import static java.util.Map.entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.zjvis.flint.data.hub.controller.filesystem.PathTransformer;
import net.zjvis.flint.data.hub.controller.graph.vo.GraphVO;
import net.zjvis.flint.data.hub.controller.graph.vo.SqlTableVO;
import net.zjvis.flint.data.hub.controller.graph.vo.TableInfoVO;
import net.zjvis.flint.data.hub.entity.algorithm.BusinessAlgorithm;
import net.zjvis.flint.data.hub.entity.graph.BusinessGraph;
import net.zjvis.flint.data.hub.entity.graph.GraphMap;
import net.zjvis.flint.data.hub.entity.graph.Job;
import net.zjvis.flint.data.hub.entity.graph.Task;
import net.zjvis.flint.data.hub.entity.graph.TaskRuntime;
import net.zjvis.flint.data.hub.lib.minio.MinioConnection;
import net.zjvis.flint.data.hub.lib.schedule.graph.Edge;
import net.zjvis.flint.data.hub.lib.schedule.graph.Graph;
import net.zjvis.flint.data.hub.lib.schedule.graph.Vertex;
import net.zjvis.flint.data.hub.lib.schedule.graph.Vertex.SupplierResult;
import net.zjvis.flint.data.hub.lib.schedule.scheduler.AsyncScheduler;
import net.zjvis.flint.data.hub.lib.schedule.scheduler.DefaultScheduler;
import net.zjvis.flint.data.hub.repository.graph.BusinessGraphRepository;
import net.zjvis.flint.data.hub.service.account.AccountUtils;
import net.zjvis.flint.data.hub.service.algorithm.BusinessAlgorithmService;
import net.zjvis.flint.data.hub.service.graph.planner.DefaultPlanner;
import net.zjvis.flint.data.hub.service.graph.planner.conf.AlgorithmVertexConfiguration;
import net.zjvis.flint.data.hub.service.graph.planner.conf.Configuration;
import net.zjvis.flint.data.hub.service.graph.planner.conf.DataVertexConfiguration;
import net.zjvis.flint.data.hub.service.graph.planner.conf.ETLVertexConfiguration;
import net.zjvis.flint.data.hub.service.graph.planner.conf.SqlVertexConfiguration;
import net.zjvis.flint.data.hub.util.JobStatusEnum;
import okhttp3.OkHttpClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class GraphService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private DefaultPlanner planner;
    private final DefaultScheduler defaultScheduler = DefaultScheduler.builder().build();
    private AsyncScheduler asyncScheduler = AsyncScheduler.builder().build();
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphService.class);
    private final JobService jobService;
    private final TaskRuntimeService taskRuntimeService;
    private final TaskService taskService;
    private final TableMapService tableMapService;
    private final GraphMapService graphMapService;
    private final MinioConnection minioConnection;
    private final BusinessGraphRepository businessGraphRepository;
    private final ThreadPoolTaskExecutor graphJobThreadPool;
    private final AccountUtils accountUtils;
    private final OkHttpClient okHttpClient;
    private final BusinessAlgorithmService businessAlgorithmService;
    private final PathTransformer pathTransformer;
    private final String bucketName;

    public GraphService(JobService jobService, TaskRuntimeService taskRuntimeService,
        TaskService taskService,
        MinioConnection minioConnection,
        TableMapService tableMapService,
        GraphMapService graphMapService,
        BusinessGraphRepository businessGraphRepository,
        ThreadPoolTaskExecutor graphJobThreadPool,
        AccountUtils accountUtils,
        @Value("${application.s3.minio.bucket.filesystem}") String bucketName,
        OkHttpClient okHttpClient,
        BusinessAlgorithmService businessAlgorithmService,
        PathTransformer pathTransformer) {
        this.jobService = jobService;
        this.taskRuntimeService = taskRuntimeService;
        this.taskService = taskService;
        this.tableMapService = tableMapService;
        this.graphMapService = graphMapService;
        this.minioConnection = minioConnection;
        this.businessGraphRepository = businessGraphRepository;
        this.graphJobThreadPool = graphJobThreadPool;
        this.accountUtils = accountUtils;
        this.planner = DefaultPlanner.builder()
            .taskRuntimeService(taskRuntimeService)
            .taskService(taskService)
            .minioConnection(minioConnection)
            .accountUtils(accountUtils)
            .bucketName(bucketName)
            .okHttpClient(okHttpClient)
            .pathTransformer(pathTransformer)
            .build();
        this.asyncScheduler = asyncScheduler.toBuilder().executor(graphJobThreadPool).build();
        this.okHttpClient = okHttpClient;
        this.businessAlgorithmService = businessAlgorithmService;
        this.bucketName = bucketName;
        this.pathTransformer = pathTransformer;
    }


    public String executeSync(GraphVO graphVO) throws Exception {
        Graph graph = constructGraph(graphVO);
        String graphUuid = graphVO.getGraphUuid();
        GraphMap graphMap = Optional.ofNullable(graphMapService.queryGraphMapByGraphUuid(graphUuid))
            .orElseThrow(
                () -> new Exception(
                    String.format("cannot find graph map by graph uuid=%s", graphUuid)));
        Long jobId = graphMap.getJobId();
        startGraphJob(jobId);
        defaultScheduler.schedule(graph);
        jobService.checkJob(jobId);
        return graphUuid;
    }

    public String executeAsync(GraphVO graphVO) throws Exception {
        Graph graph = constructGraph(graphVO);
        String graphUuid = graphVO.getGraphUuid();
        GraphMap graphMap = Optional.ofNullable(graphMapService.queryGraphMapByGraphUuid(graphUuid))
            .orElseThrow(
                () -> new Exception(
                    String.format("cannot find graph map by graph uuid=%s", graphUuid)));
        Long jobId = graphMap.getJobId();
        startGraphJob(jobId);
        asyncScheduler.schedule(graph);
        jobService.checkJob(jobId);
        return graphUuid;
    }

    public Job initGraphJob(GraphVO graphVO) throws Exception {
        LOGGER.info("[initGraphJob] start, graphVO={}", graphVO.toString());
        GraphMap graphMap = graphMapService.queryGraphMapByGraphUuid(graphVO.getGraphUuid());
        LOGGER.info("[initGraphJob] found graph map={} by graph uuid={}", graphMap,
            graphVO.getGraphUuid());
        Job initedJob = Job.builder()
            .graphUuid(graphVO.getGraphUuid())
            .param(OBJECT_MAPPER.writeValueAsString(graphVO))
            .status(JobStatusEnum.INIT.getName())
            .gmtCreate(LocalDateTime.now())
            .gmtModify(LocalDateTime.now())
            .build();
        if (graphMap != null) {
            Job existedGraphJob = jobService.findJobById(graphMap.getJobId())
                .orElseThrow(() -> new Exception(
                    String.format("cannot find job by id=%s", graphMap.getJobId())));
            initedJob = initedJob.toBuilder()
                .id(existedGraphJob.getId())
                .build();
        }
        Job updatedJob = jobService.saveJob(initedJob);
        LOGGER.info("[initGraphJob] inited graph job={}", updatedJob.toString());
        if (graphMap == null) {
            graphMapService.saveGraphMap(
                GraphMap.builder()
                    .graphUuid(graphVO.getGraphUuid())
                    .jobId(updatedJob.getId())
                    .gmtCreate(LocalDateTime.now())
                    .gmtModify(LocalDateTime.now())
                    .build()
            );
        }
        return updatedJob;
    }

    public List<BusinessGraph> getGraphListByUserId(String userId) {
        return businessGraphRepository.findByUniversalUserId(userId);
    }

    public BusinessGraph findByGraphUuid(String graphUuid) {
        return businessGraphRepository.findByGraphUuid(graphUuid).get(0);
    }

    public List<BusinessGraph> findByProjectId(Long projectId, String userId) {
        return businessGraphRepository.findByProjectIdAndUniversalUserId(projectId, userId);
    }

    public BusinessGraph save(BusinessGraph businessGraph) {
        businessGraph = businessGraph.toBuilder()
            .gmtModify(LocalDateTime.now())
            .gmtCreate(businessGraph.getGmtCreate() == null ? LocalDateTime.now()
                : businessGraph.getGmtCreate())
            .graphUuid(StringUtils.isEmpty(businessGraph.getGraphUuid()) ? UUID.randomUUID()
                .toString()
                .replaceAll("-", "")
                : businessGraph.getGraphUuid())
            .build();
        return businessGraphRepository.save(
            businessGraph);
    }

    public void batchDeleteGraphs(List<String> graphUuidList) {
        businessGraphRepository.deleteByGraphUuidIn(graphUuidList);
    }

    public SqlTableVO getTableList(GraphVO graphVO) throws Exception {

        Map<String, List<TableInfoVO>> tableList = graphVO.getVertexIdList().stream()
            .collect(Collectors.toMap(id -> id, id -> {
                try {
                    return taskRuntimeService.getTableNamesByTaskUuid(id);
                } catch (Exception e) {
                    LOGGER.error("[getTableList] failed, error={}", e.getMessage());
                    throw new RuntimeException(e);
                }
            }));

        LOGGER.info("[getTableList] finished, tableList={}",
            OBJECT_MAPPER.writeValueAsString(tableList));
        return SqlTableVO.builder()
            .tableList(tableList)
            .build();
    }

    private Graph<Vertex.SupplierResult> constructGraph(GraphVO graphVO)
        throws JsonProcessingException {
        Graph<Vertex.SupplierResult> graph = Graph.builder()
            .name(graphVO.getGraphUuid())
            .build();

        // 1. vertexes handler
        Map<String, Vertex<SupplierResult>> vertexIndex = graphVO.getVertexIdList()
            .stream()
            .map(id -> DefaultVertex.builder()
                .name(String.valueOf(id))
                .build())
            .collect(Collectors.toMap(
                Vertex::getName,
                vertex -> vertex
            ));
        vertexIndex.values().forEach(graph::addVertex);

        // 2. edge handler
        // group edge param index firstly(for the situation of multiple-edge from the same vertex)
        Map<String, List<Map<String, List<Map<String, String>>>>> paramMapAll = getEdgeParams(
            graphVO);
        LOGGER.info("[constructGraph] graphUuid={}, get paramMapAll={}",
            graphVO.getGraphUuid(), OBJECT_MAPPER.writeValueAsString(paramMapAll));
        graphVO.getEdgeList().forEach(edgeVO -> {
            graph.addEdge(vertexIndex.get(edgeVO.getFrom().getUuid()),
                vertexIndex.get(edgeVO.getTo().getUuid()),
                Edge.builder().params(
                        paramMapAll.get(edgeVO.getFrom().getUuid()).stream()
                            .filter(item -> item.get(edgeVO.getTo().getUuid()) != null)
                            .findAny().get()
                            .get(edgeVO.getTo().getUuid())
                    )
                    .build());
        });
        // 3. fill up supplier
        List<Vertex> updatedVertexList = new ArrayList<>();
        Iterator<Vertex<SupplierResult>> topologicalIterator = graph.topologicalIterator();
        while (topologicalIterator.hasNext()) {
            Vertex vertex = topologicalIterator.next();
            try {
                Vertex<Vertex.SupplierResult> updatedVertex = DefaultVertex.builder()
                    .name(vertex.getName())
                    .supplier(DefaultVertex.DefaultSupplier.builder()
                        .configuration(constructVertexConfiguration(graph, vertex,
                            graph.sourceVertexSet(vertex),
                            graph.outGoingVertexSet(vertex),
                            graphVO.getSelectedVertexIdList()))
                        .planner(planner)
                        .build())
                    .build();
                updatedVertexList.add(updatedVertex);
            } catch (Exception e) {
                LOGGER.error("[constructGraph] failed, graph name={}, vertex id={}, error={}",
                    graph.getName(), vertex.getName(), e.getMessage());
            }
        }

        // update supplier and reconstructor graph  [need to optimized later]
        Graph updatedGraph = Graph.builder()
            .name(RandomStringUtils.randomAlphabetic(8))
            .build();
        Map<String, Vertex<SupplierResult>> updatedVertexIndex = updatedVertexList.stream()
            .collect(Collectors.toMap(
                Vertex::getName,
                vertex -> vertex
            ));
        updatedVertexIndex.values().forEach(updatedGraph::addVertex);
        // to be optimized (not for each edgeVO, should for each real graph edge)
        graphVO.getEdgeList().forEach(edgeVO -> {
            updatedGraph.addEdge(updatedVertexIndex.get(edgeVO.getFrom().getUuid()),
                updatedVertexIndex.get(edgeVO.getTo().getUuid()),
                Edge.builder().params(
                        paramMapAll.get(edgeVO.getFrom().getUuid()).stream()
                            .filter(item -> item.get(edgeVO.getTo().getUuid()) != null)
                            .findAny().get()
                            .get(edgeVO.getTo().getUuid())
                    )
                    .build());
        });
        return updatedGraph;
    }

    private Configuration constructVertexConfiguration(
        Graph<Vertex.SupplierResult> graph,
        Vertex<Vertex.SupplierResult> curVertex,
        Set<Vertex<SupplierResult>> incomingVertexes,
        Set<Vertex<SupplierResult>> outgoingVertexes,
        List<String> selectedVertexIdList) throws Exception {
        String taskUuid = curVertex.getName();
        Task task = taskService.findByTaskUuid(taskUuid).orElseThrow(
            () -> new Exception(String.format("cannot find task by uuid=%s", taskUuid)));
        boolean selected = !((selectedVertexIdList != null) && (!selectedVertexIdList.isEmpty())
            && !selectedVertexIdList.contains(
            taskUuid));

        if (task.isSQL()) {
            return SqlVertexConfiguration.builder()
                .selectSql(task.getConfiguration())
                .meta(constructConfigurationMeta(graph, curVertex, incomingVertexes, selected))
                .outgoingMeta(
                    constructConfigurationOutgoingMeta(graph, curVertex, outgoingVertexes))
                .task(task)
                .taskRuntimeService(taskRuntimeService)
                .selected(selected)
                .build();
        } else if (task.isData()) {
            return DataVertexConfiguration.builder()
                .meta(constructConfigurationMeta(graph, curVertex, incomingVertexes, selected))
                .task(task)
                .taskRuntimeService(taskRuntimeService)
                .selected(selected)
                .build();
        } else if (task.isAlgorithm()) {
            BusinessAlgorithm businessAlgorithm = businessAlgorithmService.findById(
                Long.valueOf(task.getConfigurationReference())).orElseThrow(
                () -> new Exception(
                    String.format("Cannot find businessAlgorithm by id=%s",
                        task.getConfigurationReference()))
            );
            return AlgorithmVertexConfiguration.builder()
                .meta(constructConfigurationMeta(graph, curVertex, incomingVertexes, selected))
                .outgoingMeta(
                    constructConfigurationOutgoingMeta(graph, curVertex, outgoingVertexes))
                .task(task)
                .taskRuntimeService(taskRuntimeService)
                .taskService(taskService)
                .jobService(jobService)
                .selected(selected)
                .okHttpClient(okHttpClient)
                .algorithmServerAddress(
                    String.format("http://%s/%s", businessAlgorithm.getOuterServerAddress(),
                        "algorithms/invoke")
                )
                .algorithmConfiguration(businessAlgorithm.getAlgorithm().getConfigurationYaml())
                .build();
        } else if (task.isETL()) {
            return ETLVertexConfiguration.builder()
                .meta(constructConfigurationMeta(graph, curVertex, incomingVertexes, selected))
                .outgoingMeta(
                    constructConfigurationOutgoingMeta(graph, curVertex, outgoingVertexes))
                .task(task)
                .taskRuntimeService(taskRuntimeService)
                .selected(selected)
                .build();
        } else {
            // TODO:
            LOGGER.error("[constructVertexConfiguration] Not supported task type={}", task);
            return null;
        }
    }

    private Map<String, List<Map<String, String>>> constructConfigurationMeta(
        Graph<Vertex.SupplierResult> graph,
        Vertex<Vertex.SupplierResult> curVertex,
        Set<Vertex<SupplierResult>> incomingVertexes,
        boolean selected) throws Exception {
        Map<String, List<Map<String, String>>> meta = new HashMap<>();
        incomingVertexes.stream().forEach(preVertex -> {
                Edge edge = graph.getEdge(preVertex, curVertex);
                // TODO: check later: task param key should be unique in all graph
                meta.put(preVertex.getName(), edge.getParams());
            }
        );
        if (selected) {
            initBuild(curVertex.getName(), graph.getName(), meta);
        }
        return meta;
    }

    private Map<String, List<Map<String, String>>> constructConfigurationOutgoingMeta(
        Graph<Vertex.SupplierResult> graph,
        Vertex<Vertex.SupplierResult> curVertex,
        Set<Vertex<SupplierResult>> outgoingVertexes) {
        Map<String, List<Map<String, String>>> meta = new HashMap<>();
        outgoingVertexes.stream().forEach(nextVertex -> {
                Edge edge = graph.getEdge(curVertex, nextVertex);
                meta.put(nextVertex.getName(), edge.getParams());
            }
        );
        return meta;
    }

    private Map getEdgeParams(GraphVO graphVO) {
        Map<String, List<Map<String, List<Map<String, String>>>>> paramMapAll = new HashMap<>();
        graphVO.getEdgeList().forEach(edgeVO -> {
            List<Map<String, List<Map<String, String>>>> paramMapList = paramMapAll.get(
                edgeVO.getFrom().getUuid());
            Map<String, String> fromToParamMap = Map.ofEntries(
                entry(String.valueOf(edgeVO.getFrom().getParam()),
                    String.valueOf(edgeVO.getTo().getParam()))
            );
            LOGGER.info("[getEdgeParams] graphUuid={}, paramMapList={}", graphVO.getGraphUuid(),
                paramMapList);
            if (CollectionUtils.isEmpty(paramMapList)) {
                paramMapAll.put(edgeVO.getFrom().getUuid(),
                    new ArrayList<>(Arrays.asList(Map.ofEntries(
                        entry(edgeVO.getTo().getUuid(),
                            new ArrayList<>(Arrays.asList(fromToParamMap)))))));
            } else {
                Map<String, List<Map<String, String>>> tempMap = paramMapList.stream()
                    .filter(item -> item.get(edgeVO.getTo().getUuid()) != null)
                    .findAny().orElse(null);
                if (tempMap == null) {
                    paramMapList.add(Map.ofEntries(
                        entry(edgeVO.getTo().getUuid(),
                            new ArrayList<>(Arrays.asList(fromToParamMap
                            )))
                    ));
                } else {
                    Map<String, List<Map<String, String>>> paramVertexToMap = new HashMap<>(tempMap
                    );
                    List<Map<String, String>> fromToParamMapList = paramVertexToMap.get(
                        edgeVO.getTo().getUuid());
                    fromToParamMapList.add(fromToParamMap);
                }
            }

        });
        return paramMapAll;
    }

    private TaskRuntime initBuild(String taskUuid, String graphUid,
        Map<String, List<Map<String, String>>> meta)
        throws Exception {
        GraphMap graphMap = Optional.ofNullable(graphMapService.queryGraphMapByGraphUuid(graphUid))
            .orElseThrow(
                () -> new Exception(
                    String.format("cannot find graph map by graph uid=%s", graphUid)));
        Long jobId = graphMap.getJobId();
        Optional<TaskRuntime> buildOptional = taskRuntimeService.findByTaskUuid(taskUuid);
        TaskRuntime initedTaskRuntime = TaskRuntime.builder()
            .taskUuid(taskUuid)
            .jobId(jobId)
            .param(OBJECT_MAPPER.writeValueAsString(meta))
            .status(JobStatusEnum.INIT.getName())
            .gmtCreate(LocalDateTime.now())
            .gmtModify(LocalDateTime.now())
            .build();
        if (buildOptional.isPresent()) {
            initedTaskRuntime = initedTaskRuntime.toBuilder().id(buildOptional.get().getId())
                .build();
        }
        return taskRuntimeService.saveBuild(initedTaskRuntime);
    }

    private Job startGraphJob(Long jobId) throws Exception {
        Job job = jobService.findJobById(jobId).orElseThrow(
            () -> new Exception(String.format("cannot find job by id=%s", jobId)));
        return jobService.saveJob(
            job.toBuilder()
                .status(JobStatusEnum.START.getName())
                .gmtModify(LocalDateTime.now())
                .build());
    }


}
