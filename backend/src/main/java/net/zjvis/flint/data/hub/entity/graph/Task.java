package net.zjvis.flint.data.hub.entity.graph;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.zjvis.flint.data.hub.controller.graph.vo.TaskVO;
import net.zjvis.flint.data.hub.util.TaskTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String taskUuid;
    @Column(columnDefinition = "TEXT")
    private String configuration;
    @Column(columnDefinition = "TEXT")
    private String configurationReference;
    @Column(columnDefinition = "TEXT")
    private String inputParamTemplate;
    @Column(columnDefinition = "TEXT")
    private String outputParamTemplate;
    @Basic
    private String taskTypeName;
    private String name;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime gmtCreate;
    @LastModifiedDate //有修改时 会自动时间
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime gmtModify;

    public boolean isAlgorithm() {
        return StringUtils.equals(this.getTaskTypeName(), TaskTypeEnum.Algorithm.getName());
    }

    public boolean isData() {
        return StringUtils.equals(this.getTaskTypeName(), TaskTypeEnum.Data.getName());
    }

    public boolean isETL() {
        return StringUtils.equals(this.getTaskTypeName(), TaskTypeEnum.ETL.getName());
    }

    public boolean isSQL() {
        return StringUtils.equals(this.getTaskTypeName(), TaskTypeEnum.SQL.getName());
    }

    public static Task fromVO(TaskVO taskVO) {
        return Task.builder()
            .id(taskVO.getId())
            .taskUuid(taskVO.getTaskUuid())
            .configuration(taskVO.getConfiguration())
            .configurationReference(taskVO.getConfigurationReference())
            .taskTypeName(taskVO.getTaskTypeName())
            .name(taskVO.getName())
            .inputParamTemplate(taskVO.getInputParamTemplate())
            .outputParamTemplate(taskVO.getOutputParamTemplate())
            .gmtCreate(taskVO.getGmtCreate())
            .gmtModify(taskVO.getGmtModify())
            .build();
    }

    public TaskVO toVO() {
        return TaskVO.builder()
            .id(this.getId())
            .taskUuid(this.getTaskUuid())
            .configuration(this.getConfiguration())
            .configurationReference(this.getConfigurationReference())
            .taskTypeName(this.getTaskTypeName())
            .name(this.getName())
            .inputParamTemplate(this.getInputParamTemplate())
            .outputParamTemplate(this.getOutputParamTemplate())
            .gmtCreate(this.getGmtCreate())
            .gmtModify(this.getGmtModify())
            .build();
    }

}
