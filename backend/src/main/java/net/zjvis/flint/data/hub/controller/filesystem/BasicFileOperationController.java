package net.zjvis.flint.data.hub.controller.filesystem;

import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import net.zjvis.flint.data.hub.PagedData;
import net.zjvis.flint.data.hub.StandardResponse;
import net.zjvis.flint.data.hub.controller.BasicController;
import net.zjvis.flint.data.hub.controller.filesystem.vo.BatchMoveOperationVO;
import net.zjvis.flint.data.hub.controller.filesystem.vo.FileMetaVO;
import net.zjvis.flint.data.hub.controller.filesystem.vo.FileMetaVO.FileType;
import net.zjvis.flint.data.hub.controller.filesystem.vo.FileVO;
import net.zjvis.flint.data.hub.controller.filesystem.vo.MoveOperation;
import net.zjvis.flint.data.hub.entity.account.UniversalUser;
import net.zjvis.flint.data.hub.lib.minio.MinioException;
import net.zjvis.flint.data.hub.lib.minio.MinioFile;
import net.zjvis.flint.data.hub.lib.minio.MinioTagKey;
import net.zjvis.flint.data.hub.service.FilesystemService;
import net.zjvis.flint.data.hub.service.account.AccountUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/filesystem/basic")
public class BasicFileOperationController implements BasicController {
    public enum ERROR implements BasicController.Error {
        NOT_LOGIN_USER(HttpStatus.UNAUTHORIZED, 1L, "login first"),
        FILE_OR_DIRECTORY_NOT_EXISTS(HttpStatus.NOT_FOUND, 2L, "file/directory(%s) not exists"),
        UPLOAD_FILE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 3L, "upload file(%s) failed"),
        CREATE_DIRECTORY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 4L, "create directory(%s) failed"),
        DELETE_DIRECTORY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 5L, "delete directoryList(%s) failed"),
        PATH_LIST_CANNOT_BE_EMPTY(HttpStatus.INTERNAL_SERVER_ERROR, 6L, "pathList cannot be empty"),
        DIRECTORY_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, 7L, "directory(%s) already exists"),
        TARGET_PATH_IS_NOT_DIRECTORY(HttpStatus.BAD_REQUEST,
                8L, "target path(%s) is not a directory"),
        TARGET_PATH_NOT_EXISTS(HttpStatus.BAD_REQUEST, 9L, "target path(%s) not exists"),
        TARGET_PATH_IS_NOT_FILE(HttpStatus.BAD_REQUEST, 10L, "target path(%s) is not a file"),
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

    public static final Logger LOGGER = LoggerFactory.getLogger(BasicFileOperationController.class);
    private final PathTransformer pathTransformer;
    private final AccountUtils accountUtils;
    private final FilesystemService filesystemService;

    public BasicFileOperationController(
            PathTransformer pathTransformer,
            AccountUtils accountUtils,
            FilesystemService filesystemService
    ) {
        this.pathTransformer = pathTransformer;
        this.accountUtils = accountUtils;
        this.filesystemService = filesystemService;
    }

    @Override
    public long id() {
        return 4L;
    }

    @Operation(summary = "list directories and files in specific directory")
    @GetMapping("/directory/list")
    public StandardResponse<PagedData<FileVO>> directoryList(
            @Schema(description = "path to list", example = "/")
            @RequestParam String path,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return errorResponse(ERROR.NOT_LOGIN_USER);
        }
        Long userId = currentUserOptional.get().getId();
        Page<MinioFile> pagedMinioFiles = filesystemService.directoryList(
                pathTransformer.userIndependentPath(userId, path),
                PageRequest.of(page - 1, size)
        );
        return success(PagedData.<FileVO>builder()
                .totalElements(pagedMinioFiles.getTotalElements())
                .totalPages(pagedMinioFiles.getTotalPages())
                .content(pagedMinioFiles.getContent()
                        .stream()
                        .map(minioFile -> asFileVO(userId, minioFile))
                        .collect(Collectors.toList()))
                .build());
    }

    @Operation(summary = "list directories and files in specific directory(with meta)")
    @GetMapping("/directory/list/meta")
    public StandardResponse<PagedData<FileMetaVO>> directoryListMeta(
            @Schema(description = "path to list", example = "/")
            @RequestParam String path,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return errorResponse(ERROR.NOT_LOGIN_USER);
        }
        Long userId = currentUserOptional.get().getId();
        Page<MinioFile> pagedMinioFiles = filesystemService.directoryList(
                pathTransformer.userIndependentPath(userId, path),
                PageRequest.of(page - 1, size)
        );
        return success(PagedData.<FileMetaVO>builder()
                .totalElements(pagedMinioFiles.getTotalElements())
                .totalPages(pagedMinioFiles.getTotalPages())
                .content(pagedMinioFiles.getContent()
                        .stream()
                        .map(minioFile -> {
                            try {
                                return asFileMetaVO(userId, minioFile, false);
                            } catch (Exception e) {
                                LOGGER.info("[directoryListMeta] failed, error={}", e.toString());
                                return null;
                            }
                        })
                        .filter( fileMetaVO ->
                            !(fileMetaVO != null && fileMetaVO.isDirectory() &&
                                StringUtils.equals(fileMetaVO.getPath(), "temp_output_" + userId + "/")
                                )
                        )
                        .collect(Collectors.toList()))
                .build());
    }

    @Operation(summary = "list algorithm directories and files in specific directory")
    @GetMapping("/directory/list/meta/algorithm")
    public StandardResponse<PagedData<FileMetaVO>> algorithmDirectoryListMeta(
            @Schema(description = "path to list", example = "/")
            @RequestParam String path,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return errorResponse(ERROR.NOT_LOGIN_USER);
        }
        Long userId = currentUserOptional.get().getId();
        Page<MinioFile> pagedMinioFiles = filesystemService.directoryList(
                pathTransformer.userIndependentPath(userId, path),
                PageRequest.of(page - 1, size)
        );
        return success(PagedData.<FileMetaVO>builder()
                .totalElements(pagedMinioFiles.getTotalElements())
                .totalPages(pagedMinioFiles.getTotalPages())
                .content(pagedMinioFiles.getContent()
                        .stream()
                        .map(minioFile -> {
                            try {
                                return asFileMetaVO(userId, minioFile, true);
                            } catch (Exception e) {
                                LOGGER.error("[algorithmDirectoryListMeta] failed, error={}", e.toString());
                                return null;
                            }
                        })
                        .filter( fileMetaVO ->
                            !(fileMetaVO.isDirectory() &&
                                fileMetaVO.getPath().equals("temp_output_" + userId + "/"))
                        )
                        .collect(Collectors.toList()))
                .build());
    }

    @Operation(summary = "searching directories and files in specific directory")
    @GetMapping("/search")
    public StandardResponse<PagedData<FileVO>> search(
            @Schema(description = "keyword to search", example = "foo.txt")
            @RequestParam String keyword,
            @Schema(description = "path to list", example = "/")
            @RequestParam String path,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return errorResponse(ERROR.NOT_LOGIN_USER);
        }
        Long userId = currentUserOptional.get().getId();
        Page<MinioFile> pagedMinioFiles = filesystemService.searchDirectoryList(
                pathTransformer.userIndependentPath(userId, path),
                PageRequest.of(page - 1, size),
                keyword,
                pathTransformer.userHomePath(userId)
        );
        return success(PagedData.<FileVO>builder()
                .totalElements(pagedMinioFiles.getTotalElements())
                .totalPages(pagedMinioFiles.getTotalPages())
                .content(pagedMinioFiles.getContent()
                        .stream()
                        .map(minioFile -> asFileVO(userId, minioFile))
                        .collect(Collectors.toList()))
                .build());
    }

    @Operation(summary = "searching directories and files in specific directory")
    @GetMapping("/search/meta")
    public StandardResponse<PagedData<FileMetaVO>> searchListMeta(
            @Schema(description = "keyword to search", example = "foo.txt")
            @RequestParam String keyword,
            @Schema(description = "path to list", example = "/")
            @RequestParam String path,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return errorResponse(ERROR.NOT_LOGIN_USER);
        }
        Long userId = currentUserOptional.get().getId();
        Page<MinioFile> pagedMinioFiles = filesystemService.searchDirectoryList(
                pathTransformer.userIndependentPath(userId, path),
                PageRequest.of(page - 1, size),
                keyword,
                pathTransformer.userHomePath(userId)
        );
        return success(PagedData.<FileMetaVO>builder()
                .totalElements(pagedMinioFiles.getTotalElements())
                .totalPages(pagedMinioFiles.getTotalPages())
                .content(pagedMinioFiles.getContent()
                        .stream()
                        .map(minioFile -> {
                            try {
                                return asFileMetaVO(userId, minioFile, false);
                            } catch (Exception e) {
                                LOGGER.error("[searchListMeta] failed, error={}", e.toString());
                                return null;
                            }
                        })
                        .collect(Collectors.toList()))
                .build());
    }

    @Operation(summary = "query meta for specific file or directory")
    @GetMapping("/meta")
    public StandardResponse<FileMetaVO> meta(
            @Schema(description = "file or directories path", example = "/")
            @RequestParam String path
    ) throws Exception {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return errorResponse(ERROR.NOT_LOGIN_USER);
        }
        Long userId = currentUserOptional.get().getId();
        String userIndependentPath = pathTransformer.userIndependentPath(userId, path);
        boolean directoryExists = filesystemService.directoryExists(userIndependentPath);
        boolean objectExists = filesystemService.objectExists(userIndependentPath);
        if (!directoryExists && !objectExists) {
            errorResponse(ERROR.FILE_OR_DIRECTORY_NOT_EXISTS, path);
        }
        // if directoryExists then this path should represent a directory
        String userRelativePath = pathTransformer.userRelativePath(userId, userIndependentPath);
        FileVO fileVO = constructFileVO(userRelativePath, directoryExists);
        return success(constructFileMetaVO(userIndependentPath, fileVO, false));
    }

    @Operation(summary = "upload a file to specific path")
    @PostMapping(value = "/file/upload/with/target/path", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StandardResponse<Void> fileUploadWithTargetPath(
            @Schema(description = "target path to upload to", example = "some/path/foo.txt")
            @RequestParam String path,
            @Parameter(
                    description = "file to upload",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart MultipartFile file,
            @RequestParam(defaultValue = "true") boolean overwrite
    ) throws IOException {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return errorResponse(ERROR.NOT_LOGIN_USER);
        }
        Long userId = currentUserOptional.get().getId();
        String userIndependentPath = pathTransformer.userIndependentPath(userId, path);
        String uploadedPath = fileUpload(file.getInputStream(), userIndependentPath, overwrite);
        if (null == uploadedPath) {
            // use path instead of userIndependentPath to avoid confusion
            errorResponse(ERROR.UPLOAD_FILE_FAILED, path);
        }
        return success(null);
    }

    @Operation(summary = "upload a file, whose filename will be detected according to the MultipartFile from request")
    @PostMapping(value = "/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StandardResponse<Void> fileUpload(
            @Schema(description = "directory path to upload", example = "/")
            @RequestParam(defaultValue = "/") String currentPath,
            @RequestParam(defaultValue = "true") boolean overwrite,
            @Parameter(
                    description = "file to upload",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart MultipartFile file,
            @RequestParam(required = false) String rootDirectoryToReplace
    ) throws IOException {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return errorResponse(ERROR.NOT_LOGIN_USER);
        }
        Long userId = currentUserOptional.get().getId();
        String filenameFromRequest = Optional.ofNullable(file.getOriginalFilename()).orElse(file.getName());
        String filePathFromRequest = StringUtils.isBlank(rootDirectoryToReplace)
                ? filenameFromRequest
                : replaceRootDirectory(rootDirectoryToReplace, filenameFromRequest);
        String path = String.format("%s/%s", currentPath, filePathFromRequest);
        String userIndependentPath = pathTransformer.userIndependentPath(userId, path);
        String uploadedPath = fileUpload(file.getInputStream(), userIndependentPath, overwrite);
        if (null == uploadedPath) {
            // use path instead of userIndependentPath to avoid confusion
            errorResponse(ERROR.UPLOAD_FILE_FAILED, path);
        }
        return success(null);
    }

    @Operation(summary = "create directory")
    @PostMapping(value = "/directory/create", consumes = MediaType.TEXT_PLAIN_VALUE)
    public StandardResponse<Void> directoryCreate(
            @Schema(description = "path to create", example = "/test/foo/bar")
            @RequestBody String path
    ) {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return errorResponse(ERROR.NOT_LOGIN_USER);
        }
        Long userId = currentUserOptional.get().getId();
        String userIndependentPath = pathTransformer.userIndependentPath(userId, path);
        try {
            filesystemService.directoryCreate(userIndependentPath);
        } catch (MinioException e) {
            if (StringUtils.equals(FilesystemService.directoryAlreadyExists(userIndependentPath), e.getMessage())) {
                // use path instead of userIndependentPath to avoid confusion
                return errorResponse(ERROR.DIRECTORY_ALREADY_EXISTS, path);
            }
            LOGGER.warn(String.format("create directory(%s) failed", userIndependentPath), e);
            // use path instead of userIndependentPath to avoid confusion
            return errorResponse(ERROR.CREATE_DIRECTORY_FAILED, path);
        }
        return success(null);
    }

    @Operation(summary = "batch remove file/directory")
    @DeleteMapping("/delete/batch")
    public StandardResponse<Void> batchDelete(
            @Schema(example = "[\"path/to/directory/foo.txt\"]")
            @RequestBody List<String> pathList) {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return errorResponse(ERROR.NOT_LOGIN_USER);
        }
        Long userId = currentUserOptional.get().getId();
        try {
            filesystemService.batchRemove(
                    pathList.stream()
                            .map(path -> pathTransformer.userIndependentPath(userId, path))
                            .collect(Collectors.toList()),
                    true);
        } catch (MinioException e) {
            LOGGER.warn(String.format("delete directoryList(%s) failed", pathList), e);
            return errorResponse(ERROR.DELETE_DIRECTORY_FAILED, String.valueOf(pathList));
        }
        return success(null);
    }

    @Operation(summary = "move file or directory")
    @PostMapping("/move")
    public StandardResponse<Void> move(@RequestBody MoveOperation moveOperation) {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return errorResponse(ERROR.NOT_LOGIN_USER);
        }
        Long userId = currentUserOptional.get().getId();
        String sourcePath = moveOperation.getSourcePath();
        String targetPath = moveOperation.getTargetPath();
        String userIndependentSourcePath = pathTransformer.userIndependentPath(userId, sourcePath);
        String userIndependentTargetPath = pathTransformer.userIndependentPath(userId, targetPath);
        try {
            filesystemService.move(userIndependentSourcePath, userIndependentTargetPath, moveOperation.getOverride());
            return success(null);
        } catch (MinioException e) {
            if (StringUtils.equals(
                    FilesystemService.objectPathIsDirectory(userIndependentTargetPath), e.getMessage())) {
                return errorResponse(ERROR.TARGET_PATH_IS_NOT_FILE, targetPath);
            }
            if (StringUtils.equals(
                    FilesystemService.targetPathIsNotDirectory(userIndependentTargetPath), e.getMessage())) {
                return errorResponse(ERROR.TARGET_PATH_IS_NOT_DIRECTORY, targetPath);
            }
            LOGGER.warn(String.format("move operation(%s) failed: %s", moveOperation, e.getMessage()), e);
            return errorResponse(ERROR.DELETE_DIRECTORY_FAILED, String.valueOf(moveOperation));
        }
    }

    @Operation(summary = "batch move or rename file/directory")
    @PostMapping("/move/batch")
    public StandardResponse<Void> batchMove(@RequestBody BatchMoveOperationVO batchMoveOperationVO) {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return errorResponse(ERROR.NOT_LOGIN_USER);
        }
        Long userId = currentUserOptional.get().getId();
        String targetPath = pathTransformer.userIndependentPath(userId, batchMoveOperationVO.getTargetPath());
        try {
            filesystemService.batchMoveToDirectory(
                    batchMoveOperationVO.getSourcePathList()
                            .stream()
                            .map(path -> pathTransformer.userIndependentPath(userId, path))
                            .collect(Collectors.toList()),
                    targetPath,
                    batchMoveOperationVO.getOverride());
            return success(null);
        } catch (MinioException e) {
            if (StringUtils.equals(FilesystemService.targetPathIsNotDirectory(targetPath), e.getMessage())) {
                return errorResponse(ERROR.TARGET_PATH_IS_NOT_DIRECTORY, batchMoveOperationVO.getTargetPath());
            }
            if (StringUtils.equals(FilesystemService.targetDirectoryPathNotExists(targetPath), e.getMessage())) {
                return errorResponse(ERROR.TARGET_PATH_NOT_EXISTS, batchMoveOperationVO.getTargetPath());
            }
            LOGGER.warn(String.format("batch move operation(%s) failed: %s", batchMoveOperationVO, e.getMessage()), e);
            return errorResponse(ERROR.DELETE_DIRECTORY_FAILED, String.valueOf(batchMoveOperationVO));
        }
    }

    @Operation(summary = "download file/directory")
    @GetMapping("/download/batch/{filename}")
    public ResponseEntity<StreamingResponseBody> batchDownload(
            @Schema(description = "an indicator for the type of file")
            @RequestHeader(defaultValue = "application/octet-stream") String contentType,
            @PathVariable(required = false) String filename,
            @Schema(description = "path list to download", example = "/foo/bar.txt,/foo/baz")
            @RequestParam List<String> pathList
    ) {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .build();
        }
        Long userId = currentUserOptional.get().getId();
        boolean filenameSpecified = StringUtils.isNotBlank(filename);
        if (1 == pathList.size()) {
            final String path = pathTransformer.userIndependentPath(userId, pathList.get(0));
            return constructStreamingResponseBodyResponseEntity(
                    () -> {
                        try {
                            return filesystemService.download(path);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    },
                    MediaType.valueOf(contentType),
                    filenameSpecified ? filename : constructFilename(path)
            );
        }
        return constructStreamingResponseBodyResponseEntity(
                () -> {
                    try {
                        return filesystemService.batchDownload(
                                pathList.stream()
                                        .map(path -> pathTransformer.userIndependentPath(userId, path))
                                        .collect(Collectors.toList()));
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                },
                MediaType.APPLICATION_OCTET_STREAM,
                filenameSpecified ? filename : "见微数据.zip"
        );
    }

    @Operation(summary = "download file/directory")
    @GetMapping("/download/batch")
    public ResponseEntity<StreamingResponseBody> batchDownload(
            @Schema(description = "an indicator for the type of file")
            @RequestHeader(defaultValue = "application/octet-stream") String contentType,
            @Schema(description = "path list to download", example = "/foo/bar.txt,/foo/baz")
            @RequestParam(defaultValue = "/") List<String> pathList
    ) {
        return batchDownload(contentType, null, pathList);
    }

    @Operation(
            summary = "check file/directory exists",
            description = "the result of one path may be null if exception found when checking"
    )
    @GetMapping("/exists/batch")
    public StandardResponse<Map<String, Boolean>> batchExists(
            @Schema(description = "path list to check", example = "/foo/bar.txt,/foo/baz")
            @RequestParam List<String> pathList) {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return errorResponse(ERROR.NOT_LOGIN_USER);
        }
        Long userId = currentUserOptional.get().getId();
        if (pathList.isEmpty()) {
            return errorResponse(ERROR.PATH_LIST_CANNOT_BE_EMPTY);
        }
        Map<String, Boolean> result = pathList.stream()
                .map(path -> {
                    String userIndependentPath = pathTransformer.userIndependentPath(userId, path);
                    try {
                        return Pair.of(
                                path,
                                FilesystemService.isDirectory(userIndependentPath)
                                        ? filesystemService.directoryExists(userIndependentPath)
                                        : filesystemService.objectExists(userIndependentPath));
                    } catch (MinioException e) {
                        LOGGER.warn(
                                String.format(
                                        "check exists of(%s) failed: %s", userIndependentPath, e.getMessage()),
                                e);
                        return Pair.of(path, (Boolean) null);
                    }
                }).collect(Collectors.toMap(
                        Pair::getLeft,
                        Pair::getRight
                ));
        return success(result);
    }

    @Operation(summary = "suggest a name for directory, which is expected not existing")
    @GetMapping("/directory/name/suggest")
    public StandardResponse<String> renameDirectory(
            @Schema(description = "path of directory")
            @RequestParam(defaultValue = "/") String path
    ) throws IOException {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return errorResponse(ERROR.NOT_LOGIN_USER);
        }
        Long userId = currentUserOptional.get().getId();
        String userIndependentPath = pathTransformer.userIndependentPath(userId, StringUtils.removeEnd(path, "/"));
        while (filesystemService.directoryExists(userIndependentPath)) {
            userIndependentPath = String.format("%s_1", userIndependentPath);
        }
        return success(FilenameUtils.getName(userIndependentPath));
    }

    private String constructFilename(String path) {
        if (!path.endsWith("/")) {
            return path.substring(path.lastIndexOf("/") + 1);
        }
        final String pathWithoutSlash = StringUtils.removeEnd(path, "/");
        if (pathWithoutSlash.contains("/")) {
            return String.format("%s.zip", pathWithoutSlash.substring(pathWithoutSlash.lastIndexOf("/") + 1));
        }
        return String.format("%s.zip", pathWithoutSlash);
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

    private String fileUpload(InputStream inputStream, String userIndependentPath, boolean overwrite)
            throws MinioException {
        String path = renameIfExists(userIndependentPath, overwrite);
        try {
            filesystemService.objectUpload(inputStream, path);
            return path;
        } catch (Exception exception) {
            LOGGER.warn(String.format("upload file failed: %s", exception.getMessage()), exception);
            if (filesystemService.objectExists(path)) {
                filesystemService.batchRemove(Collections.singletonList(path), false);
            }
            return null;
        }
    }

    private String renameIfExists(String userIndependentPath, boolean overwrite) throws MinioException {
        String actualFilePath = userIndependentPath;
        while (!overwrite && filesystemService.objectExists(actualFilePath)) {
            String parentPath = FilenameUtils.getPathNoEndSeparator(actualFilePath);
            String baseName = FilenameUtils.getBaseName(actualFilePath);
            String extension = FilenameUtils.getExtension(actualFilePath);
            actualFilePath = String.format("%s/%s_1.%s", parentPath, baseName, extension);
        }
        return actualFilePath;
    }

    private String replaceRootDirectory(String rootDirectoryToReplace, String filename) {
        return String.format(
                "%s/%s",
                StringUtils.removeEnd(rootDirectoryToReplace, "/"),
                filename.substring(filename.indexOf("/") + 1)
        );
    }

    private FileMetaVO asFileMetaVO(
            Long userId,
            MinioFile minioFile,
            boolean distinguishAlgorithmDirectory
    ) throws Exception {
        String objectKey = minioFile.getObjectKey();
        FileVO fileVO = asFileVO(userId, minioFile);
        return constructFileMetaVO(objectKey, fileVO, distinguishAlgorithmDirectory);
    }

    private FileMetaVO constructFileMetaVO(
            String objectKey,
            FileVO fileVO,
            boolean distinguishAlgorithmDirectory
    ) throws Exception {
        boolean isDirectory = fileVO.isDirectory();
        Optional<Map<String, String>> objectStatOptional
                = Optional.ofNullable(isDirectory ? null : filesystemService.getStatObject(objectKey));
        LOGGER.info("[constructFileMetaVO] start, objectKey={}, fileVo={}, objectStatOptional={}",
            objectKey, fileVO, objectStatOptional);
        Long size = 0L;
        Long createTimestamp = 0L;
        Long updateTimestamp = 0L;
        try {
            size = objectStatOptional
                .map(statObject -> Long.parseLong(
                    statObject.get(MinioTagKey.SIZE.getKey()) == null ? "0" : statObject.get(MinioTagKey.SIZE.getKey())))
                .orElse(0L);
            createTimestamp = objectStatOptional
                .map(statObject -> Long.parseLong(
                    statObject.get(MinioTagKey.CREATE_TIME.getKey()) == null ? "0" : statObject.get(MinioTagKey.CREATE_TIME.getKey())))
                .orElse(0L);
            updateTimestamp = objectStatOptional
                .map(statObject -> Long.parseLong(
                    statObject.get(MinioTagKey.LAST_MODIFIED.getKey()) == null ? "0" : statObject.get(MinioTagKey.LAST_MODIFIED.getKey())))
                .orElse(0L);
        } catch (Exception e) {
            LOGGER.error("[constructFileMetaVO] failed, error={}", e.toString());
        }
        FileType fileType = analyseFileType(isDirectory, objectKey, distinguishAlgorithmDirectory);
        return FileMetaVO.builder()
                .path(fileVO.getPath())
                .directory(isDirectory)
                .name(fileVO.getName())
                .parentDirectory(fileVO.getParentDirectory())
                .size(size)
                .creatTimestamp(createTimestamp)
                .updateTimestamp(updateTimestamp)
                .fileType(fileType)
                .build();
    }

    private FileType analyseFileType(boolean isDirectory, String objectKey, boolean distinguishAlgorithmDirectory) {
        if (isDirectory) {
            if (distinguishAlgorithmDirectory && filesystemService.checkAlgorithmDirectory(objectKey)) {
                return FileType.ALGORITHM;
            }
            return FileType.DIRECTORY;
        } else {
            String fileExtension = FilenameUtils.getExtension(objectKey);
            return analyseFileType(fileExtension);
        }
    }

    private static FileType analyseFileType(String fileExtension) {
        FileType fileType;
        switch (fileExtension) {
            case "csv":
                fileType = FileType.CSV;
                break;
            case "png":
                fileType = FileType.IMAGE;
                break;
            case "mp4":
                fileType = FileType.VIDEO;
                break;
            case "txt":
                fileType = FileType.TXT;
                break;
            case "my":
                fileType = FileType.MYSQL;
                break;
            case "ps":
                fileType = FileType.POSTGRESQL;
                break;
            case "s3":
                fileType = FileType.S3;
                break;
            default:
                fileType = FileType.NOT_RECOGNITION;
        }
        return fileType;
    }

    private FileVO asFileVO(Long userId, MinioFile minioFile) {
        String objectKey = minioFile.getObjectKey();
        String userRelativePath = pathTransformer.userRelativePath(userId, objectKey);
        boolean isDirectory = StringUtils.endsWith(minioFile.getObjectKey(), "/")
                && 0 == minioFile.getSize()
                || minioFile.isDirectory();
        return constructFileVO(userRelativePath, isDirectory);
    }

    private FileVO constructFileVO(String userRelativePath, boolean isDirectory) {
        return FileVO.builder()
                .path(StringUtils.removeStart(userRelativePath, "/"))
                .directory(isDirectory)
                .name(FilenameUtils.getName(isDirectory
                        ? StringUtils.removeEnd(userRelativePath, "/")
                        : userRelativePath))
                .parentDirectory(FilenameUtils.getPath(isDirectory
                        ? StringUtils.removeEnd(userRelativePath, "/")
                        : userRelativePath))
                .build();
    }
}
