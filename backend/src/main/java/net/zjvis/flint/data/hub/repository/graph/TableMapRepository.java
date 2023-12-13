package net.zjvis.flint.data.hub.repository.graph;

import net.zjvis.flint.data.hub.entity.graph.TableMap;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableMapRepository extends CrudRepository<TableMap, Long> {

    TableMap findByFuzzyName(String name);
}
