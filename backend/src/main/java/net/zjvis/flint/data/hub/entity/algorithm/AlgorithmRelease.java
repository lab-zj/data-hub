package net.zjvis.flint.data.hub.entity.algorithm;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.controller.algorithm.vo.AlgorithmReleaseVO;
import net.zjvis.flint.data.hub.controller.artifact.vo.BusinessDockerRegistryImageVO;
import net.zjvis.flint.data.hub.entity.artifact.DockerRegistryImage;
import net.zjvis.flint.data.hub.util.ActionStatusEnum;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@Jacksonized
@EqualsAndHashCode
@ToString
public class AlgorithmRelease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(cascade = CascadeType.REFRESH)
    private Algorithm algorithm;
    private ActionStatusEnum status;
    @OneToOne(cascade = CascadeType.REFRESH)
    private DockerRegistryImage image;
    private String jobName;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime gmtCreate;
    @LastModifiedDate
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime gmtModify;

    public static AlgorithmRelease fromVO(
        AlgorithmReleaseVO algorithmReleaseVO
    ) {
        return AlgorithmRelease.builder()
            .id(algorithmReleaseVO.getId())
            .algorithm(Algorithm.fromVO(algorithmReleaseVO.getAlgorithm()))
            .status(algorithmReleaseVO.getStatus())
            .image(DockerRegistryImage.builder()
                .registry(algorithmReleaseVO.getImage().getRegistry())
                .repository(algorithmReleaseVO.getImage().getRepository())
                .tag(algorithmReleaseVO.getImage().getTag())
                .build())
            .jobName(algorithmReleaseVO.getJobName())
            .gmtCreate(algorithmReleaseVO.getGmtCreate())
            .gmtModify(algorithmReleaseVO.getGmtModify())
            .build();
    }

    public AlgorithmReleaseVO toVO() {
        AlgorithmReleaseVO algorithmReleaseVO = AlgorithmReleaseVO.builder()
            .id(this.getId())
            .algorithm(this.getAlgorithm().toVO())
            .status(this.getStatus())
            .jobName(this.getJobName())
            .gmtCreate(this.getGmtCreate())
            .gmtModify(this.getGmtModify())
            .build();
        if (this.getImage() != null) {
            algorithmReleaseVO = algorithmReleaseVO.toBuilder()
                .image(BusinessDockerRegistryImageVO.builder()
                    .registry(this.getImage().getRegistry())
                    .repository(this.getImage().getRepository())
                    .tag(this.getImage().getTag())
                    .build())
                .build();
        }
        return algorithmReleaseVO;
    }
}
