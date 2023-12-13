package net.zjvis.flint.data.hub.bean;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import net.zjvis.flint.data.hub.controller.filesystem.PathTransformer;
import net.zjvis.flint.data.hub.lib.minio.MinioConnection;
import net.zjvis.flint.data.hub.lib.minio.MinioManager;
import net.zjvis.flint.data.hub.lib.talk.DingTalkAppConfiguration;
import net.zjvis.flint.data.hub.lib.talk.DingTalkUserManager;
import okhttp3.OkHttpClient;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BeansFactory implements ApplicationContextAware {
    protected static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        if (BeansFactory.applicationContext == null) {
            BeansFactory.applicationContext = applicationContext;
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public MinioConnection minioConnection(
            @Value("${application.s3.minio.endpoint}") String endpoint,
            @Value("${application.s3.minio.accessKey}") String accessKey,
            @Value("${application.s3.minio.accessSecret}") String accessSecret
    ) {
        return MinioConnection.builder()
                .endpoint(endpoint)
                .accessKey(accessKey)
                .accessSecret(accessSecret)
                .build();
    }

    @Bean
    public MinioManager minioManager(MinioConnection minioConnection) {
        return MinioManager.builder()
                .minioConnection(minioConnection)
                .build();
    }

    @Bean
    public DingTalkAppConfiguration dingTalkAppConfiguration(
            @Value("${application.dingtalk.app.protocol}") String protocol,
            @Value("${application.dingtalk.app.regionId}") String regionId,
            @Value("${application.dingtalk.app.key}") String appKey,
            @Value("${application.dingtalk.app.secret}") String appSecret
    ) {
        return DingTalkAppConfiguration.builder()
                .protocol(protocol)
                .regionId(regionId)
                .appKey(appKey)
                .appSecret(appSecret)
                .build();
    }

    @Bean
    public DingTalkUserManager dingTalkUserManager(
            DingTalkAppConfiguration dingTalkAppConfiguration
    ) {
        return DingTalkUserManager.builder()
                .dingTalkAppConfiguration(dingTalkAppConfiguration)
                .build();
    }

    @Bean
    public AlgorithmConfiguration algorithmConfiguration(
            @Value("${application.algorithm.namespace}") String namespace,
            @Value("${application.algorithm.registry}") String registry,
            @Value("${application.algorithm.service-account}") String serviceAccountName,
            @Value("${application.algorithm.role-bind}") String roleBindingName,
            @Value("${application.algorithm.minio.bucket}") String minioBucket,
            @Value("${application.algorithm.server.inner.schema}") String innerServerSchema,
            @Value("${application.algorithm.server.inner.host}") String innerServerHost,
            @Value("${application.algorithm.server.inner.port}") int innerServerPort,
            @Value("${application.algorithm.configmap}") String configmapName
    ) throws IOException {
        return AlgorithmConfiguration.builder()
                .namespace(namespace)
                .configmapName(configmapName)
                .registry(registry)
                .serviceAccountName(serviceAccountName)
                .roleBindingName(roleBindingName)
                .minioBucket(minioBucket)
                .algorithmReleaseJobTemplate(resourceAsString("algorithm/algorithms.docker.job.yaml"))
                .algorithmDeployJobTemplate(resourceAsString("algorithm/algorithms.helm.job.yaml"))
                .algorithmServiceAccountTemplate(resourceAsString("algorithm/algorithms.service.account.yaml"))
                .algorithmRoleBindingTemplate(resourceAsString("algorithm/algorithms.role.binding.yaml"))
                .innerServerSchema(innerServerSchema)
                .innerServerHost(innerServerHost)
                .innerServerPort(innerServerPort)
                .build();
    }

    @Bean
    public KubernetesClient kubernetesClient() {
        return new KubernetesClientBuilder()
                .build();
    }

    @Bean
    public PathTransformer pathTransformer(@Value("${application.user.home:user/home}") String userHome) {
        return PathTransformer.builder()
                .userHome(userHome)
                .build();
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder().build();
    }

    private String resourceAsString(String resourcePath) throws IOException {
        try (InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream(resourcePath)) {
            return IOUtils.toString(
                    Objects.requireNonNull(inputStream, String.format("resource(%s) not found", resourcePath)),
                    StandardCharsets.UTF_8);
        }
    }
}
