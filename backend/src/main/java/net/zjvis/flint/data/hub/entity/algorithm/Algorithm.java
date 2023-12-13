package net.zjvis.flint.data.hub.entity.algorithm;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.controller.algorithm.vo.AlgorithmVO;
import net.zjvis.flint.data.hub.controller.artifact.vo.BusinessDockerRegistryImageVO;
import net.zjvis.flint.data.hub.entity.artifact.DockerRegistryImage;
import net.zjvis.flint.data.hub.entity.filesystem.MinioResource;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@Jacksonized
@EqualsAndHashCode
@ToString
public class Algorithm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Lob
    private String valuesYaml;
    @Lob
    private String configurationYaml;
    @Lob
    private String paramInfoTemplateYaml;
    private String registry;
    @OneToOne(cascade = CascadeType.REFRESH)
    private DockerRegistryImage baseImage;
    @OneToOne(cascade = CascadeType.ALL)
    private MinioResource codeTarGz;
    private String dirPath;

    public static Algorithm fromVO(AlgorithmVO algorithmVO) {
        return Algorithm.builder()
            .id(algorithmVO.getId())
            .valuesYaml(algorithmVO.getValuesYaml())
            .configurationYaml(algorithmVO.getConfigurationYaml())
            .paramInfoTemplateYaml(algorithmVO.getParamInfoTemplateYaml())
            .registry(algorithmVO.getRegistry())
            .baseImage(DockerRegistryImage.builder()
                .id(algorithmVO.getBaseImage().getId())
                .registry(algorithmVO.getBaseImage().getRegistry())
                .repository(algorithmVO.getBaseImage().getRepository())
                .tag(algorithmVO.getBaseImage().getTag())
                .build()

            )
            .dirPath(algorithmVO.getDirPath())
            .build();
    }

    public AlgorithmVO toVO() {
        return AlgorithmVO.builder()
            .id(this.getId())
            .valuesYaml(this.getValuesYaml())
            .configurationYaml(this.getConfigurationYaml())
            .paramInfoTemplateYaml(this.getParamInfoTemplateYaml())
            .registry(this.getRegistry())
            .baseImage(BusinessDockerRegistryImageVO.builder()
                .id(this.getBaseImage().getId())
                .registry(this.getBaseImage().getRegistry())
                .repository(this.getBaseImage().getRepository())
                .tag(this.getBaseImage().getTag())
                // TODO: to be filled
                //.universalUserId(this.getBaseImage().get)
                //.shared(this.getBaseImage())
                .build())
            //.codeTarGz(this.getCodeTarGz().toVO())
            .dirPath(this.getDirPath())
            .build();
    }
}
