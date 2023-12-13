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
public class FileMetaVO {
    public enum FileType {
        NOT_RECOGNITION,
        DIRECTORY,
        CSV,
        TXT,
        VIDEO,
        IMAGE,
        MYSQL,
        S3,
        POSTGRESQL,
        ALGORITHM
    }

    @Schema(description = "full path of directory/file", example = "/path/to/directory/file.txt")
    private final String path;
    @Schema(description = "is directory or not. true for directory.")
    private final boolean directory;
    @Schema(description = "duplicated for frontend display", example = "file.txt")
    private final String name;
    @Schema(description = "duplicated for frontend display", example = "/path/to/directory")
    private final String parentDirectory;
    @Schema(description = "size for frontend display, Unit of measurement kb", example = "123")
    private final Long size;
    @Schema(description = "creat time", example = "1683684647461")
    private final Long creatTimestamp;
    @Schema(description = "change the time", example = "1683684647461")
    private final Long updateTimestamp;
    @Schema(description = "file type", example = "CSV")
    private final FileType fileType;

    @Builder(toBuilder = true)
    @Jacksonized
    public FileMetaVO(
            String path,
            String name,
            Long size,
            Long creatTimestamp,
            Long updateTimestamp,
            FileType fileType,
            boolean directory,
            String parentDirectory
    ) {
        this.path = path;
        this.name = name;
        this.size = size;
        this.creatTimestamp = creatTimestamp;
        this.updateTimestamp = updateTimestamp;
        this.fileType = fileType;
        this.directory = directory;
        this.parentDirectory = parentDirectory;
    }
}
