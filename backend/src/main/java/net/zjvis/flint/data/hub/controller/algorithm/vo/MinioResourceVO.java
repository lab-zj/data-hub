package net.zjvis.flint.data.hub.controller.algorithm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@Setter
@Jacksonized
@EqualsAndHashCode
@ToString
@Schema
public class MinioResourceVO {
    @Schema(description = "Generated by database to identify unique minio resource")
    private Long id;
    @Schema(description = "File object key")
    private String objectKey;
}