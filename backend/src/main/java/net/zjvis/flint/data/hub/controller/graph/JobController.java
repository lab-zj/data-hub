package net.zjvis.flint.data.hub.controller.graph;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import net.zjvis.flint.data.hub.Code;
import net.zjvis.flint.data.hub.StandardResponse;
import net.zjvis.flint.data.hub.controller.graph.vo.JobVO;
import net.zjvis.flint.data.hub.entity.graph.Job;
import net.zjvis.flint.data.hub.service.graph.JobService;
import net.zjvis.flint.data.hub.util.JobStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/job")
public class JobController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobController.class);
    private final JobService jobService;

    public JobController(
        JobService jobService) {
        this.jobService = jobService;
    }



    @Operation(summary = "Cancel job")
    @PostMapping("/{jobId}/cancel")
    public StandardResponse<JobVO> cancelJob(
        @Parameter(description = "To be canceled job id")
        @PathVariable Long jobId) {
        Job job;
        JobVO jobVO;
        try {
            LOGGER.info("[cancelJob] start, jobId={}", jobId);
            job = jobService.findJobById(jobId)
                .orElseThrow(
                    () -> new Exception(String.format("cannot find job by job id=%s", jobId)));

            jobVO = jobService.cancelOrStopGraphJobWithBuild(job.getId(), JobStatusEnum.CANCELED);
        } catch (Exception e) {
            LOGGER.error("[cancelJob] failed, error={}", e.getMessage());
            return StandardResponse.<JobVO>builder().code(Code.BAD_REQUEST.getValue())
                .message(e.getMessage()).build();
        }
        return StandardResponse.<JobVO>builder().data(jobVO
        ).build();
    }

    @Operation(summary = "Stop job")
    @PostMapping("/{jobId}/stop")
    public StandardResponse<JobVO> stopJob(
        @Parameter(description = "To be stopped job id")
        @PathVariable Long jobId) {
        Job job;
        JobVO jobVO;
        try {
            LOGGER.info("[stopJob] start, jobId={}", jobId);
            job = jobService.findJobById(jobId)
                .orElseThrow(
                    () -> new Exception(String.format("cannot find job by id=%s", jobId)));
            jobVO = jobService.cancelOrStopGraphJobWithBuild(job.getId(), JobStatusEnum.STOPPED);
        } catch (Exception e) {
            LOGGER.error("[stopJob] failed, error={}", e.getMessage());
            return StandardResponse.<JobVO>builder().code(Code.BAD_REQUEST.getValue())
                .message(e.getMessage()).build();
        }
        return StandardResponse.<JobVO>builder().data(jobVO
        ).build();
    }

    @Operation(summary = "Get job status")
    @GetMapping("/{jobId}/status")
    public StandardResponse<JobVO> getJob(
        @Parameter(description = "Job id of queried graph job info")
        @PathVariable Long jobId) {
        Job job;
        JobVO jobVO;
        try {
            job = jobService.findJobById(jobId)
                .orElseThrow(
                    () -> new Exception(String.format("cannot find job by id=%s", jobId)));
            jobVO = jobService.checkJob(job.getId());
        } catch (Exception e) {
            LOGGER.error("[getJob] failed, error={}", e.getMessage());
            return StandardResponse.<JobVO>builder().code(Code.BAD_REQUEST.getValue())
                .message(e.getMessage()).build();
        }
        return StandardResponse.<JobVO>builder().data(jobVO)
            .build();
    }


}
