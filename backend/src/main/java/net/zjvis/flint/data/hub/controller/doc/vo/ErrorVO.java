package net.zjvis.flint.data.hub.controller.doc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder(toBuilder = true)
@Jacksonized
@ToString
@EqualsAndHashCode
@Schema
public class ErrorVO {
    @Schema(description = "error")
    private Long code;
    @Schema(description = "error message")
    private String message;
}
