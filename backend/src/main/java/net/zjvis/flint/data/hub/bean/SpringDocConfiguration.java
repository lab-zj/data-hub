package net.zjvis.flint.data.hub.bean;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfiguration {
    public static final String CONTROLLER_PACKAGE = "net.zjvis.flint.data.hub.controller";

    @Bean
    public GroupedOpenApi accountApi() {
        return GroupedOpenApi.builder()
                .group("account")
                .packagesToScan(String.format("%s.account", CONTROLLER_PACKAGE))
                .build();
    }

    @Bean
    public GroupedOpenApi fileSystemApi() {
        return GroupedOpenApi.builder()
                .group("filesystem")
                .packagesToScan(String.format("%s.filesystem", CONTROLLER_PACKAGE))
                .build();
    }

    @Bean
    public GroupedOpenApi algorithmApi() {
        return GroupedOpenApi.builder()
                .group("algorithm")
                .packagesToScan(String.format("%s.algorithm", CONTROLLER_PACKAGE),
                        String.format("%s.artifact", CONTROLLER_PACKAGE))
                .build();
    }

    @Bean
    public GroupedOpenApi graphApi() {
        return GroupedOpenApi.builder()
                .group("graph")
                .packagesToScan(String.format("%s.graph", CONTROLLER_PACKAGE))
                .build();
    }

    @Bean
    public GroupedOpenApi docApi() {
        return GroupedOpenApi.builder()
                .group("doc")
                .packagesToScan(String.format("%s.doc", CONTROLLER_PACKAGE))
                .build();
    }
}
