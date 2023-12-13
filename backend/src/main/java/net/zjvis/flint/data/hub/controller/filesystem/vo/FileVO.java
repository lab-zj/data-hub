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
@Schema(description = "directory or file")
public class FileVO {
    @Schema(description = "full path of directory/file", example = "/path/to/directory/file.txt")
    private final String path;
    @Schema(description = "is directory or not. true for directory.", example = "false")
    private final boolean directory;
    @Schema(description = "duplicated for frontend display", example = "file.txt")
    private final String name;
    @Schema(description = "duplicated for frontend display", example = "/path/to/directory")
    private final String parentDirectory;

    @Builder
    @Jacksonized
    public FileVO(
            String path,
            String name,
            boolean directory,
            String parentDirectory
    ) {
        this.path = path;
        this.name = name;
        this.directory = directory;
        this.parentDirectory = parentDirectory;
    }
}
