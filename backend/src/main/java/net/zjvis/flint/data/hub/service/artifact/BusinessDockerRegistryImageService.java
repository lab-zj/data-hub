package net.zjvis.flint.data.hub.service.artifact;

import net.zjvis.flint.data.hub.entity.artifact.BusinessDockerRegistryImage;
import net.zjvis.flint.data.hub.repository.artifact.BusinessDockerRegistryImageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BusinessDockerRegistryImageService {

    private final BusinessDockerRegistryImageRepository businessDockerRegistryImageRepository;

    public BusinessDockerRegistryImageService(
        BusinessDockerRegistryImageRepository businessDockerRegistryImageRepository) {
        this.businessDockerRegistryImageRepository = businessDockerRegistryImageRepository;
    }


    public BusinessDockerRegistryImage save(
        BusinessDockerRegistryImage businessDockerRegistryImage) {
        return businessDockerRegistryImageRepository.save(businessDockerRegistryImage);
    }

    public Optional<BusinessDockerRegistryImage> findById(Long id) {
        return businessDockerRegistryImageRepository.findById(id);
    }

    public Page<BusinessDockerRegistryImage> findAll(Pageable pageable) {
        return businessDockerRegistryImageRepository.findAll(pageable);
    }


    public Page<BusinessDockerRegistryImage> findByUniversalUserId(Long universalUserId,
        Pageable pageable) {
        return businessDockerRegistryImageRepository.findByUniversalUserId(universalUserId,
            pageable);
    }

    public Page<BusinessDockerRegistryImage> findByUniversalUserIdOrShared(Long universalUserId,
        boolean shared, Pageable pageable) {
        return businessDockerRegistryImageRepository.findByUniversalUserIdOrShared(universalUserId,
            shared, pageable);
    }

    public Page<BusinessDockerRegistryImage> findByShared(boolean shared, Pageable pageable) {
        return businessDockerRegistryImageRepository.findByShared(shared, pageable);
    }
}
