package net.zjvis.flint.data.hub.controller.artifact.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Schema
public class BusinessDockerRegistryImageVO {
    @Schema(description = "Generated by database to identify unique business docker registry image")
    private Long id;
    @Schema(description = "Generated by database to identify unique docker registry image")
    private Long baseImageId;
    @Schema(description = "Image registry", example = "docker-registry-ops-dev.lab.zjvis.net:32443/docker.io")
    private String registry;
    @Schema(description = "Image repository", example = "continuumio/miniconda3")
    private String repository;
    @Schema(description = "Image tag", example = "4.9.2-20230110-r6")
    private String tag;
    @Schema(description = "User id")
    private Long universalUserId;
    @Schema(description = "The flag of whether shared for others")
    private boolean shared;
    @Schema(description = "Timestamp of create")
    private Long gmtCreate;
    @Schema(description = "Timestamp of modify")
    private Long gmtModify;
}
