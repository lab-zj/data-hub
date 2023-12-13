package net.zjvis.flint.data.hub.repository.algorithm;

import net.zjvis.flint.data.hub.entity.algorithm.BusinessAlgorithm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface BusinessAlgorithmRepository extends
    PagingAndSortingRepository<BusinessAlgorithm, Long> {

    Page<BusinessAlgorithm> findByUniversalUserId(Long universalUserId, Pageable pageable);

    Page<BusinessAlgorithm> findByUniversalUserIdAndDeletedAndType(
        Long universalUserId,
        boolean deleted,
        String type,
        Pageable pageable);

    Page<BusinessAlgorithm> findByUniversalUserIdAndTypeAndDeletedAndNameContaining(
        Long universalUserId,
        String type,
        boolean deleted,
        String name,
        Pageable pageable);

    Optional<BusinessAlgorithm> findByAlgorithmId(Long algorithmId);

    Optional<BusinessAlgorithm> findByNameAndUniversalUserIdAndVersionAndDeleted(String name,
        Long universalUserId, String version, Boolean deleted);

    List<BusinessAlgorithm> findByAlgorithmIdIn(List<Long> algorithmIds);
}
