package net.zjvis.flint.data.hub.controller.filesystem;

import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import net.zjvis.flint.data.hub.PagedData;
import net.zjvis.flint.data.hub.StandardResponse;
import net.zjvis.flint.data.hub.controller.BasicController;
import net.zjvis.flint.data.hub.controller.filesystem.vo.ColumnVO;
import net.zjvis.flint.data.hub.controller.filesystem.vo.TableVO;
import net.zjvis.flint.data.hub.entity.account.UniversalUser;
import net.zjvis.flint.data.hub.lib.data.Record;
import net.zjvis.flint.data.hub.lib.data.Type;
import net.zjvis.flint.data.hub.lib.minio.MinioException;
import net.zjvis.flint.data.hub.lib.minio.MinioTagKey;
import net.zjvis.flint.data.hub.service.FilesystemService;
import net.zjvis.flint.data.hub.service.account.AccountUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/filesystem/preview")
public class PreviewController implements BasicController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreviewController.class);
    private final PathTransformer pathTransformer;
    private final AccountUtils accountUtils;
    private final FilesystemService filesystemService;

    @Override
    public long id() {
        return 5L;
    }

    public enum ERROR implements BasicController.Error {
        NOT_LOGIN_USER(HttpStatus.UNAUTHORIZED, 1L, "login first"),
        PREVIEW_IMAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 2L, "preview image(%s) failed: %s"),
        PREVIEW_CSV_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 3L, "preview csv(%s) failed: %s"),
        PREVIEW_TEXT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 4L, "preview text(%s) failed: %s"),
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

    public PreviewController(
            PathTransformer pathTransformer,
            AccountUtils accountUtils,
            FilesystemService filesystemService
    ) {
        this.pathTransformer = pathTransformer;
        this.accountUtils = accountUtils;
        this.filesystemService = filesystemService;
    }

    @Operation(summary = "preview image")
    @GetMapping("/image")
    public ResponseEntity<StreamingResponseBody> previewImage(
            @Schema(description = "path of image", example = "/foo/bar/some.png")
            @RequestParam String path
    ) {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .build();
        }
        Long userId = currentUserOptional.get().getId();
        String userIndependentPath = pathTransformer.userIndependentPath(userId, path);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.IMAGE_PNG)
                .body(outputStream -> {
                    try (InputStream inputStream = filesystemService.download(userIndependentPath)) {
                        IOUtils.copy(inputStream, outputStream);
                    }
                });
    }

    @Operation(summary = "preview csv")
    @GetMapping("/csv")
    public StandardResponse<TableVO> previewCsv(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int size,
            @Schema(description = "path of csv", example = "/foo/bar/some.csv")
            @RequestParam String path
    ) {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return errorResponse(ERROR.NOT_LOGIN_USER);
        }
        Long userId = currentUserOptional.get().getId();
        String userIndependentPath = pathTransformer.userIndependentPath(userId, path);
        Page<Record> recordPage;
        try {
            recordPage = filesystemService.previewCsv(userIndependentPath, PageRequest.of(page - 1, size));
        } catch (MinioException e) {
            LOGGER.warn(String.format("preview csv(%s) failed: %s", userIndependentPath, e.getMessage()), e);
            return errorResponse(ERROR.PREVIEW_CSV_FAILED, path, e.getMessage());
        }
        return success(TableVO.wrapFromPage(recordPage));
    }

    @Operation(summary = "preview text")
    @GetMapping("/text")
    public StandardResponse<String> previewText(
            @Schema(description = "path of text file", example = "/foo/bar/some.csv")
            @RequestParam String path,
            @Schema(description = "size of characters to preview", example = "2048")
            @RequestParam(defaultValue = "4096") int size
    ) {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return errorResponse(ERROR.NOT_LOGIN_USER);
        }
        Long userId = currentUserOptional.get().getId();
        String userIndependentPath = pathTransformer.userIndependentPath(userId, path);
        try {
            return success(filesystemService.previewText(userIndependentPath, size));
        } catch (IOException e) {
            LOGGER.warn(String.format("preview csv(%s) failed: %s", userIndependentPath, e.getMessage()), e);
            return errorResponse(ERROR.PREVIEW_TEXT_FAILED, path, e.getMessage());
        }
    }

    @Operation(summary = "preview video")
    @GetMapping("/video")
    public ResponseEntity<List<ResourceRegion>> previewVideo(
            @Schema(description = "path of video to be previewed")
            @RequestParam(defaultValue = "/") String path,
            @RequestHeader HttpHeaders headers
    ) throws IOException {
        Optional<UniversalUser> currentUserOptional = accountUtils.currentUserOptional();
        if (currentUserOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .build();
        }
        Long userId = currentUserOptional.get().getId();
        String userIndependentPath = pathTransformer.userIndependentPath(userId, path);
        String size = filesystemService.getStatObject(userIndependentPath)
                .get(MinioTagKey.SIZE.getKey());
        InputStream inputStream = filesystemService.download(userIndependentPath);
        InputStreamResource inputStreamResource = new SizedInputStreamResource(inputStream, Long.parseLong(size));
        List<HttpRange> rangeList = headers.getRange();
        MediaType mediaType = MediaType.valueOf(com.google.common.net.MediaType.MP4_VIDEO.toString());
        if (rangeList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(mediaType)
                    .body(Collections.singletonList(new ResourceRegion(
                            inputStreamResource, 0, inputStreamResource.contentLength())));
        }
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(mediaType)
                .body(HttpRange.toResourceRegions(rangeList, inputStreamResource));
    }
}
