package net.zjvis.flint.data.hub.service.graph.executor;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.entity.graph.Task;
import net.zjvis.flint.data.hub.lib.calcite.connector.CalciteConnector;
import net.zjvis.flint.data.hub.lib.calcite.executor.Executor;
import net.zjvis.flint.data.hub.lib.calcite.func.UserDefinedFunction;
import net.zjvis.flint.data.hub.service.executor.RelNodeExecutor;
import net.zjvis.flint.data.hub.service.graph.TaskRuntimeService;
import org.springframework.util.Base64Utils;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode
@ToString
public class ETLTaskExecutor implements TaskExecutor<Void> {

    private final Executor executor;

    private static final SimpleModule module = new SimpleModule().addDeserializer(
            Timestamp.class, new TimeStampDeserializer()
    );
    public static final ObjectMapper YAML_MAPPER = new ObjectMapper(
            new YAMLFactory()
                    .disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID))
            .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
            .registerModule(module)
            ;

    @Builder
    @Jacksonized
    public ETLTaskExecutor(
        @Singular("sourceConnector")
        List<CalciteConnector> sourceConnectorList,
        @Singular("userDefinedFunction") List<UserDefinedFunction> userDefinedFunctionList,
        CalciteConnector sinkConnector,
        String sinkTableName,
        Map<String, List<Map<String, String>>> meta,
        Task task,
        TaskRuntimeService taskRuntimeService,
        boolean selected
    ) throws IOException {
        executor = RelNodeExecutor.builder()
            .sourceConnectorList(sourceConnectorList)
            .userDefinedFunctionList(userDefinedFunctionList)
            .sinkConnector(sinkConnector)
            .sinkTableName(sinkTableName)
            .meta(meta)
            .task(task)
            .etlTemplate(YAML_MAPPER.readValue(Base64Utils.decode(task.getConfiguration().getBytes(StandardCharsets.UTF_8)), new TypeReference<>() {}))
            .taskRuntimeService(taskRuntimeService)
            .selected(selected)
            .build();
    }

    @Override
    public Void execute() {
        executor.execute();
        return null;
    }

    private static class TimeStampDeserializer extends JsonDeserializer<Timestamp> {

        @Override
        public Timestamp deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            String unixTimestamp = parser.getText().trim();
            return new Timestamp(Long.parseLong(unixTimestamp));
        }
    }
}
