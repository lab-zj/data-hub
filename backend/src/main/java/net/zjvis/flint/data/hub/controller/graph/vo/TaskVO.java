package net.zjvis.flint.data.hub.controller.graph.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder(toBuilder = true)
@Jacksonized
@ToString
@EqualsAndHashCode
@Schema(description = "Task node info")
public class TaskVO {
    @Schema(description = "Generated by database to identify unique task")
    private Long id;
    @Schema(description = "Generated by server to identify unique task")
    private String taskUuid;
    @Schema(description = "(Except data/algorithm task node)Task node config, with yaml format", example = "")
    private String configuration;
    @Schema(description = "(For data/algorithm task node) data source file path/business algorithm id", example = "/path/to/directory/file.txt OR 1")
    private String configurationReference;
    @Schema(description = "Task node type", example = "data, algorithm, sql")
    private String taskTypeName;
    @Schema(description = "User defined task node name")
    private String name;
    @Schema(description = "Task input config param info")
    private String inputParamTemplate;
    @Schema(description = "Task output config param info")
    private String outputParamTemplate;
    @Schema(description = "Task created date")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime gmtCreate;
    @Schema(description = "Task modified date")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime gmtModify;
}
