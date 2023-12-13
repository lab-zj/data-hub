package net.zjvis.flint.data.hub.controller.filesystem.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
public class BatchMoveOperationVO {
    @Schema(example = "[\"some/path/foo.csv\", \"some/path/bar.csv\"]")
    private final List<String> sourcePathList;
    @Schema(description = "target path to move to, which should be an existing directory", example = "some/path/bar/")
    private final String targetPath;
    @Schema(example = "true", defaultValue = "false")
    private final Boolean override;

    @Builder
    @Jacksonized
    public BatchMoveOperationVO(List<String> sourcePathList, String targetPath, Boolean override) {
        this.sourcePathList = sourcePathList;
        this.targetPath = targetPath;
        this.override = null != override && override;
    }
}
