package net.zjvis.flint.data.hub.entity.filesystem;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.controller.algorithm.vo.MinioResourceVO;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@Jacksonized
@EqualsAndHashCode
@ToString
public class MinioResource implements FileResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String objectKey;

    public static MinioResource fromVO(MinioResourceVO minioResourceVO) {
        return MinioResource.builder()
            .id(minioResourceVO.getId())
            .objectKey(minioResourceVO.getObjectKey())
            .build();
    }

    public MinioResourceVO toVO() {
        return MinioResourceVO.builder()
            .id(this.getId())
            .objectKey(this.getObjectKey())
            .build();
    }
}
