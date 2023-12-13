package net.zjvis.flint.data.hub.lib.calcite.executor;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import net.zjvis.flint.data.hub.service.executor.DataNodeExecutor;
import net.zjvis.flint.data.hub.service.executor.RelNodeExecutor;
import net.zjvis.flint.data.hub.service.executor.SqlNodeExecutor;

@JsonTypeInfo(property = "type", use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DataNodeExecutor.class, name = "DataNodeExecutor"),
        @JsonSubTypes.Type(value = SqlNodeExecutor.class, name = "SqlNodeExecutor"),
        @JsonSubTypes.Type(value = RelNodeExecutor.class, name = "RelNodeExecutor"),
})
public interface Executor {
    void execute();
}
