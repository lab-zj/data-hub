package net.zjvis.flint.data.hub.controller.artifact;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import net.zjvis.flint.data.hub.Code;
import net.zjvis.flint.data.hub.PagedData;
import net.zjvis.flint.data.hub.StandardResponse;
import net.zjvis.flint.data.hub.controller.artifact.vo.BusinessDockerRegistryImageVO;
import net.zjvis.flint.data.hub.entity.artifact.BusinessDockerRegistryImage;
import net.zjvis.flint.data.hub.service.account.AccountUtils;
import net.zjvis.flint.data.hub.service.artifact.BusinessDockerRegistryImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/docker/registry")
public class DockerImageController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerImageController.class);
    private final static String DEFAULT_PAGE_NUMBER = "1";
    private final static String DEFAULT_PAGE_SIZE = "100";
    private final BusinessDockerRegistryImageService businessDockerRegistryImageService;
    private final AccountUtils accountUtils;

    public DockerImageController(
            BusinessDockerRegistryImageService businessDockerRegistryImageService,
            AccountUtils accountUtils
    ) {
        this.businessDockerRegistryImageService = businessDockerRegistryImageService;
        this.accountUtils = accountUtils;
    }


    @Operation(summary = "Upsert docker image")
    @PostMapping(value = "")
    public StandardResponse<BusinessDockerRegistryImageVO> upsertImage(
            @Parameter(description = "Image info to upsert")
            @RequestBody BusinessDockerRegistryImageVO dockerRegistryImageVO) {
        try {
            String userId = accountUtils.currentUserIdOptional()
                    .orElseThrow(() -> new Exception("User id is empty"));
            BusinessDockerRegistryImage businessDockerRegistryImage
                    = BusinessDockerRegistryImage.fromVO(dockerRegistryImageVO)
                    .toBuilder()
                    .universalUserId(Long.parseLong(userId))
                    .gmtModify(LocalDateTime.now())
                    .gmtCreate(dockerRegistryImageVO.getId() == null ?
                            LocalDateTime.now() : LocalDateTime.ofInstant(Instant.ofEpochMilli(dockerRegistryImageVO.getGmtCreate()), ZoneId.systemDefault())
                    )
                    .build();
            BusinessDockerRegistryImage upsertedBusinessDockerRegistryImage
                    = businessDockerRegistryImageService.save(businessDockerRegistryImage);
            return StandardResponse.<BusinessDockerRegistryImageVO>builder()
                    .data(upsertedBusinessDockerRegistryImage.toVO()).build();
        } catch (Exception e) {
            LOGGER.error("[upsertImage] failed, error={}", e.getMessage());
            return StandardResponse.<BusinessDockerRegistryImageVO>builder()
                    .code(Code.INTERNAL_ERROR.getValue())
                    .message(e.getMessage()).build();
        }
    }

    @Operation(summary = "Get specified docker image")
    @GetMapping("/{businessImageId}")
    public StandardResponse<BusinessDockerRegistryImageVO> findById(
            @Parameter(description = "Business image id")
            @PathVariable Long businessImageId
    ) {
        try {
            BusinessDockerRegistryImage businessDockerRegistryImage
                    = businessDockerRegistryImageService.findById(businessImageId)
                    .orElseThrow(() -> new Exception(
                            String.format("cannot find business docker registry image by id=%s",
                                    businessImageId))
                    );
            return StandardResponse.<BusinessDockerRegistryImageVO>builder()
                    .data(businessDockerRegistryImage.toVO())
                    .build();
        } catch (Exception e) {
            LOGGER.error("[findBusinessDockerRegistryImageById] failed, error={}", e.getMessage());
            return StandardResponse.<BusinessDockerRegistryImageVO>builder()
                    .code(Code.BAD_REQUEST.getValue())
                    .message(e.getMessage()).build();
        }
    }

    @Operation(summary = "Get all of my related images")
    @GetMapping("/all")
    public StandardResponse<PagedData<BusinessDockerRegistryImageVO>> findAll(
            @Parameter(description = "Page number")
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) Integer pageNumber,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) Integer pageSize,
            @Parameter(description = "Flag of shared for all users")
            @RequestParam(defaultValue = "false") boolean shared
    ) {
        try {
            String userId = accountUtils.currentUserIdOptional().orElseThrow(
                    () -> new Exception("User id is empty")
            );
            Page<BusinessDockerRegistryImage> page = businessDockerRegistryImageService.findByShared(
                    shared,
                    PageRequest.of(pageNumber - 1, pageSize));
            List<BusinessDockerRegistryImageVO> businessDockerRegistryImageVOList = page.getContent()
                    .stream()
                    .map(BusinessDockerRegistryImage::toVO)
                    .collect(Collectors.toList());
            return StandardResponse.<PagedData<BusinessDockerRegistryImageVO>>builder()
                    .data(PagedData.<BusinessDockerRegistryImageVO>builder()
                            .totalElements(page.getTotalElements())
                            .totalPages(page.getTotalPages())
                            .content(businessDockerRegistryImageVOList)
                            .build())
                    .build();
        } catch (Exception e) {
            LOGGER.error("[findAll] failed, error={}", e.getMessage());
            return StandardResponse.<PagedData<BusinessDockerRegistryImageVO>>builder()
                    .code(Code.BAD_REQUEST.getValue())
                    .message(e.getMessage()).build();
        }
    }
}
