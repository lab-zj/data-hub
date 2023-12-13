package net.zjvis.flint.data.hub.controller.filesystem;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.Getter;
import net.zjvis.flint.data.hub.StandardResponse;
import net.zjvis.flint.data.hub.controller.BasicController;
import net.zjvis.flint.data.hub.controller.filesystem.vo.FileMetaVO;
import net.zjvis.flint.data.hub.controller.filesystem.vo.FileVO;
import net.zjvis.flint.data.hub.controller.filesystem.vo.VirtualFileVO;
import net.zjvis.flint.data.hub.entity.account.UniversalUser;
import net.zjvis.flint.data.hub.entity.filesystem.database.S3VirtualFile;
import net.zjvis.flint.data.hub.entity.filesystem.database.VirtualFile;
import net.zjvis.flint.data.hub.lib.minio.MinioException;
import net.zjvis.flint.data.hub.lib.minio.MinioTagKey;
import net.zjvis.flint.data.hub.service.FilesystemService;
import net.zjvis.flint.data.hub.service.VirtualFileService;
import net.zjvis.flint.data.hub.service.account.AccountUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@RestController
@RequestMapping("/filesystem/virtual/file")
public class VirtualFileController implements BasicController {
    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualFileController.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final PathTransformer pathTransformer;
    private final AccountUtils accountUtils;
    private final FilesystemService filesystemService;
    private final VirtualFileService virtualFileService;

    public VirtualFileController(
            PathTransformer pathTransformer,
            AccountUtils accountUtils,
            FilesystemService filesystemService,
            VirtualFileService virtualFileService
    ) {
        this.pathTransformer = pathTransformer;
        this.accountUtils = accountUtils;
        this.filesystemService = filesystemService;
        this.virtualFileService = virtualFileService;
    }

    @Override
    public long id() {
        return 6L;
    }

    public enum ERROR implements BasicController.Error {
        NOT_LOGIN_USER(HttpStatus.UNAUTHORIZED, 1L, "login first"),
        FILE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, 2L, "file(%s) already exists"),
        READ_VIRTUAL_FILE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,
                3L, "read virtual file(%s) failed"),
        CREATE_VIRTUAL_FILE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,
                4L, "create virtual file(%s) failed"),
        PARSE_VIRTUAL_FILE_JSON_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,
                5L, "parse virtual file json(%s) failed"),
        CONNECTION_TEST_FAILED(HttpStatus.BAD_REQUEST, 6L, "connection test failed: %s"),
        ;

        @Getter
        private final HttpStatus httpStatus;
        @Getter
        private final long code;
        @Getter
        private final String messageTemplate;

        ERROR(HttpStatus httpStatus, long code, String messageTemplate) {
            this.httpStatus = httpStatus;
            this.code = code;
            this.messageTemplate = messageTemplate;
            Preconditions.checkArgument(code >= 1 && code < 1000, "code must be in [1, 1000)");
        }
    }

    @Operation(summary = "virtual file")
    @PostMapping("/")
    public StandardResponse<Void> create(@RequestBody VirtualFileVO virtualFileVO) throws IOException {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return errorResponse(ERROR.NOT_LOGIN_USER);
        }
        Long userId = currentUserOptional.get().getId();
        String path = virtualFileVO.getPath();
        String userIndependentPath = pathTransformer.userIndependentPath(userId, path);
        try {
            if (filesystemService.objectExists(userIndependentPath)) {
                return errorResponse(ERROR.FILE_ALREADY_EXISTS, path);
            }
            filesystemService.objectUpload(
                    new ByteArrayInputStream(OBJECT_MAPPER.writeValueAsBytes(virtualFileVO.getVirtualFile())),
                    userIndependentPath);
            return success(null);
        } catch (MinioException e) {
            if (filesystemService.objectExists(userIndependentPath)) {
                filesystemService.batchRemove(Collections.singletonList(userIndependentPath), false);
            }
            LOGGER.warn(
                    String.format("create virtual file(%s) failed: %s", userIndependentPath, e.getMessage()),
                    e);
            return errorResponse(ERROR.CREATE_VIRTUAL_FILE_FAILED, path);
        }
    }

    @Operation(
            summary = "find virtual file by path",
            responses = {
                    @ApiResponse(description = "standard response containing virtual file")
            }
    )
    @GetMapping("/")
    public StandardResponse<Map<String, Object>> find(@RequestParam(defaultValue = "/") String path) {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return errorResponse(ERROR.NOT_LOGIN_USER);
        }
        Long userId = currentUserOptional.get().getId();
        String userIndependentPath = pathTransformer.userIndependentPath(userId, path);
        String virtualFileJson;
        try {
            virtualFileJson = filesystemService.previewText(userIndependentPath, 2048);
        } catch (IOException e) {
            LOGGER.warn(String.format("read virtual file(%s) failed: %s", userIndependentPath, e.getMessage()),
                    e);
            return errorResponse(ERROR.READ_VIRTUAL_FILE_FAILED, path);
        }
        VirtualFile virtualFile;
        try {
            virtualFile = OBJECT_MAPPER.readValue(virtualFileJson, new TypeReference<>() {
            });
            // serialization from springboot cannot recognize the type of virtualFile, we need to re-serialize as Map
            return success(OBJECT_MAPPER.readValue(
                    OBJECT_MAPPER.writeValueAsString(virtualFile),
                    new TypeReference<>() {
                    }));
        } catch (JsonProcessingException e) {
            LOGGER.warn(String.format("parse virtual file(%s) json from json(%s) failed: %s",
                            userIndependentPath, virtualFileJson, e.getMessage()),
                    e);
            return errorResponse(ERROR.PARSE_VIRTUAL_FILE_JSON_FAILED, path);
        }
    }

    @Operation(
            summary = "preview virtual file content by path",
            responses = {
                    @ApiResponse(description = "standard response containing virtual file content")
            }
    )
    @GetMapping("/preview")
    public StandardResponse<Object> preview(
            @Schema(description = "path of file", example = "some_minio.s3:aaa.csv")
            @RequestParam(defaultValue = "/") String path,
            @RequestHeader HttpHeaders headers) {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return errorResponse(ERROR.NOT_LOGIN_USER);
        }
        Long userId = currentUserOptional.get().getId();
        String userIndependentPath = pathTransformer.userIndependentPath(userId, path.split(":")[0]);
        String virtualFileJson;
        try {
            virtualFileJson = filesystemService.previewText(userIndependentPath, 2048);
        } catch (IOException e) {
            LOGGER.warn(String.format("read virtual file(%s) failed: %s", userIndependentPath, e.getMessage()),
                    e);
            return errorResponse(ERROR.READ_VIRTUAL_FILE_FAILED, path);
        }
        VirtualFile virtualFile;
        try {
            virtualFile = OBJECT_MAPPER.readValue(virtualFileJson, new TypeReference<>() {
            });
            if (virtualFile instanceof S3VirtualFile) {
                return success(virtualFile.previewData());
            } else {
                return success(virtualFile.previewData("table"));
            }
        } catch (JsonProcessingException e) {
            LOGGER.warn(String.format("parse virtual file(%s) json from json(%s) failed: %s",
                            userIndependentPath, virtualFileJson, e.getMessage()),
                    e);
            return errorResponse(ERROR.PARSE_VIRTUAL_FILE_JSON_FAILED, path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Operation(summary = "connection test")
    @PostMapping("/connection/test")
    public StandardResponse<Boolean> connectionTest(
            @Schema(description = "virtualFile", example = "{}")
            @RequestBody VirtualFile virtualFile
    ) {
        Pair<Boolean, String> result = virtualFile.connectionTest();
        if (result.getLeft()) {
            return success(true);
        } else {
            return errorResponse(ERROR.CONNECTION_TEST_FAILED, result.getRight());
        }
    }

    @Operation(summary = "query meta for specified virtual file")
    @GetMapping("/meta")
    public StandardResponse<FileMetaVO> meta(
            @Schema(description = "virtual file path", example = "/result.s3")
            @RequestParam(defaultValue = "/") String path
    ) throws IOException {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return errorResponse(BasicFileOperationController.ERROR.NOT_LOGIN_USER);
        }
        Long userId = currentUserOptional.get().getId();
        String userIndependentPath = pathTransformer.userIndependentPath(userId, path);
        S3VirtualFile s3VirtualFile = OBJECT_MAPPER.readValue(
                IOUtils.toString(filesystemService.download(userIndependentPath), StandardCharsets.UTF_8),
                new TypeReference<>() {
                });
        return success(constructFileMetaVO(s3VirtualFile, constructFileVO(s3VirtualFile.getObjectKey())));
    }


    @Operation(summary = "download virtual file")
    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> batchDownload(
            @Schema(description = "an indicator for the type of file")
            @RequestHeader(defaultValue = "application/octet-stream") String contentType,
            @Schema(description = "virtual file path to download", example = "/foo/bar.s3")
            @RequestParam(defaultValue = "/") String path
    ) throws IOException {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .build();
        }
        Long userId = currentUserOptional.get().getId();
        final String userIndependentPath = pathTransformer.userIndependentPath(userId, path);
        S3VirtualFile s3VirtualFile = OBJECT_MAPPER.readValue(
                IOUtils.toString(filesystemService.download(userIndependentPath), StandardCharsets.UTF_8),
                new TypeReference<>() {
                });
        return constructStreamingResponseBodyResponseEntity(
                () -> {
                    try {
                        return virtualFileService.download(s3VirtualFile);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                },
                MediaType.valueOf(contentType),
                path.substring(path.lastIndexOf("/") + 1)
        );
    }

    private FileVO constructFileVO(String objectKey) {
        return FileVO.builder()
                .path(StringUtils.removeStart(objectKey, "/"))
                .directory(false)
                .name(FilenameUtils.getName(objectKey))
                .parentDirectory(FilenameUtils.getPath(objectKey))
                .build();
    }

    private FileMetaVO constructFileMetaVO(
            S3VirtualFile s3VirtualFile,
            FileVO fileVO
    ) throws MinioException {
        boolean isDirectory = fileVO.isDirectory();
        Optional<Map<String, String>> objectStatOptional
                = Optional.ofNullable(virtualFileService.getStatObject(s3VirtualFile));
        Long size = objectStatOptional
                .map(statObject -> Long.parseLong(statObject.get(MinioTagKey.SIZE.getKey())))
                .orElse(0L);
        Long createTimestamp = objectStatOptional
                .map(statObject -> Long.parseLong(statObject.get(MinioTagKey.CREATE_TIME.getKey())))
                .orElse(0L);
        Long updateTimestamp = objectStatOptional
                .map(statObject -> Long.parseLong(statObject.get(MinioTagKey.LAST_MODIFIED.getKey())))
                .orElse(0L);
        return FileMetaVO.builder()
                .path(fileVO.getPath())
                .directory(isDirectory)
                .name(fileVO.getName())
                .parentDirectory(fileVO.getParentDirectory())
                .size(size)
                .creatTimestamp(createTimestamp)
                .updateTimestamp(updateTimestamp)
                .fileType(analyseVirtualFileType(FilenameUtils.getExtension(s3VirtualFile.getObjectKey())))
                .build();
    }

    private static FileMetaVO.FileType analyseVirtualFileType(String fileExtension) {
        FileMetaVO.FileType fileType;
        switch (fileExtension) {
            case "csv":
                fileType = FileMetaVO.FileType.CSV;
                break;
            case "png":
                fileType = FileMetaVO.FileType.IMAGE;
                break;
            case "mp4":
                fileType = FileMetaVO.FileType.VIDEO;
                break;
            case "txt":
                fileType = FileMetaVO.FileType.TXT;
                break;
            case "my":
                fileType = FileMetaVO.FileType.MYSQL;
                break;
            case "ps":
                fileType = FileMetaVO.FileType.POSTGRESQL;
                break;
            case "s3":
                fileType = FileMetaVO.FileType.S3;
                break;
            default:
                fileType = FileMetaVO.FileType.NOT_RECOGNITION;
        }
        return fileType;
    }

    private ResponseEntity<StreamingResponseBody> constructStreamingResponseBodyResponseEntity(
            Supplier<InputStream> inputStreamSupplier,
            MediaType contentType,
            String filename
    ) {
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(URLEncoder.encode(filename, StandardCharsets.UTF_8))
                .build();
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(outputStream -> {
                    try (InputStream inputStream = inputStreamSupplier.get()) {
                        StreamUtils.copy(inputStream, outputStream);
                    }
                });
    }
}
