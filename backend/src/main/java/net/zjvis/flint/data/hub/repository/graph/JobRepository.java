package net.zjvis.flint.data.hub.repository.graph;

import net.zjvis.flint.data.hub.entity.graph.Job;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends CrudRepository<Job, Long> {
}
