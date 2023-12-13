package net.zjvis.flint.data.hub.repository.graph;

import net.zjvis.flint.data.hub.entity.graph.BusinessGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public interface BusinessGraphRepository extends CrudRepository<BusinessGraph, Long> {

    List<BusinessGraph> findByUniversalUserId(String universalUserId);
    List<BusinessGraph> findByGraphUuid(String graphUid);
    List<BusinessGraph> findByProjectIdAndUniversalUserId(Long projectId,  String universalUserId);

    void deleteByGraphUuidIn(List<String> graphUidList);
}
