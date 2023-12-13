package net.zjvis.flint.data.hub.entity.algorithm;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.controller.algorithm.vo.BusinessAlgorithmVO;
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
public class BusinessAlgorithm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(cascade = CascadeType.ALL)
    private Algorithm algorithm;
    private String name;
    private String version;
    // TODO entity
    private Long universalUserId;
    private String outerServerAddress;
    private boolean shared;
    private String type;
    private boolean deleted;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime gmtCreate;
    @LastModifiedDate //有修改时 会自动时间
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime gmtModify;

    public BusinessAlgorithmVO toVO() {
        return BusinessAlgorithmVO.builder()
            .id(this.getId())
            .algorithm(this.getAlgorithm().toVO())
            .name(this.getName())
            .version(this.getVersion())
            .universalUserId(this.getUniversalUserId())
            .shared(this.isShared())
            .type(this.getType())
            .deleted(this.isDeleted())
            .outerServerAddress(this.getOuterServerAddress())
            .gmtCreate(this.getGmtCreate())
            .gmtModify(this.getGmtModify())
            .build();
    }

    public static BusinessAlgorithm fromVO(BusinessAlgorithmVO businessAlgorithmVO) {
        return BusinessAlgorithm.builder()
            .id(businessAlgorithmVO.getId())
            .algorithm(Algorithm.fromVO(businessAlgorithmVO.getAlgorithm()))
            .name(businessAlgorithmVO.getName())
            .version(businessAlgorithmVO.getVersion())
            .universalUserId(businessAlgorithmVO.getUniversalUserId())
            .shared(businessAlgorithmVO.isShared())
            .type(businessAlgorithmVO.getType())
            .deleted(businessAlgorithmVO.isDeleted())
            .outerServerAddress(businessAlgorithmVO.getOuterServerAddress())
            .gmtCreate(businessAlgorithmVO.getGmtCreate())
            .gmtModify(businessAlgorithmVO.getGmtModify())
            .build();
    }
}
