package net.zjvis.flint.data.hub.lib.calcite.executor.result;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@Builder
@Getter
@Jacksonized
@EqualsAndHashCode
@ToString
public class ExecutorResult<DataType> {
    private int code;
    private DataType data;
    private String message;
}
