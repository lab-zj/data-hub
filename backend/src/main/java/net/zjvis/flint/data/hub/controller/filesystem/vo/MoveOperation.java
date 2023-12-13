package net.zjvis.flint.data.hub.controller.filesystem.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@Getter
@EqualsAndHashCode
@ToString
public class MoveOperation {
    @Schema(example = "some/path/foo.csv")
    private final String sourcePath;
    @Schema(description = "target path to move to", example = "some/path/bar/foo.csv")
    private final String targetPath;
    @Schema(example = "true", defaultValue = "false")
    private final Boolean override;

    @Builder
    @Jacksonized
    public MoveOperation(String sourcePath, String targetPath, Boolean override) {
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
        this.override = override;
    }
}
