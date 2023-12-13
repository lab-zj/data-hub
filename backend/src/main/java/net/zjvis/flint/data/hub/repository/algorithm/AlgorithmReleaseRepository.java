package net.zjvis.flint.data.hub.repository.algorithm;

import net.zjvis.flint.data.hub.entity.algorithm.AlgorithmRelease;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlgorithmReleaseRepository extends PagingAndSortingRepository<AlgorithmRelease, Long> {
    Optional<AlgorithmRelease> findByAlgorithmId(Long algorithmId);
}
