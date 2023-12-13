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
@Schema
public class TaskRuntimeVO {

  @Schema(description = "Generated by database to identify unique task build")
  private Long id;
  @Schema(description = "Associated task uuid")
  private String taskUuid;
  //private String param;
  @Schema(description = "Task build result data")
  private String data;
  @Schema(description = "Task build result data in cloud storage")
  private String cloudData;
  @Schema(description = "Task build error message")
  private String message;
  @Schema(description = "Associated graph job id")
  private Long jobId;
  @Schema(description = "Task build status", example = "init, start , finished, canceled, stopped")
  private String status;
  @Schema(description = "Task build result", example = "success , failed")
  private String result;
  @Schema(description = "Task build start time")
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  private LocalDateTime gmtCreate;
  @Schema(description = "Task build update time")
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  private LocalDateTime gmtModify;

}
