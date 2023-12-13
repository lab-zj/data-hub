package net.zjvis.flint.data.hub.controller.algorithm.request;

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
public class BusinessAlgorithmSearchRequest {
    @Schema(description = "Algorithm name key word")
    private String nameKeyWord;
    @Schema(description = "Algorithm version", example = "v1.0 (default value)")
    private String version = "v1.0";
    @Schema(description = "Algorithm type", example = "custom , built-in")
    private String type;
    @Schema(description = "Page number")
    private int pageNumber = 1;
    @Schema(description = "Page size")
    private int pageSize = 100;
}
