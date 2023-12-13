package net.zjvis.flint.data.hub.repository.artifact;

import net.zjvis.flint.data.hub.entity.artifact.BusinessDockerRegistryImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessDockerRegistryImageRepository extends
    PagingAndSortingRepository<BusinessDockerRegistryImage, Long> {

    Page<BusinessDockerRegistryImage> findByUniversalUserId(Long universalUserId,
        Pageable pageable);

    Page<BusinessDockerRegistryImage> findByUniversalUserIdOrShared(Long universalUserId,
        boolean shared, Pageable pageable);

    Page<BusinessDockerRegistryImage> findByShared(boolean shared, Pageable pageable);

    BusinessDockerRegistryImage findByDockerRegistryImageId(Long dockerRegistryImageId);
}
