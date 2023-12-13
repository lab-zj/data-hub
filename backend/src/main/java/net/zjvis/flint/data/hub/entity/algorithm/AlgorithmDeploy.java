package net.zjvis.flint.data.hub.entity.algorithm;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.controller.algorithm.vo.AlgorithmDeployVO;
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
public class AlgorithmDeploy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(cascade = CascadeType.REFRESH)
    private Algorithm algorithm;
    private ActionStatusEnum status;
    @Lob
    private String valuesContent;
    private String jobName;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime gmtCreate;
    @LastModifiedDate
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime gmtModify;

    public static AlgorithmDeploy fromVO(
        AlgorithmDeployVO algorithmDeployVO
    ) {
        return AlgorithmDeploy.builder()
            .id(algorithmDeployVO.getId())
            .algorithm(Algorithm.fromVO(algorithmDeployVO.getAlgorithmVO()))
            .status(algorithmDeployVO.getStatus())
            .valuesContent(algorithmDeployVO.getValuesContent())
            .jobName(algorithmDeployVO.getJobName())
            .gmtCreate(algorithmDeployVO.getGmtCreate())
            .gmtModify(algorithmDeployVO.getGmtModify())
            .build();
    }

    public AlgorithmDeployVO toVO() {
        return AlgorithmDeployVO.builder()
            .id(this.getId())
            .algorithmVO(this.getAlgorithm().toVO())
            .status(this.getStatus())
            .valuesContent(this.getValuesContent())
            .jobName(this.getJobName())
            .gmtCreate(this.getGmtCreate())
            .gmtModify(this.getGmtModify())
            .build();
    }
}
