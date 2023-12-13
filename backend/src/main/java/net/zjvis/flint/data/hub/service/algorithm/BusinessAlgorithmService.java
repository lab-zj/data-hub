package net.zjvis.flint.data.hub.service.algorithm;

import net.zjvis.flint.data.hub.bean.AlgorithmConfiguration;
import net.zjvis.flint.data.hub.entity.algorithm.BusinessAlgorithm;
import net.zjvis.flint.data.hub.lib.minio.MinioException;
import net.zjvis.flint.data.hub.lib.minio.MinioManager;
import net.zjvis.flint.data.hub.repository.algorithm.BusinessAlgorithmRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BusinessAlgorithmService {

    private final BusinessAlgorithmRepository businessAlgorithmRepository;
    private final MinioManager minioManager;
    private final String bucketName;

    public BusinessAlgorithmService(
        BusinessAlgorithmRepository businessAlgorithmRepository,
        MinioManager minioManager,
        AlgorithmConfiguration algorithmConfiguration
    ) {
        this.businessAlgorithmRepository = businessAlgorithmRepository;
        this.minioManager = minioManager;
        this.bucketName = algorithmConfiguration.getMinioBucket();
        try {
            minioManager.bucketCreate(bucketName, true);
        } catch (MinioException e) {
            throw new RuntimeException(e);
        }
    }

    public BusinessAlgorithm add(
        BusinessAlgorithm businessAlgorithm,
        Long universalUserId
    ) {
        return businessAlgorithmRepository.save(businessAlgorithm.toBuilder()
            .universalUserId(universalUserId)
            .gmtCreate(LocalDateTime.now())
            .gmtModify(LocalDateTime.now())
            .algorithm(businessAlgorithm.getAlgorithm())
            .build());
    }


    public void deleteById(Long id) {
        Optional<BusinessAlgorithm> businessAlgorithmOptional = findById(id);
        if (businessAlgorithmOptional.isEmpty()) {
            return;
        }
        businessAlgorithmRepository.save(businessAlgorithmOptional.get().toBuilder()
            .deleted(true)
            .gmtModify(LocalDateTime.now())
            .build());
    }

    public void deleteByAlgorithmId(Long algorithmId) {
        Optional<BusinessAlgorithm> businessAlgorithmOptional = findByAlgorithmId(algorithmId);
        if (businessAlgorithmOptional.isEmpty()) {
            return;
        }
        businessAlgorithmRepository.save(businessAlgorithmOptional.get().toBuilder()
            .deleted(true)
            .gmtModify(LocalDateTime.now())
            .build());
    }

    public Optional<BusinessAlgorithm> findById(Long id) {
        return businessAlgorithmRepository.findById(id);
    }

    public Optional<BusinessAlgorithm> findByAlgorithmId(Long algorithmId) {
        return businessAlgorithmRepository.findByAlgorithmId(algorithmId);
    }

    public BusinessAlgorithm save(BusinessAlgorithm businessAlgorithm) {
        return businessAlgorithmRepository.save(businessAlgorithm);
    }

    public Optional<BusinessAlgorithm> findByNameAndUniversalUserIdAndVersion(String name,
        Long universalUserId, String version) {
        return businessAlgorithmRepository.findByNameAndUniversalUserIdAndVersionAndDeleted(name,
            universalUserId, version, false);
    }

    public Page<BusinessAlgorithm> findByUniversalUserId(Long universalUserId, Pageable pageable) {
        return businessAlgorithmRepository.findByUniversalUserId(universalUserId, pageable);
    }

    public Page<BusinessAlgorithm> findByUniversalUserIdAndDeletedAndType(
        Long universalUserId,
        boolean deleted,
        String type,
        Pageable pageable) {
        return businessAlgorithmRepository.findByUniversalUserIdAndDeletedAndType(
            universalUserId,
            deleted,
            type,
            pageable);
    }


    public Page<BusinessAlgorithm> findByUniversalUserIdAndTypeAndDeletedAndNameContaining(
        Long universalUserId, String type,
        boolean deleted,
        String name,
        Pageable pageable) {
        return businessAlgorithmRepository.findByUniversalUserIdAndTypeAndDeletedAndNameContaining(
            universalUserId,
            type,
            deleted,
            name,
            pageable);
    }

    public List<BusinessAlgorithm> findByAlgorithmIdIn(List<Long> algorithmIds) {
        return businessAlgorithmRepository.findByAlgorithmIdIn(algorithmIds);

    }
}
