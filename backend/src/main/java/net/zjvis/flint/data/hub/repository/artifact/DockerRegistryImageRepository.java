package net.zjvis.flint.data.hub.repository.artifact;

import net.zjvis.flint.data.hub.entity.artifact.DockerRegistryImage;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DockerRegistryImageRepository extends PagingAndSortingRepository<DockerRegistryImage, Long> {
}
