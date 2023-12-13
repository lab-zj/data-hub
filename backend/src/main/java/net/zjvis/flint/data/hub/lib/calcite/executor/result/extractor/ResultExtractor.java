package net.zjvis.flint.data.hub.lib.calcite.executor.result.extractor;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.function.Function;

@JsonTypeInfo(property = "type", use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = JdbcResultExtractor.class, name = "JdbcResultExtractor"),
})
public interface ResultExtractor<SourceType, TargetType> extends Function<SourceType, TargetType> {
}
