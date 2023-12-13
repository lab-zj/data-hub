package net.zjvis.flint.data.hub.controller.algorithm;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import net.zjvis.flint.data.hub.Code;
import net.zjvis.flint.data.hub.PagedData;
import net.zjvis.flint.data.hub.StandardResponse;
import net.zjvis.flint.data.hub.controller.algorithm.request.BusinessAlgorithmSearchRequest;
import net.zjvis.flint.data.hub.controller.algorithm.vo.AlgorithmDeployVO;
import net.zjvis.flint.data.hub.controller.algorithm.vo.AlgorithmReleaseVO;
import net.zjvis.flint.data.hub.controller.algorithm.vo.BusinessAlgorithmVO;
import net.zjvis.flint.data.hub.entity.algorithm.Algorithm;
import net.zjvis.flint.data.hub.entity.algorithm.AlgorithmDeploy;
import net.zjvis.flint.data.hub.entity.algorithm.AlgorithmRelease;
import net.zjvis.flint.data.hub.entity.algorithm.BusinessAlgorithm;
import net.zjvis.flint.data.hub.service.account.AccountUtils;
import net.zjvis.flint.data.hub.service.algorithm.AlgorithmDeployService;
import net.zjvis.flint.data.hub.service.algorithm.AlgorithmReleaseService;
import net.zjvis.flint.data.hub.service.algorithm.BusinessAlgorithmService;
import net.zjvis.flint.data.hub.util.ActionStatusEnum;
import net.zjvis.flint.data.hub.util.ApplicationTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/algorithm")
public class AlgorithmController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlgorithmController.class);
    private final BusinessAlgorithmService businessAlgorithmService;
    private final AlgorithmReleaseService algorithmReleaseService;
    private final AlgorithmDeployService algorithmDeployService;
    private final AccountUtils accountUtils;

    public AlgorithmController(
            BusinessAlgorithmService businessAlgorithmService,
            AlgorithmReleaseService algorithmReleaseService,
            AlgorithmDeployService algorithmDeployService,
            AccountUtils accountUtils
    ) {
        this.businessAlgorithmService = businessAlgorithmService;
        this.algorithmReleaseService = algorithmReleaseService;
        this.algorithmDeployService = algorithmDeployService;
        this.accountUtils = accountUtils;
    }

    @Operation(summary = "Add a new business algorithm")
    @PostMapping(value = "")
    public StandardResponse<BusinessAlgorithmVO> add(
            @Parameter(description = "Algorithm definition")
            @RequestBody BusinessAlgorithmVO businessAlgorithm
    ) {
        try {
            String userId = accountUtils.currentUserIdOptional().orElseThrow(
                    () -> new Exception("User id is empty")
            );
            if (StringUtils.isBlank(businessAlgorithm.getName())) {
                businessAlgorithm.setName(UUID.randomUUID()
                        .toString()
                        .replaceAll("-", ""));
            }
            BusinessAlgorithm businessAlgorithmAdded = businessAlgorithmService.add(
                    BusinessAlgorithm.fromVO(businessAlgorithm),
                    Long.parseLong(userId));
            return StandardResponse.<BusinessAlgorithmVO>builder()
                    .data(businessAlgorithmAdded.toVO())
                    .build();
        } catch (Exception e) {
            LOGGER.error("[addAlgorithm] failed, error={}", e.getMessage());
            return StandardResponse.<BusinessAlgorithmVO>builder()
                    .code(Code.BAD_REQUEST.getValue())
                    .message(e.getMessage()).build();
        }
    }

    @Operation(summary = "Delete a business algorithm (Not truly gone)")
    @DeleteMapping(value = "/{businessAlgorithmId}")
    public StandardResponse<String> delete(
            @Parameter(description = "To be deleted business algorithm id")
            @PathVariable Long businessAlgorithmId
    ) {
        businessAlgorithmService.deleteById(businessAlgorithmId);
        return StandardResponse.<String>builder().build();
    }

    @GetMapping(value = "/algorithmid")
    public StandardResponse<BusinessAlgorithmVO> findByAlgorithmId(@RequestParam Long id) {
        try {
            BusinessAlgorithm businessAlgorithm = businessAlgorithmService.findById(id)
                    .orElseThrow(() -> new Exception(
                            String.format("cannot find businessAlgorithm by algorithmId=%s", id)));
            return StandardResponse.<BusinessAlgorithmVO>builder()
                    .data(businessAlgorithm.toVO())
                    .build();
        } catch (Exception e) {
            LOGGER.error("[findByAlgorithmId] failed, error={}", e.getMessage());
            return StandardResponse.<BusinessAlgorithmVO>builder()
                    .code(Code.BAD_REQUEST.getValue())
                    .message(e.getMessage()).build();
        }

    }

    @Operation(summary = "Release the specified business algorithm")
    @PostMapping(value = "/{businessAlgorithmId}/release")
    public StandardResponse<AlgorithmReleaseVO> release(
            @Parameter(description = "Business algorithm id")
            @PathVariable Long businessAlgorithmId
    ) {
        try {
            BusinessAlgorithm businessAlgorithm = businessAlgorithmService.findById(
                    businessAlgorithmId).orElseThrow(
                    () -> new Exception(
                            String.format("Cannot find businessAlgorithm by id=%s", businessAlgorithmId))
            );
            Algorithm algorithm = businessAlgorithm.getAlgorithm();
            return StandardResponse.<AlgorithmReleaseVO>builder()
                    .data(algorithmReleaseService.release(
                            algorithm,
                            businessAlgorithm.getName(),
                            businessAlgorithm.getVersion()).toVO())
                    .build();
        } catch (Exception e) {
            LOGGER.error("[release] failed, need to delete the algorithm={}, error={}", businessAlgorithmId, e.getMessage());
            // no another try now, need to delete this algorithm
            businessAlgorithmService.deleteById(businessAlgorithmId);
            return StandardResponse.<AlgorithmReleaseVO>builder()
                    .code(Code.BAD_REQUEST.getValue())
                    .message(e.getMessage()).build();
        }
    }

    @Operation(summary = "Check the algorithm release info")
    @GetMapping(value = "/{businessAlgorithmId}/release")
    public StandardResponse<AlgorithmReleaseVO> getReleaseInfo(
            @Parameter(description = "Business algorithm id")
            @PathVariable Long businessAlgorithmId
    ) {
        try {
            BusinessAlgorithm businessAlgorithm = businessAlgorithmService.findById(
                    businessAlgorithmId).orElseThrow(
                    () -> new Exception(
                            String.format("Cannot find businessAlgorithm by id=%s", businessAlgorithmId))
            );
            AlgorithmRelease algorithmRelease = algorithmReleaseService
                    .findByAlgorithmId(businessAlgorithm
                            .getAlgorithm()
                            .getId()).orElseThrow(
                            () -> new Exception(
                                    String.format("Cannot find algorithmRelease by algorithm id=%s",
                                            businessAlgorithm.getAlgorithm().getId()))
                    );
            return StandardResponse.<AlgorithmReleaseVO>builder()
                    .data(algorithmRelease.toVO())
                    .build();
        } catch (Exception e) {
            LOGGER.error("[getReleaseInfo] failed, error={}", e.getMessage());
            return StandardResponse.<AlgorithmReleaseVO>builder()
                    .code(Code.BAD_REQUEST.getValue())
                    .message(e.getMessage()).build();
        }
    }

    @PostMapping(value = "/release/{releaseId}/callback")
    public StandardResponse<AlgorithmRelease> releaseCallback(
            @PathVariable Long releaseId,
            @RequestBody Map<String, Object> data
    ) {
        LOGGER.info("[releaseCallback] releasedId={}, data={}", releaseId, data);
        return StandardResponse.<AlgorithmRelease>builder()
                .data(algorithmReleaseService.callback(
                        releaseId,
                        String.valueOf(data.get("token")),
                        (boolean) data.get("succeed")))
                .build();
    }

    @Operation(summary = "Deploy the specified business algorithm")
    @PostMapping(value = "{businessAlgorithmId}/deploy")
    public StandardResponse<AlgorithmDeployVO> deploy(
            @Parameter(description = "Business algorithm id")
            @PathVariable Long businessAlgorithmId
    ) {
        try {
            BusinessAlgorithm businessAlgorithm = businessAlgorithmService.findById(
                    businessAlgorithmId).orElseThrow(
                    () -> new Exception(
                            String.format("Cannot find businessAlgorithm by id=%s", businessAlgorithmId))
            );
            return StandardResponse.<AlgorithmDeployVO>builder()
                    .data(algorithmDeployService.deploy(
                            businessAlgorithm.getAlgorithm(),
                            businessAlgorithm.getName(),
                            businessAlgorithm.getVersion()).toVO())
                    .build();
        } catch (Exception e) {
            LOGGER.error("[deploy] failed, error={}", e.getMessage());
            return StandardResponse.<AlgorithmDeployVO>builder()
                    .code(Code.BAD_REQUEST.getValue())
                    .message(e.getMessage()).build();
        }
    }

    @Operation(summary = "Check the algorithm deploy info")
    @GetMapping(value = "{businessAlgorithmId}/deploy")
    public StandardResponse<AlgorithmDeployVO> getDeployInfo(
            @Parameter(description = "Business algorithm id")
            @PathVariable Long businessAlgorithmId
    ) {
        try {
            BusinessAlgorithm businessAlgorithm = businessAlgorithmService.findById(
                    businessAlgorithmId).orElseThrow(
                    () -> new Exception(
                            String.format("Cannot find businessAlgorithm by id=%s", businessAlgorithmId))
            );
            AlgorithmDeploy algorithmDeploy = algorithmDeployService
                    .findByAlgorithmId(businessAlgorithm
                            .getAlgorithm()
                            .getId()).
                    orElseThrow(
                            () -> new Exception(
                                    String.format("Cannot find algorithmDeploy by algorithm id=%s",
                                            businessAlgorithm.getAlgorithm().getId()))
                    );
            return StandardResponse.<AlgorithmDeployVO>builder()
                    .data(algorithmDeploy.toVO())
                    .build();
        } catch (Exception e) {
            LOGGER.error("[getDeployInfo] failed, error={}", e.getMessage());
            return StandardResponse.<AlgorithmDeployVO>builder()
                    .code(Code.BAD_REQUEST.getValue())
                    .message(e.getMessage()).build();
        }
    }

    @PostMapping(value = "/deploy/{deployId}/callback")
    public StandardResponse<AlgorithmDeploy> deployCallback(
            @PathVariable Long deployId,
            @RequestBody Map<String, Object> data
    ) {
        try {
            return StandardResponse.<AlgorithmDeploy>builder()
                    .data(algorithmDeployService.callback(
                            deployId,
                            String.valueOf(data.get("token")),
                            (boolean) data.get("succeed")))
                    .build();
        } catch (Exception e) {
            return StandardResponse.<AlgorithmDeploy>builder()
                    .code(Code.INTERNAL_ERROR.getValue())
                    .message(e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "Get my related algorithms with filter (Only show deployed algorithm)")
    @GetMapping("/all/search")
    public StandardResponse<PagedData<BusinessAlgorithmVO>> getMyRelatedAlgorithmByFilter(
            @Parameter(description = "Filter")
            BusinessAlgorithmSearchRequest filter
    ) {
        // TODO: filter deployed algorithm by default
        // TODO: fill up other conditions in filter
        try {
            LOGGER.info("[getMyRelatedAlgorithmByFilter] start, request={}", filter);
            String userId = accountUtils.currentUserIdOptional().orElseThrow(
                    () -> new Exception("User id is empty")
            );
            PageRequest pageRequest = PageRequest.of(filter.getPageNumber() - 1,
                    filter.getPageSize(),
                    Sort.by("id"));
            Page<BusinessAlgorithm> page;
            if (StringUtils.isNotBlank(filter.getNameKeyWord())) {
                page = businessAlgorithmService.findByUniversalUserIdAndTypeAndDeletedAndNameContaining(
                        Long.parseLong(userId),
                        filter.getType(),
                    false,
                        filter.getNameKeyWord(),
                        pageRequest);
            } else {
                page = businessAlgorithmService.findByUniversalUserIdAndDeletedAndType(
                        Long.parseLong(userId),
                        false,
                        filter.getType(),
                        pageRequest);
            }

            List<Long> algorithmIds = page.getContent().stream()
                    .map(businessAlgorithm -> businessAlgorithm.getAlgorithm().getId())
                    .collect(Collectors.toList());
            List<Long> deployedAlgorithmIds = algorithmDeployService.findByAlgorithmIdIn(
                            algorithmIds)
                    .stream().filter(deploy -> ActionStatusEnum.SUCCEED == deploy.getStatus()).map(
                            deploy -> deploy.getAlgorithm().getId()
                    ).collect(Collectors.toList());
            List<BusinessAlgorithm> businessAlgorithms = businessAlgorithmService.findByAlgorithmIdIn(
                    deployedAlgorithmIds);
            List<BusinessAlgorithmVO> businessAlgorithmVOs = businessAlgorithms.stream()
                    .map(BusinessAlgorithm::toVO).collect(
                            Collectors.toList());
            return StandardResponse.<PagedData<BusinessAlgorithmVO>>builder()
                    .data(PagedData.<BusinessAlgorithmVO>builder()
                            .totalElements(page.getTotalElements())
                            .totalPages(page.getTotalPages())
                            .content(businessAlgorithmVOs)
                            .build())
                    .build();
        } catch (Exception e) {
            LOGGER.error("[getMyRelatedAlgorithmByFilter] failed, error={}", e.getMessage());
            return StandardResponse.<PagedData<BusinessAlgorithmVO>>builder()
                    .code(Code.BAD_REQUEST.getValue())
                    .message(e.getMessage()).build();
        }
    }

    @Operation(summary = "Check whether algorithm info exist")
    @GetMapping("/info/exist")
    public StandardResponse<Boolean> checkAlgorithmInfoExist(
            @Parameter(description = "Algorithm name")
            @RequestParam String name,
            @Parameter(description = "Algorithm version")
            @RequestParam(defaultValue = "v1.0") String version) {
        try {
            LOGGER.info("[checkAlgorithmInfoExist] start, name={}, version={}", name, version);
            String userId = accountUtils.currentUserIdOptional().orElseThrow(
                    () -> new Exception("User id is empty")
            );
            return StandardResponse.<Boolean>builder()
                    .data(businessAlgorithmService.findByNameAndUniversalUserIdAndVersion(
                            name,
                            Long.parseLong(userId),
                            version
                    ).isPresent())
                    .build();
        } catch (Exception e) {
            LOGGER.error("[checkAlgorithmInfoExist] failed, error={}", e.getMessage());
            return StandardResponse.<Boolean>builder()
                    .code(Code.BAD_REQUEST.getValue())
                    .message(e.getMessage()).build();
        }
    }

    // TODO: get it from db table (data persistence)
    @Operation(summary = "List all available algorithm service types")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "{\n"
                    + "    \"code\": 200,\n"
                    + "    \"data\": {\n"
                    + "        \"Java\": \"https://resource-ops-dev.lab.zjvis.net/charts/cnconti/flask-1.0.0-C358ca08.tgz\",\n"
                    + "        \"Python\": \"https://resource-ops-dev.lab.zjvis.net/charts/cnconti/flask-1.0.0-C358ca08.tgz\"\n"
                    + "    },\n"
                    + "    \"message\": null\n"
                    + "}")
    })
    @GetMapping("/application/type/all")
    public StandardResponse<Map<String, String>> findAllAlgorithmType() {
        return StandardResponse.<Map<String, String>>builder()
                .data(Arrays.stream(ApplicationTypeEnum.values())
                        .collect(Collectors.toMap(
                                ApplicationTypeEnum::getName,
                                ApplicationTypeEnum::getHelmChart)))
                .build();
    }
}
