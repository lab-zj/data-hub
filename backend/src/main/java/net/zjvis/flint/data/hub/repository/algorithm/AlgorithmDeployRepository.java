package net.zjvis.flint.data.hub.repository.algorithm;

import net.zjvis.flint.data.hub.entity.algorithm.AlgorithmDeploy;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface AlgorithmDeployRepository extends
    PagingAndSortingRepository<AlgorithmDeploy, Long> {
    Optional<AlgorithmDeploy> findByAlgorithmId(Long algorithmId);
    List<AlgorithmDeploy> findByAlgorithmIdIn(List<Long> algorithmIds);
}
