package net.zjvis.flint.data.hub.service.graph;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.zjvis.flint.data.hub.controller.graph.vo.JobVO;
import net.zjvis.flint.data.hub.entity.graph.Job;
import net.zjvis.flint.data.hub.entity.graph.TaskRuntime;
import net.zjvis.flint.data.hub.repository.graph.JobRepository;
import net.zjvis.flint.data.hub.util.JobStatusEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobService.class);

    private final JobRepository jobRepository;
    private final TaskRuntimeService taskRuntimeService;

    public JobService(JobRepository jobRepository,
        TaskRuntimeService taskRuntimeService) {
        this.jobRepository = jobRepository;
        this.taskRuntimeService = taskRuntimeService;
    }


    public Job saveJob(Job job) {
        return jobRepository.save(job);
    }

    public Optional<Job> findJobById(Long id) {
        return jobRepository.findById(id);
    }

    public JobVO cancelOrStopGraphJobWithBuild(Long jobId, JobStatusEnum jobStatusEnum)
        throws Exception {
        Job job = updateJob(jobId, jobStatusEnum.getName(), null);
        List<TaskRuntime> updateTaskRuntimeList = taskRuntimeService.findByJobId(jobId).stream()
            .filter(build -> JobStatusEnum.START.getName().equals(build.getStatus())
                || JobStatusEnum.INIT.getName().equals(build.getStatus()))
            .map(build -> build.toBuilder().gmtModify(
                    LocalDateTime.now())
                .status(jobStatusEnum.getName()).build())
            .collect(Collectors.toList());
        taskRuntimeService.batchSave(updateTaskRuntimeList);
        return job.toVO().toBuilder().taskRuntimeVOList(
            taskRuntimeService.findByJobId(jobId).stream().map(
                TaskRuntime::toVO).collect(
                Collectors.toList())).build();
    }

    public Job updateJob(Long jobId, String status, String result) throws Exception {
        Job job = findJobById(jobId).orElseThrow(
            () -> new Exception(String.format("cannot find job by id=%s", jobId)));
        Job updatedJob = job.toBuilder()
            .status(status)
            .gmtModify(LocalDateTime.now())
            .build();
        if (StringUtils.isNotEmpty(result)) {
            updatedJob = updatedJob.toBuilder()
                .result(result)
                .build();
        }
        return saveJob(updatedJob);
    }

    public JobVO checkJob(Long jobId) throws Exception {
        List<TaskRuntime> taskRuntimeList = taskRuntimeService.findByJobId(jobId);
        if (taskRuntimeList.stream().allMatch(build ->
            StringUtils.equals(build.getStatus(), JobStatusEnum.FINISHED.getName()) &&
                StringUtils.equals(build.getResult(), JobStatusEnum.SUCCESS.getName()))) {
            LOGGER.info("[checkJob][{}] update case 1", jobId);
            return updateJob(jobId, JobStatusEnum.FINISHED.getName(),
                JobStatusEnum.SUCCESS.getName()).toVO().toBuilder()
                .taskRuntimeVOList(taskRuntimeList.stream().map(
                    TaskRuntime::toVO).collect(
                    Collectors.toList())).build();
        } else if (taskRuntimeList.stream().allMatch(build ->
            StringUtils.equals(build.getStatus(), JobStatusEnum.FINISHED.getName()))) {
            LOGGER.info("[checkJob][{}] update case 2", jobId);
            return updateJob(jobId, JobStatusEnum.FINISHED.getName(),
                JobStatusEnum.FAILED.getName()).toVO().toBuilder()
                .taskRuntimeVOList(taskRuntimeList.stream().map(
                    TaskRuntime::toVO).collect(
                    Collectors.toList())).build();
        } else if ((taskRuntimeList.stream().noneMatch(
            build -> StringUtils.equals(build.getStatus(), JobStatusEnum.START.getName())))
            && (taskRuntimeList.stream().noneMatch(
            build -> StringUtils.equals(build.getStatus(), JobStatusEnum.INIT.getName())))
            && (taskRuntimeList.stream().anyMatch(
            build -> StringUtils.equals(build.getResult(), JobStatusEnum.FAILED.getName())))
        ) {
            LOGGER.info("[checkJob][{}] update case 3", jobId);
            return updateJob(jobId, JobStatusEnum.FINISHED.getName(),
                JobStatusEnum.FAILED.getName()).toVO().toBuilder()
                .taskRuntimeVOList(taskRuntimeList.stream().map(
                    TaskRuntime::toVO).collect(
                    Collectors.toList())).build();
        } else if ((taskRuntimeList.stream().noneMatch(
            build -> StringUtils.equals(build.getStatus(), JobStatusEnum.START.getName())))
            && (taskRuntimeList.stream().noneMatch(
            build -> StringUtils.equals(build.getStatus(), JobStatusEnum.INIT.getName())))
            && (taskRuntimeList.stream().anyMatch(
            build -> StringUtils.equals(build.getStatus(), JobStatusEnum.STOPPED.getName())))
        ) {
            LOGGER.info("[checkJob][{}] update case 4", jobId);
            return updateJob(jobId, JobStatusEnum.STOPPED.getName(),
                JobStatusEnum.FAILED.getName()).toVO().toBuilder()
                .taskRuntimeVOList(taskRuntimeList.stream().map(
                    TaskRuntime::toVO).collect(
                    Collectors.toList())).build();
        } else if ((taskRuntimeList.stream().noneMatch(
            build -> StringUtils.equals(build.getStatus(), JobStatusEnum.START.getName())))
            && (taskRuntimeList.stream().noneMatch(
            build -> StringUtils.equals(build.getStatus(), JobStatusEnum.INIT.getName())))
            && (taskRuntimeList.stream().anyMatch(
            build -> StringUtils.equals(build.getStatus(), JobStatusEnum.CANCELED.getName())))
        ) {
            LOGGER.info("[checkJob][{}] update case 5", jobId);
            return updateJob(jobId, JobStatusEnum.CANCELED.getName(),
                JobStatusEnum.FAILED.getName()).toVO().toBuilder()
                .taskRuntimeVOList(taskRuntimeList.stream().map(
                    TaskRuntime::toVO).collect(
                    Collectors.toList())).build();
        } else {
            LOGGER.info("[checkJob][{}] update case default", jobId);
            return findJobById(jobId).get().toVO().toBuilder()
                .taskRuntimeVOList(taskRuntimeList.stream().map(
                    TaskRuntime::toVO).collect(
                    Collectors.toList())).build();
        }
    }

}
