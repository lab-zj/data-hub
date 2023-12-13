package net.zjvis.flint.data.hub.bean;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@Getter
@EqualsAndHashCode
@ToString
public class AlgorithmConfiguration {
    private final String namespace;
    private final String configmapName;
    private final String registry;
    private final String serviceAccountName;
    private final String roleBindingName;
    private final String minioBucket;
    private final String algorithmReleaseJobTemplate;
    private final String algorithmDeployJobTemplate;
    private final String algorithmServiceAccountTemplate;
    private final String algorithmRoleBindingTemplate;
    private final String innerServerSchema;
    private final String innerServerHost;
    private final int innerServerPort;

    @Builder
    @Jacksonized
    public AlgorithmConfiguration(
            String namespace,
            String configmapName,
            String registry,
            String serviceAccountName,
            String roleBindingName,
            String minioBucket,
            String algorithmReleaseJobTemplate,
            String algorithmDeployJobTemplate,
            String algorithmServiceAccountTemplate,
            String algorithmRoleBindingTemplate,
            String innerServerSchema,
            String innerServerHost,
            int innerServerPort
    ) {
        this.namespace = namespace;
        this.configmapName = configmapName;
        this.registry = registry;
        this.serviceAccountName = serviceAccountName;
        this.roleBindingName = roleBindingName;
        this.minioBucket = minioBucket;
        this.algorithmReleaseJobTemplate = algorithmReleaseJobTemplate;
        this.algorithmDeployJobTemplate = algorithmDeployJobTemplate;
        this.algorithmServiceAccountTemplate = algorithmServiceAccountTemplate;
        this.algorithmRoleBindingTemplate = algorithmRoleBindingTemplate;
        this.innerServerSchema = innerServerSchema;
        this.innerServerHost = innerServerHost;
        this.innerServerPort = innerServerPort;
    }

    public String callbackServer() {
        return String.format("%s://%s:%s",
                innerServerSchema,
                innerServerHost,
                innerServerPort
        );
    }
}
