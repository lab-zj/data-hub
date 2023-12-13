package net.zjvis.flint.data.hub.service.algorithm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ScalableResource;
import net.zjvis.flint.data.hub.bean.AlgorithmConfiguration;
import net.zjvis.flint.data.hub.entity.algorithm.Algorithm;
import net.zjvis.flint.data.hub.entity.algorithm.AlgorithmDeploy;
import net.zjvis.flint.data.hub.entity.algorithm.BusinessAlgorithm;
import net.zjvis.flint.data.hub.exception.BugException;
import net.zjvis.flint.data.hub.lib.cache.CacheManager;
import net.zjvis.flint.data.hub.repository.algorithm.AlgorithmDeployRepository;
import net.zjvis.flint.data.hub.util.ActionStatusEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlgorithmDeployService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlgorithmDeployService.class);
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper(
            new YAMLFactory().enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE));
    private static final String KEY = "content";
    private final AlgorithmDeployRepository algorithmDeployRepository;
    private final BusinessAlgorithmService businessAlgorithmService;
    private final KubernetesClient kubernetesClient;
    private final String defaultRegistry;
    private final String namespace;
    private final String algorithmDeployJobTemplate;
    private final String callbackServer;
    private final String serviceAccountName;
    private final String configMapName;
    private final CacheManager<Long, String> tokenCache;

    public AlgorithmDeployService(
            AlgorithmDeployRepository algorithmDeployRepository,
            BusinessAlgorithmService businessAlgorithmService,
            KubernetesClient kubernetesClient,
            AlgorithmConfiguration algorithmConfiguration
    ) {
        this.algorithmDeployRepository = algorithmDeployRepository;
        this.businessAlgorithmService = businessAlgorithmService;
        this.kubernetesClient = kubernetesClient;
        this.namespace = algorithmConfiguration.getNamespace();
        this.defaultRegistry = algorithmConfiguration.getRegistry();
        this.algorithmDeployJobTemplate = algorithmConfiguration.getAlgorithmDeployJobTemplate();
        this.callbackServer = algorithmConfiguration.callbackServer();
        this.serviceAccountName = algorithmConfiguration.getServiceAccountName();
        this.configMapName = algorithmConfiguration.getConfigmapName();
        this.tokenCache = CacheManager.<Long, String>builder()
                .timeoutSeconds(3600L)
                .maximumSize(1000)
                .loader(id -> UUID.randomUUID().toString().replaceAll("-", ""))
                .build();
    }

    public AlgorithmDeploy deploy(Algorithm algorithm, String name, String version)
            throws JsonProcessingException {
        return deploy(algorithm, name, version, true);
    }

    public AlgorithmDeploy deploy(Algorithm algorithm, String name, String version, boolean ignoreJobExists)
            throws JsonProcessingException {
        Optional<AlgorithmDeploy> algorithmDeployOptional = findByAlgorithmId(algorithm.getId());
        if (algorithmDeployOptional
                .map(algorithmDeploy -> !algorithmDeploy.getStatus().stopped())
                .orElse(false)
        ) {
            throw new IllegalArgumentException(
                    String.format("algorithmDeploy(%s) already exists and is not stopped",
                            algorithmDeployOptional.get()));
        }
        BusinessAlgorithm businessAlgorithm = businessAlgorithmService.findByAlgorithmId(algorithm.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("algorithm not found by id(%s)", algorithm.getId())));
        String algorithmDeployName = String.format("data-hub-%s-%s", businessAlgorithm.getId(),
            businessAlgorithm.getUniversalUserId());
        final String applicationValues = constructValuesYaml(
                algorithm.getValuesYaml(), algorithmDeployName, version, businessAlgorithm);
        final String secretName = String.format("secret-%s", algorithm.getId());
        saveSecret(secretName, applicationValues);
        AlgorithmDeploy algorithmDeploy = algorithmDeployOptional
                .map(algorithmDeployFound -> algorithmDeployFound.toBuilder()
                        .status(ActionStatusEnum.CREATED)
                        .gmtCreate(LocalDateTime.now())
                        .gmtModify(LocalDateTime.now())
                        .build()
                ).orElseGet(() -> algorithmDeployRepository.save(
                        AlgorithmDeploy.builder()
                                .algorithm(algorithm)
                                .status(ActionStatusEnum.CREATED)
                                .gmtCreate(LocalDateTime.now())
                                .gmtModify(LocalDateTime.now())
                                .valuesContent(applicationValues)
                                .jobName(String.format("algorithm-deploy-%s", algorithm.getId()))
                                .build()
                ));
        ScalableResource<Job> existingJobResource = kubernetesClient.batch()
                .v1()
                .jobs()
                .inNamespace(namespace)
                .withName(algorithmDeploy.getJobName());
        if (null != existingJobResource.get()) {
            if (!ignoreJobExists) {
                throw new RuntimeException(String.format("job(%s) already exists in namespace(%s)",
                        algorithmDeploy.getJobName(), namespace));
            }
            existingJobResource.delete();
        }
        String token = tokenCache.getUnchecked(algorithmDeploy.getId());
        Job job = templateJob();
        PodSpec podSpec = job.getSpec()
                .getTemplate()
                .getSpec();
        configureVolumes(podSpec, secretName, configMapName);
        configureServiceAccount(podSpec, serviceAccountName);
        configureContainer(
                algorithmDeploy.getId(),
                algorithmDeployName,
                version,
                token,
                podSpec.getContainers()
                        .stream()
                        .filter(container -> StringUtils.equals("deployer", container.getName()))
                        .findFirst()
                        .orElseThrow(() -> new BugException("'deployer' container not found"))
        );
        job.getMetadata().setName(algorithmDeploy.getJobName());
        kubernetesClient.batch()
                .v1()
                .jobs()
                .resource(job)
                .createOrReplace();
        return algorithmDeployRepository.save(algorithmDeploy.toBuilder()
                .status(ActionStatusEnum.RUNNING)
                .build());
    }

    public AlgorithmDeploy callback(Long algorithmDeployId, String callbackToken, boolean succeed)
            throws JsonProcessingException {
        AlgorithmDeploy algorithmDeploy = algorithmDeployRepository.findById(algorithmDeployId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("algorithmDeploy not found by id(%s)", algorithmDeployId)));
        String token = tokenCache.getUnchecked(algorithmDeployId);
        if (!StringUtils.equals(callbackToken, token)) {
            throw new IllegalArgumentException(String.format(
                    "algorithm(id=%s) token(%s) not matches", algorithmDeployId, token));
        }
        if (succeed) {
            updateServerAddress(algorithmDeploy.getValuesContent(), algorithmDeploy.getAlgorithm().getId());
        } else {
            LOGGER.info("[AlgorithmDeployService-callback] failed, start to delete algorithm={}", algorithmDeploy.getAlgorithm().getId());
            businessAlgorithmService.deleteByAlgorithmId(
                algorithmDeploy.getAlgorithm().getId());
        }
        return algorithmDeployRepository.save(algorithmDeploy.toBuilder()
                .status(succeed ? ActionStatusEnum.SUCCEED : ActionStatusEnum.FAILED)
                .gmtModify(LocalDateTime.now())
                .build());
    }

    public Optional<AlgorithmDeploy> findByAlgorithmId(Long id) {
        return algorithmDeployRepository.findByAlgorithmId(id);
    }

    public List<AlgorithmDeploy> findByAlgorithmIdIn(List<Long> algorithmIds) {
        return algorithmDeployRepository.findByAlgorithmIdIn(algorithmIds);
    }

    public AlgorithmDeploy cancel(Long algorithmId, boolean ignoreNotRunning) {
        Optional<AlgorithmDeploy> algorithmDeployOptional = findByAlgorithmId(algorithmId);
        if (algorithmDeployOptional.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("algorithmRelease not found by id(%s)", algorithmId));
        }
        AlgorithmDeploy algorithmDeploy = algorithmDeployOptional.get();
        if (!ActionStatusEnum.RUNNING.equals(algorithmDeploy.getStatus())) {
            if (!ignoreNotRunning) {
                throw new IllegalArgumentException(String.format(
                        "algorithmRelease(id=%s) is not running", algorithmDeploy.getId()));
            }
            return algorithmDeploy;
        }
        kubernetesClient.batch()
                .v1()
                .jobs()
                .withName(algorithmDeploy.getJobName())
                .delete();
        // TODO check if job is canceled
        return algorithmDeployRepository.save(algorithmDeploy.toBuilder()
                .status(ActionStatusEnum.CANCELED)
                .gmtModify(LocalDateTime.now())
                .build());
    }

    private Job templateJob() {
        Job job = kubernetesClient.batch()
                .v1()
                .jobs()
                .load(new ByteArrayInputStream(
                        algorithmDeployJobTemplate.getBytes(StandardCharsets.UTF_8)))
                .get();
        job.getMetadata().setNamespace(namespace);
        return job;
    }

    private String constructValuesYaml(
            String content,
            String repository,
            String version,
            BusinessAlgorithm businessAlgorithm
    ) throws JsonProcessingException {
        String registry = StringUtils.isEmpty(businessAlgorithm.getAlgorithm().getRegistry())
                ? defaultRegistry
                : businessAlgorithm.getAlgorithm().getRegistry();
        Map<String, Object> valuesMapping = OBJECT_MAPPER.readValue(content, new TypeReference<>() {
        });
        Map<String, String> contentMapping = Map.of(
                "repository", String.format("%s/docker.io/%s", registry, repository),
                "tag", version);
        valuesMapping.put("image", contentMapping);
        // update map of key = hosts
        Map<String, Object> hostsMap = getIngressHostsMap(valuesMapping);
        List<Object> pathList = OBJECT_MAPPER.readValue(
                OBJECT_MAPPER.writeValueAsString(hostsMap.get("paths")),
                new TypeReference<>() {
                });
        Map<String, Object> pathMap = OBJECT_MAPPER.readValue(
                OBJECT_MAPPER.writeValueAsString(pathList.get(0)),
                new TypeReference<>() {
                });
        pathMap.put("path", String.format("/algorithms/%s/?(.*)", businessAlgorithm.getId()));
        pathList.set(0, pathMap);
        hostsMap.put("paths", pathList);
        updateIngressHostsMap(valuesMapping, hostsMap);
        return OBJECT_MAPPER.writeValueAsString(valuesMapping);
    }

    private void saveSecret(String secretName, String valuesToSave) {
        Map<String, String> dataWithValueEncoded = Map.of(KEY, valuesToSave)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Base64.getEncoder()
                                .encodeToString(entry.getValue().getBytes(StandardCharsets.UTF_8))
                ));
        kubernetesClient.secrets()
                .inNamespace(namespace)
                .resource(new SecretBuilder()
                        .withNewMetadata()
                        .withName(secretName)
                        .endMetadata()
                        .addToData(dataWithValueEncoded)
                        .build())
                .createOrReplace();
    }

    private void configureVolumes(PodSpec podSpec, String secretName, String configMapName) {
        SecretVolumeSource volumes = podSpec.getVolumes()
                .stream()
                .filter(volume -> StringUtils.equals("nebula-algorithm-values", volume.getName()))
                .findFirst()
                .orElseThrow(() -> new BugException("'nebula-algorithm-values' volume not found"))
                .getSecret();
        volumes.setSecretName(secretName);
        ConfigMapVolumeSource configMapVolumeSource = podSpec.getVolumes()
                .stream()
                .filter(volume -> StringUtils.equals("nebula-algorithm-init", volume.getName()))
                .findFirst()
                .orElseThrow(() -> new BugException("'nebula-algorithm-init' volume not found"))
                .getConfigMap();
        configMapVolumeSource.setName(configMapName);
    }

    private void configureServiceAccount(PodSpec podSpec, String serviceAccountName) {
        podSpec.setServiceAccountName(serviceAccountName);
    }

    private void configureContainer(
            Long algorithmDeployId,
            String name,
            String version,
            String token,
            Container buildContainer
    ) {
        for (EnvVar envVar : buildContainer.getEnv()) {
            // TODO rename env names
            switch (envVar.getName()) {
                case "ALGORITHM_NAME":
                    envVar.setValue(name);
                    break;
                case "ALGORITHM_VERSION":
                    envVar.setValue(version);
                    break;
                case "ALGORITHM_NAMESPACE":
                    envVar.setValue(namespace);
                    break;
                case "CALLBACK_ADDRESS":
                    envVar.setValue(
                            String.format("%s/algorithm/deploy/%s/callback",
                                    callbackServer, algorithmDeployId));
                    break;
                case "CALLBACK_TOKEN":
                    envVar.setValue(token);
                    break;
                default:
                    // do nothing
            }
        }
    }

    private String constructOuterServerAddress(String valueContent, Long businessAlgorithmId)
            throws JsonProcessingException {
        Map<String, Object> valuesMapping = OBJECT_MAPPER.readValue(
                valueContent,
                new TypeReference<>() {
                });
        Map<String, Object> hostsMap = getIngressHostsMap(valuesMapping);
        String host = String.valueOf(hostsMap.get("host"));
        List<Object> pathList = OBJECT_MAPPER.readValue(
                OBJECT_MAPPER.writeValueAsString(hostsMap.get("paths")),
                new TypeReference<>() {
                });
        Map<String, Object> pathMap = OBJECT_MAPPER.readValue(
                OBJECT_MAPPER.writeValueAsString(pathList.get(0)),
                new TypeReference<>() {
                });
        String path = String.valueOf(pathMap.get("path"));
        // TODO: hardcode part of url now, optimize it later
        return host + "/algorithms/" + businessAlgorithmId;
    }

    private void updateServerAddress(String valueContent, Long algorithmId)
            throws JsonProcessingException {
        BusinessAlgorithm businessAlgorithm = businessAlgorithmService.findByAlgorithmId(algorithmId)
                .orElseThrow(() -> new BugException(
                        String.format("businessAlgorithm not found by id(%s)", algorithmId)));
        businessAlgorithmService.save(businessAlgorithm.toBuilder()
                .outerServerAddress(
                        constructOuterServerAddress(valueContent, businessAlgorithm.getId()))
                .build());
    }

    private Map<String, Object> getIngressHostsMap(Map<String, Object> valuesMapping)
            throws JsonProcessingException {
        Map<String, Object> ingressMapping = OBJECT_MAPPER.readValue(
                OBJECT_MAPPER.writeValueAsString(valuesMapping.get("ingress")),
                new TypeReference<>() {
                });
        List<Object> hostList = OBJECT_MAPPER.readValue(
                OBJECT_MAPPER.writeValueAsString(ingressMapping.get("hosts")),
                new TypeReference<>() {
                });
        return OBJECT_MAPPER.readValue(
                OBJECT_MAPPER.writeValueAsString(hostList.get(0)),
                new TypeReference<>() {
                });
    }

    private void updateIngressHostsMap(
            Map<String, Object> valuesMapping,
            Map<String, Object> hostsMap
    ) throws JsonProcessingException {
        Map<String, Object> ingressMapping = OBJECT_MAPPER.readValue(
                OBJECT_MAPPER.writeValueAsString(valuesMapping.get("ingress")),
                new TypeReference<>() {
                });
        List<Object> hostList = OBJECT_MAPPER.readValue(
                OBJECT_MAPPER.writeValueAsString(ingressMapping.get("hosts")),
                new TypeReference<>() {
                });
        hostList.set(0, hostsMap);
        ingressMapping.put("hosts", hostList);
        valuesMapping.put("ingress", ingressMapping);
    }
}
