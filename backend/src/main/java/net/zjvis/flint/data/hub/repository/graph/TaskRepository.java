package net.zjvis.flint.data.hub.repository.graph;

import net.zjvis.flint.data.hub.entity.graph.Task;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface TaskRepository extends CrudRepository<Task, Long> {
    List<Task> findByTaskUuidIn(List<String> taskUuidList);

    Optional<Task> findByTaskUuid(String taskUuid);

    void deleteByTaskUuidIn(List<String> taskUuidList);


}
