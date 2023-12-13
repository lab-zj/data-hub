package net.zjvis.flint.data.hub.service.algorithm;

import static net.zjvis.flint.data.hub.util.AlgorithmServerConstant.ALGO_FILE_TAR;

import io.fabric8.kubernetes.api.model.ConfigMapVolumeSource;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ScalableResource;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import net.zjvis.flint.data.hub.bean.AlgorithmConfiguration;
import net.zjvis.flint.data.hub.controller.filesystem.PathTransformer;
import net.zjvis.flint.data.hub.entity.algorithm.Algorithm;
import net.zjvis.flint.data.hub.entity.algorithm.AlgorithmRelease;
import net.zjvis.flint.data.hub.entity.algorithm.BusinessAlgorithm;
import net.zjvis.flint.data.hub.entity.artifact.BusinessDockerRegistryImage;
import net.zjvis.flint.data.hub.entity.artifact.DockerRegistryImage;
import net.zjvis.flint.data.hub.exception.BugException;
import net.zjvis.flint.data.hub.lib.cache.CacheManager;
import net.zjvis.flint.data.hub.lib.minio.MinioConnection;
import net.zjvis.flint.data.hub.repository.algorithm.AlgorithmReleaseRepository;
import net.zjvis.flint.data.hub.repository.algorithm.BusinessAlgorithmRepository;
import net.zjvis.flint.data.hub.repository.artifact.BusinessDockerRegistryImageRepository;
import net.zjvis.flint.data.hub.util.ActionStatusEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AlgorithmReleaseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlgorithmReleaseService.class);
    private final AlgorithmReleaseRepository algorithmReleaseRepository;
    private final BusinessAlgorithmRepository businessAlgorithmRepository;
    private final BusinessDockerRegistryImageRepository businessDockerRegistryImageRepository;
    private final KubernetesClient kubernetesClient;
    private final String namespace;
    private final String minioBucket;
    private final CacheManager<Long, String> tokenCache;
    private final String algorithmReleaseJobTemplate;
    private final String callbackServer;
    private final String configMapName;
    private final MinioConnection minioConnection;
    private final PathTransformer pathTransformer;
    private final BusinessAlgorithmService businessAlgorithmService;

    public AlgorithmReleaseService(
        AlgorithmReleaseRepository algorithmReleaseRepository,
        BusinessAlgorithmRepository businessAlgorithmRepository,
        BusinessDockerRegistryImageRepository businessDockerRegistryImageRepository,
        KubernetesClient kubernetesClient,
        AlgorithmConfiguration algorithmConfiguration,
        MinioConnection minioConnection,
        PathTransformer pathTransformer,
        BusinessAlgorithmService businessAlgorithmService
    ) {
        this.algorithmReleaseRepository = algorithmReleaseRepository;
        this.businessAlgorithmRepository = businessAlgorithmRepository;
        this.businessDockerRegistryImageRepository = businessDockerRegistryImageRepository;
        this.kubernetesClient = kubernetesClient;
        this.namespace = algorithmConfiguration.getNamespace();
        this.minioBucket = algorithmConfiguration.getMinioBucket();
        this.minioConnection = minioConnection;
        this.tokenCache = CacheManager.<Long, String>builder()
            .timeoutSeconds(3600L)
            .maximumSize(1000)
            .loader(id -> UUID.randomUUID().toString().replaceAll("-", ""))
            .build();
        this.algorithmReleaseJobTemplate = algorithmConfiguration.getAlgorithmReleaseJobTemplate();
        this.callbackServer = algorithmConfiguration.callbackServer();
        this.configMapName = algorithmConfiguration.getConfigmapName();
        this.pathTransformer = pathTransformer;
        this.businessAlgorithmService = businessAlgorithmService;
    }

    public AlgorithmRelease release(Algorithm algorithm, String name, String version) {
        return release(algorithm, name, version, true);
    }

    public AlgorithmRelease release(Algorithm algorithm, String name, String version,
        boolean ignoreJobExists) {
        Optional<AlgorithmRelease> algorithmReleaseOptional = findByAlgorithmId(algorithm.getId());
        if (algorithmReleaseOptional
            .map(algorithmRelease -> !algorithmRelease.getStatus().stopped())
            .orElse(false)) {
            throw new IllegalArgumentException(
                String.format("algorithmRelease(%s) already exists and is not stopped",
                    algorithmReleaseOptional.get()));
        }
        AlgorithmRelease algorithmRelease = algorithmReleaseOptional
            .map(algorithmReleaseFound -> algorithmReleaseFound.toBuilder()
                .status(ActionStatusEnum.CREATED)
                .gmtCreate(LocalDateTime.now())
                .gmtModify(LocalDateTime.now())
                .build()
            ).orElseGet(() -> algorithmReleaseRepository.save(
                AlgorithmRelease.builder()
                    .algorithm(algorithm)
                    .status(ActionStatusEnum.CREATED)
                    .gmtCreate(LocalDateTime.now())
                    .gmtModify(LocalDateTime.now())
                    .jobName(String.format("algorithm-release-%s", algorithm.getId()))
                    .build()
            ));
        BusinessAlgorithm businessAlgorithm = businessAlgorithmRepository.findByAlgorithmId(
            algorithmRelease.getAlgorithm().getId()).get();
        ScalableResource<Job> existingJobResource = kubernetesClient.batch()
            .v1()
            .jobs()
            .inNamespace(namespace)
            .withName(algorithmRelease.getJobName());
        if (null != existingJobResource.get()) {
            if (!ignoreJobExists) {
                throw new RuntimeException(String.format("job(%s) already exists in namespace(%s)",
                    algorithmRelease.getJobName(), namespace));
            }
            existingJobResource.delete();
        }
        String token = tokenCache.getUnchecked(algorithmRelease.getId());
        Job job = templateJob();
        PodSpec podSpec = job.getSpec()
            .getTemplate()
            .getSpec();
        configureFetchResourceContainer(
            algorithmRelease,
            podSpec.getInitContainers()
                .stream()
                .filter(container -> StringUtils.equals("fetch-resource", container.getName()))
                .findFirst()
                .orElseThrow(() -> new BugException(
                    "'fetch-resource' not found in init-containers not found")),
            businessAlgorithm.getUniversalUserId()
        );
        String algorithmReleaseName = String.format("data-hub-%s-%s", businessAlgorithm.getId(),
            businessAlgorithm.getUniversalUserId());
        configureBuildContainer(
            algorithmRelease,
            token,
            algorithmReleaseName,
            version,
            podSpec.getContainers()
                .stream()
                .filter(container -> StringUtils.equals("algorithm-builder", container.getName()))
                .findFirst()
                .orElseThrow(() -> new BugException("'algorithm-builder' container not found"))
        );
        configVolumes(podSpec, configMapName);
        job.getMetadata()
            .setName(algorithmRelease.getJobName());
        kubernetesClient.batch()
            .v1()
            .jobs()
            .resource(job)
            .createOrReplace();
        return algorithmReleaseRepository.save(algorithmRelease.toBuilder()
            .status(ActionStatusEnum.RUNNING)
            .build());
    }

    public Optional<AlgorithmRelease> findByAlgorithmId(Long id) {
        return algorithmReleaseRepository.findByAlgorithmId(id);
    }

    public AlgorithmRelease cancel(Long algorithmId, boolean ignoreNotRunning) {
        Optional<AlgorithmRelease> algorithmReleaseOptional
            = algorithmReleaseRepository.findByAlgorithmId(algorithmId);
        if (algorithmReleaseOptional.isEmpty()) {
            throw new IllegalArgumentException(
                String.format("algorithmRelease not found by id(%s)", algorithmId));
        }
        AlgorithmRelease algorithmRelease = algorithmReleaseOptional.get();
        if (!ActionStatusEnum.RUNNING.equals(algorithmRelease.getStatus())) {
            if (!ignoreNotRunning) {
                throw new IllegalArgumentException(String.format(
                    "algorithmRelease(id=%s) is not running", algorithmRelease.getId()));
            }
            return algorithmRelease;
        }
        kubernetesClient.batch()
            .v1()
            .jobs()
            .withName(algorithmRelease.getJobName())
            .delete();
        return algorithmReleaseRepository.save(algorithmRelease.toBuilder()
            .status(ActionStatusEnum.CANCELED)
            .gmtModify(LocalDateTime.now())
            .build());
    }

    public AlgorithmRelease callback(Long algorithmReleaseId, String callbackToken,
        boolean succeed) {
        Optional<AlgorithmRelease> algorithmReleaseOptional
            = algorithmReleaseRepository.findById(algorithmReleaseId);
        if (algorithmReleaseOptional.isEmpty()) {
            throw new IllegalArgumentException(
                String.format("algorithmRelease not found by id(%s)", algorithmReleaseId));
        }
        String token = tokenCache.getUnchecked(algorithmReleaseId);
        if (!StringUtils.equals(callbackToken, token)) {
            throw new IllegalArgumentException(String.format(
                "algorithmRelease(id=%s) token(%s) not matches", algorithmReleaseId, token));
        }
        AlgorithmRelease toUpdatedAlgorithmRelease = algorithmReleaseOptional.get();
        if (succeed) {
            toUpdatedAlgorithmRelease = updateDockerImage(toUpdatedAlgorithmRelease);
        } else {
            LOGGER.info("[AlgorithmReleaseService-callback] failed, start to delete algorithm={}", toUpdatedAlgorithmRelease.getAlgorithm().getId());
            businessAlgorithmService.deleteByAlgorithmId(
                toUpdatedAlgorithmRelease.getAlgorithm().getId());
        }
        return algorithmReleaseRepository.save(toUpdatedAlgorithmRelease
            .toBuilder()
            .status(succeed ? ActionStatusEnum.SUCCEED : ActionStatusEnum.FAILED)
            .gmtModify(LocalDateTime.now())
            .build());
    }

    private AlgorithmRelease updateDockerImage(AlgorithmRelease algorithmRelease) {
        Optional<BusinessAlgorithm> businessAlgorithmOptional = businessAlgorithmRepository.findByAlgorithmId(
            algorithmRelease.getAlgorithm().getId());
        DockerRegistryImage image;
        if (algorithmRelease.getImage() == null) {
            image = businessDockerRegistryImageRepository.save(
                BusinessDockerRegistryImage.builder()
                    .universalUserId(businessAlgorithmOptional.get().getUniversalUserId())
                    .gmtCreate(LocalDateTime.now())
                    .gmtModify(LocalDateTime.now())
                    .dockerRegistryImage(DockerRegistryImage.builder()
                        .registry(algorithmRelease.getAlgorithm().getRegistry())
                        .repository(businessAlgorithmOptional.get().getName())
                        .tag(businessAlgorithmOptional.get().getVersion())
                        .build())
                    .build()).getDockerRegistryImage();
        } else {
            BusinessDockerRegistryImage businessDockerRegistryImage = businessDockerRegistryImageRepository.findByDockerRegistryImageId(
                algorithmRelease.getImage().getId());
            image = businessDockerRegistryImageRepository.save(
                businessDockerRegistryImage.toBuilder()
                    .universalUserId(businessAlgorithmOptional.get().getUniversalUserId())
                    .gmtModify(LocalDateTime.now())
                    .dockerRegistryImage(
                        businessDockerRegistryImage.getDockerRegistryImage().toBuilder()
                            .registry(algorithmRelease.getAlgorithm().getRegistry())
                            .repository(businessAlgorithmOptional.get().getName())
                            .tag(businessAlgorithmOptional.get().getVersion())
                            .build())
                    .build()).getDockerRegistryImage();
        }
        return algorithmRelease.toBuilder().image(image).build();
    }

    private Job templateJob() {
        Job job = kubernetesClient.batch()
            .v1()
            .jobs()
            .load(new ByteArrayInputStream(
                algorithmReleaseJobTemplate.getBytes(StandardCharsets.UTF_8)))
            .get();
        job.getMetadata().setNamespace(namespace);
        return job;
    }

    private void configureFetchResourceContainer(AlgorithmRelease algorithmRelease,
        Container fetchResourceContainer, Long userId) {
        for (EnvVar envVar : fetchResourceContainer.getEnv()) {
            switch (envVar.getName()) {
                case "MINIO_ENDPOINT":
                    envVar.setValue(minioConnection.getEndpoint());
                    break;
                // TODO rename env names
                case "MINIO_ACCESSKEY":
                    envVar.setValue(minioConnection.getAccessKey());
                    break;
                // TODO rename env names
                case "MINIO_ACCESSSECRET":
                    envVar.setValue(minioConnection.getAccessSecret());
                    break;
                case "ALGORITHM_BUCKET":
                    envVar.setValue(minioBucket);
                    break;
                case "ALGORITHM_OBJECT_KEY":
                    envVar.setValue(
                        pathTransformer.userIndependentPath(userId, String.format("/%s/%s",
                            algorithmRelease.getAlgorithm().getDirPath(),
                            ALGO_FILE_TAR)));
//                    envVar.setValue(String.format("/%s/%s/%s", userId,
//                        algorithmRelease.getAlgorithm().getDirPath(),
//                        ALGO_FILE_TAR));
                    break;
                default:
                    // do nothing
            }
        }
    }

    private void configureBuildContainer(AlgorithmRelease algorithmRelease, String token,
        String repository, String tag, Container buildContainer) {
        Algorithm algorithm = algorithmRelease.getAlgorithm();
        DockerRegistryImage baseImage = algorithm.getBaseImage();
        for (EnvVar envVar : buildContainer.getEnv()) {
            // TODO rename env names
            switch (envVar.getName()) {
                case "DOCKER_REGISTRY":
                    envVar.setValue(algorithm.getRegistry());
                    break;
                case "BASE_PYTHON_REGISTRY":
                    envVar.setValue(baseImage.getRegistry());
                    break;
                case "BASE_PYTHON_IMAGE":
                    envVar.setValue(baseImage.getRepository());
                    break;
                case "BASE_PYTHON_TAG":
                    envVar.setValue(baseImage.getTag());
                    break;
                case "ALGORITHM_NAME":
                    envVar.setValue(repository);
                    break;
                case "ALGORITHM_VERSION":
                    envVar.setValue(tag);
                    break;
                case "CALLBACK_ADDRESS":
                    envVar.setValue(
                        String.format("%s/algorithm/release/%s/callback", callbackServer,
                            algorithmRelease.getId()));
                    break;
                case "CALLBACK_TOKEN":
                    envVar.setValue(token);
                    break;
                default:
                    // do nothing
            }
        }
    }

    private void configVolumes(PodSpec podSpec, String configName) {
        ConfigMapVolumeSource configMapVolumeSource = podSpec.getVolumes().stream()
            .filter(volume -> StringUtils.equals("nebula-algorithm-init", volume.getName()))
            .findFirst()
            .orElseThrow(() -> new BugException("'nebula-algorithm-init' volume not found"))
            .getConfigMap();
        configMapVolumeSource.setName(configName);
    }
}
