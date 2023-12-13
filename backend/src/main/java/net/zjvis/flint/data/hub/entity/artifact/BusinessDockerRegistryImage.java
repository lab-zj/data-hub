package net.zjvis.flint.data.hub.entity.artifact;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.controller.artifact.vo.BusinessDockerRegistryImageVO;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@EqualsAndHashCode
@ToString
@Getter
public class BusinessDockerRegistryImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(cascade = CascadeType.ALL)
    private DockerRegistryImage dockerRegistryImage;
    private Long universalUserId;
    private boolean shared;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime gmtCreate;
    @LastModifiedDate //有修改时 会自动时间
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime gmtModify;

    public static BusinessDockerRegistryImage fromVO(
        BusinessDockerRegistryImageVO businessDockerRegistryImageVO) {
        return BusinessDockerRegistryImage.builder()
            .id(businessDockerRegistryImageVO.getId())
            .dockerRegistryImage(DockerRegistryImage.builder()
                .registry(businessDockerRegistryImageVO.getRegistry())
                .repository(businessDockerRegistryImageVO.getRepository())
                .tag(businessDockerRegistryImageVO.getTag())
                .build())
            .universalUserId(businessDockerRegistryImageVO.getUniversalUserId())
            .shared(businessDockerRegistryImageVO.isShared())
            .build();
    }

    public BusinessDockerRegistryImageVO toVO() {
        return BusinessDockerRegistryImageVO.builder()
            .id(this.getId())
            .baseImageId(this.getDockerRegistryImage().getId())
            .registry(this.getDockerRegistryImage().getRegistry())
            .repository(this.getDockerRegistryImage().getRepository())
            .tag(this.getDockerRegistryImage().getTag())
            .universalUserId(this.getUniversalUserId())
            .shared(this.isShared())
            .gmtCreate(this.getGmtCreate().toInstant(ZoneOffset.of("+8")).toEpochMilli())
            .gmtModify(this.getGmtModify().toInstant(ZoneOffset.of("+8")).toEpochMilli())
            .build();
    }
}
