package net.zjvis.flint.data.hub.component;

import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.rbac.RoleBinding;
import io.fabric8.kubernetes.api.model.rbac.SubjectBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import net.zjvis.flint.data.hub.bean.AlgorithmConfiguration;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class Initializer {
    private final KubernetesClient kubernetesClient;
    private final AlgorithmConfiguration algorithmConfiguration;
    private final boolean skipInitialize;
    private final static Logger LOGGER = LoggerFactory.getLogger(Initializer.class);

    public Initializer(
            KubernetesClient kubernetesClient,
            AlgorithmConfiguration algorithmConfiguration,
            @Value("${application.initialize.skip:false}") boolean skipInitialize
    ) {
        this.kubernetesClient = kubernetesClient;
        this.algorithmConfiguration = algorithmConfiguration;
        this.skipInitialize = skipInitialize;
    }

    @PostConstruct
    public void postConstruct() {
        if (skipInitialize) {
            return;
        }
        initializeAlgorithmNamespace();
        initializeAlgorithmConfigmap();
        initializeServiceAccountAndRoleBinding();
    }

    private void initializeAlgorithmConfigmap() {
        String namespace = algorithmConfiguration.getNamespace();
        String configmapName = algorithmConfiguration.getConfigmapName();
        if (null == kubernetesClient.configMaps()
                .inNamespace(namespace)
                .withName(configmapName)
                .get()) {
            Map<String, String> data = Stream.of(
                    "build-init.sh",
                    "builder.sh",
                    "deploy-init.sh",
                    "deployer.sh",
                    "Dockerfile",
                    "entry-point.sh"
            ).collect(Collectors.toMap(
                    filename -> filename,
                    filename -> {
                        try {
                            return IOUtils.toString(
                                    Objects.requireNonNull(getClass()
                                            .getClassLoader()
                                            .getResourceAsStream(String.format("algorithm/configmap/%s", filename))),
                                    StandardCharsets.UTF_8);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }
            ));
            LOGGER.info("creating configmap({})...", configmapName);
            kubernetesClient.configMaps()
                    .resource(new ConfigMapBuilder()
                            .withNewMetadata()
                            .withNamespace(namespace)
                            .withName(configmapName)
                            .endMetadata()
                            .addToData(data)
                            .build())
                    .createOrReplace();
        }
    }

    private void initializeAlgorithmNamespace() {
        String namespace = algorithmConfiguration.getNamespace();
        if (null == kubernetesClient.namespaces()
                .withName(namespace).get()) {
            LOGGER.info("creating namespace({})...", namespace);
            kubernetesClient.namespaces()
                    .resource(new NamespaceBuilder()
                            .withNewMetadata()
                            .withName(namespace)
                            .endMetadata()
                            .build())
                    .createOrReplace();
        }
    }

    private void initializeServiceAccountAndRoleBinding() {
        initializeServiceAccount();
        initializeRoleBinding();
    }

    private void initializeServiceAccount() {
        String namespace = algorithmConfiguration.getNamespace();
        String serviceAccountName = algorithmConfiguration.getServiceAccountName();
        if (null == kubernetesClient.serviceAccounts()
                .inNamespace(namespace)
                .withName(serviceAccountName)
                .get()
        ) {
            ServiceAccount sa = kubernetesClient.serviceAccounts()
                    .load(new ByteArrayInputStream(algorithmConfiguration.getAlgorithmServiceAccountTemplate()
                            .getBytes(StandardCharsets.UTF_8)))
                    .get();
            sa.getMetadata().setName(serviceAccountName);
            sa.getMetadata().setNamespace(namespace);
            kubernetesClient.serviceAccounts()
                    .resource(sa)
                    .createOrReplace();
        }
    }

    private void initializeRoleBinding() {
        String namespace = algorithmConfiguration.getNamespace();
        String serviceAccountName = algorithmConfiguration.getServiceAccountName();
        String roleBindingName = algorithmConfiguration.getRoleBindingName();
        if (null == kubernetesClient.rbac()
                .roleBindings()
                .inNamespace(namespace)
                .withName(roleBindingName)
                .get()
        ) {
            RoleBinding roleBinding = kubernetesClient.rbac().roleBindings()
                    .load(new ByteArrayInputStream(algorithmConfiguration.getAlgorithmRoleBindingTemplate()
                            .getBytes(StandardCharsets.UTF_8)))
                    .get();
            roleBinding.getMetadata().setName(roleBindingName);
            roleBinding.getMetadata().setNamespace(namespace);
            roleBinding.setSubjects(Collections.singletonList(new SubjectBuilder()
                    .withKind("ServiceAccount")
                    .withName(serviceAccountName)
                    .withNamespace(namespace)
                    .build()));
            kubernetesClient.rbac().roleBindings()
                    .resource(roleBinding)
                    .createOrReplace();
        }
    }
}
