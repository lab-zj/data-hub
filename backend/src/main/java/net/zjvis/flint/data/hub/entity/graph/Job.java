package net.zjvis.flint.data.hub.entity.graph;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.zjvis.flint.data.hub.controller.graph.vo.JobVO;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@ToString
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String graphUuid;
    @Column(columnDefinition = "TEXT")
    private String param;
    private String status;
    private String result;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime gmtCreate;

    @LastModifiedDate //有修改时 会自动时间
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime gmtModify;

    public JobVO toVO() {
        return JobVO.builder()
            .id(this.getId())
            .graphUuid(this.getGraphUuid())
            //.param(param)
            .status(this.getStatus())
            .result(this.getResult())
            .gmtCreate(this.getGmtCreate() == null ? null : this.getGmtCreate().toInstant(
                ZoneOffset.of("+8")).toEpochMilli())
            .gmtModify(this.getGmtModify() == null ? null : this.getGmtCreate().toInstant(
                ZoneOffset.of("+8")).toEpochMilli())
            .build();
    }
}
