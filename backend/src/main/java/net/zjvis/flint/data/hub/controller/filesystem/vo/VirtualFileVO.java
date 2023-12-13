package net.zjvis.flint.data.hub.controller.filesystem.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.entity.filesystem.database.VirtualFile;

@Getter
@EqualsAndHashCode
@ToString
@Schema(description = "directory or file")
public class VirtualFileVO {
    @Schema(description = "full path of directory/file", example = "/path/to/directory/file.txt")
    private final String path;
    @Schema(description = "virtual file object")
    private final VirtualFile virtualFile;

    @Builder
    @Jacksonized
    public VirtualFileVO(String path, VirtualFile virtualFile) {
        this.path = path;
        this.virtualFile = virtualFile;
    }
}
