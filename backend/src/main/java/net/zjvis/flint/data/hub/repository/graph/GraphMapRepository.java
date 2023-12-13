package net.zjvis.flint.data.hub.repository.graph;

import net.zjvis.flint.data.hub.entity.graph.GraphMap;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GraphMapRepository extends CrudRepository<GraphMap, Long> {
    GraphMap findByGraphUuid(String graphUid);
}
