package net.zjvis.flint.data.hub.service.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import net.zjvis.flint.data.hub.entity.project.Project;
import net.zjvis.flint.data.hub.repository.project.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }


    public Page<Project> getProjectListByUserId(String userId, Pageable pageable) {
        return projectRepository.findByUniversalUserId(userId, pageable);
    }

    public Project save(Project project) {
        project = project.toBuilder()
            .modifyTimestamp(
                LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
            .createTimestamp(project.getCreateTimestamp() == null ? LocalDateTime.now()
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                : project.getCreateTimestamp())
            .pinTimestamp(project.getPinTimestamp() == null ? 0 : project.getPinTimestamp())
            .build();
        return projectRepository.save(
            project);
    }

    public void batchDeleteProjects(List<Long> projectIdList) {
        projectRepository.deleteByIdIn(projectIdList);
    }

    public Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId).orElse(null);
    }

}
