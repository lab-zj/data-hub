package net.zjvis.flint.data.hub.service.artifact;

import net.zjvis.flint.data.hub.entity.artifact.DockerRegistryImage;
import net.zjvis.flint.data.hub.repository.artifact.DockerRegistryImageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DockerRegistryImageService {
    private final DockerRegistryImageRepository dockerRegistryImageRepository;

    public DockerRegistryImageService(DockerRegistryImageRepository dockerRegistryImageRepository) {
        this.dockerRegistryImageRepository = dockerRegistryImageRepository;
    }

    public DockerRegistryImage save(DockerRegistryImage dockerRegistryImage) {
        return dockerRegistryImageRepository.save(dockerRegistryImage);
    }

    public Optional<DockerRegistryImage> findById(Long id) {
        return dockerRegistryImageRepository.findById(id);
    }

    public Page<DockerRegistryImage> findAll(Pageable pageable) {
        return dockerRegistryImageRepository.findAll(pageable);
    }
}
