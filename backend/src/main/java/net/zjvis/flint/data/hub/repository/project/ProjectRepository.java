package net.zjvis.flint.data.hub.repository.project;

import java.util.List;
import javax.transaction.Transactional;
import net.zjvis.flint.data.hub.entity.project.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface ProjectRepository extends
    PagingAndSortingRepository<Project, Long> {


    Page<Project> findByUniversalUserId(String universalUserId,
        Pageable pageable);


    void deleteByIdIn(List<Long> projectIdList);
}
