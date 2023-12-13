package net.zjvis.flint.data.hub.entity.graph;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.zjvis.flint.data.hub.controller.graph.vo.TaskRuntimeVO;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
public class TaskRuntime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String taskUuid;
    @Column(columnDefinition = "TEXT")
    private String param;
    @Lob
    private String data;
    @Lob
    private String cloudData;
    @Lob
    private String message;
    private Long jobId;
    private String status;
    private String result;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime gmtCreate;
    @LastModifiedDate
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime gmtModify;

    public TaskRuntimeVO toVO() {
        return TaskRuntimeVO.builder()
            .id(this.getId())
            .taskUuid(this.getTaskUuid())
            //.param(param)
            .data(this.getData())
            .cloudData(this.getCloudData())
            .message(this.getMessage())
            .jobId(this.getJobId())
            .status(this.getStatus())
            .result(this.getResult())
            .gmtCreate(this.getGmtCreate())
            .gmtModify(this.getGmtModify())
            .build();
    }
}
