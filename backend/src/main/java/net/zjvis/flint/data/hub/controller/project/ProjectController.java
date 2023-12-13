package net.zjvis.flint.data.hub.controller.project;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.ArrayList;
import java.util.List;
import net.zjvis.flint.data.hub.Code;
import net.zjvis.flint.data.hub.PagedData;
import net.zjvis.flint.data.hub.StandardResponse;
import net.zjvis.flint.data.hub.entity.project.Project;
import net.zjvis.flint.data.hub.service.account.AccountUtils;
import net.zjvis.flint.data.hub.service.project.ProjectService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.NullHandling;
import org.springframework.data.domain.Sort.Order;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/project")
public class ProjectController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectController.class);
    private final static String DEFAULT_PAGE_NUMBER = "1";
    private final static String DEFAULT_PAGE_SIZE = "100";
    private final ProjectService projectService;
    private final AccountUtils accountUtils;

    public ProjectController(ProjectService projectService, AccountUtils accountUtils) {
        this.projectService = projectService;
        this.accountUtils = accountUtils;
    }

    @Operation(summary = "Get my project list")
    @GetMapping("/all")
    public StandardResponse<PagedData<Project>> getAllMyProject(
        @Parameter(description = "Page number")
        @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) Integer pageSize
    ) {
        try {
            String userId = accountUtils.currentUserIdOptional().orElseThrow(
                () -> new Exception("User id is empty")
            );

            List<Order> orders = new ArrayList<>();
            Order orderPinTimestamp = new Order(Sort.Direction.DESC, "pinTimestamp",
                NullHandling.NULLS_LAST);
            orders.add(orderPinTimestamp);
            Order orderCreateTimestamp = new Order(Sort.Direction.DESC, "createTimestamp",
                NullHandling.NULLS_LAST);
            orders.add(orderCreateTimestamp);

            Page<Project> page = projectService.getProjectListByUserId(userId,
                PageRequest.of(pageNumber - 1, pageSize,
                    Sort.by(orders)
                ));
            return StandardResponse.<PagedData<Project>>builder()
                .data(PagedData.<Project>builder()
                    .totalElements(page.getTotalElements())
                    .totalPages(page.getTotalPages())
                    .content(page.getContent())
                    .build()

                ).build();
        } catch (Exception e) {
            LOGGER.error("[getMyAllGraph] failed, error={}", e.getMessage());
            return StandardResponse.<PagedData<Project>>builder()
                .code(Code.BAD_REQUEST.getValue())
                .message(e.getMessage()).build();
        }

    }


    @Operation(summary = "Upsert project info")
    @PostMapping("")
    public StandardResponse<Project> saveProjectInfo(
        @Parameter(description = "To be saved project info")
        @RequestBody Project project) {
        try {
            LOGGER.info("[saveProjectInfo] start, project info={}", project.toString());
            String userId = accountUtils.currentUserIdOptional().orElseThrow(
                () -> new Exception("User id is empty")
            );
            project = project.toBuilder()
                .universalUserId(StringUtils.isEmpty(project.getUniversalUserId()) ? userId
                    : project.getUniversalUserId())
                .build();
            return StandardResponse.<Project>builder().data(
                projectService.save(project)).build();
        } catch (Exception e) {
            LOGGER.error("[saveProjectInfo] failed, error={}", e.getMessage());
            return StandardResponse.<Project>builder()
                .code(Code.BAD_REQUEST.getValue())
                .message(e.getMessage()).build();
        }
    }

    @Operation(summary = "Batch delete project")
    @DeleteMapping("/batch")
    public StandardResponse<String> batchDeleteProjects(
        @Parameter(description = "To be deleted project id list")
        @RequestParam List<Long> projectIdList) {
        LOGGER.info("[batchDeleteProjects] start, projectIdList={}", projectIdList.toString());
        projectService.batchDeleteProjects(projectIdList);
        return StandardResponse.<String>builder()
            .data("success")
            .build();
    }

    @Operation(summary = "Get project")
    @GetMapping("/{projectId}")
    public StandardResponse<Project> getProject(
        @Parameter(description = "Project id")
        @PathVariable Long projectId) {
        try {
            return StandardResponse.<Project>builder()
                .data(projectService.getProjectById(projectId)).build();
        } catch (Exception e) {
            LOGGER.error("[getProject] failed, error={}", e.getMessage());
            return StandardResponse.<Project>builder()
                .code(Code.BAD_REQUEST.getValue())
                .message(e.getMessage()).build();
        }
    }


}
