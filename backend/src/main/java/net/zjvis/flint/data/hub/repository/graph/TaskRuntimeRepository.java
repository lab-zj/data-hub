package net.zjvis.flint.data.hub.repository.graph;

import net.zjvis.flint.data.hub.entity.graph.TaskRuntime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface TaskRuntimeRepository extends JpaRepository<TaskRuntime, Long>,
    JpaSpecificationExecutor<TaskRuntime> {

    List<TaskRuntime> findByTaskUuidOrderByGmtModifyDesc(String taskUuid);

    Optional<TaskRuntime> findByTaskUuid(String taskUuid);

    List<TaskRuntime> findByJobId(Long jobId);

    List<TaskRuntime> findAllByTaskUuidIn(List<String> taskUuids);

    void deleteAllByTaskUuidIn(List<String> taskUuids);
}
