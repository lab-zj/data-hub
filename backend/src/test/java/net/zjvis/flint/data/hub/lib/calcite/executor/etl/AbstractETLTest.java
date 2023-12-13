package net.zjvis.flint.data.hub.lib.calcite.executor.etl;

import com.fasterxml.jackson.core.JacksonException;
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
import net.zjvis.flint.data.hub.entity.etl.ETL;
import net.zjvis.flint.data.hub.entity.etl.udf.IUDFSampleFunction;
import net.zjvis.flint.data.hub.lib.calcite.connector.CalciteConnector;
import net.zjvis.flint.data.hub.lib.calcite.executor.RelExecutor;
import net.zjvis.flint.data.hub.lib.data.*;
import net.zjvis.flint.data.hub.service.graph.planner.conf.ETLVertexConfiguration;
import org.apache.calcite.linq4j.tree.Types;
import org.apache.calcite.schema.ScalarFunction;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;
import org.apache.catalina.util.URLEncoder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AbstractETLTest {

    private final Logger logger = LoggerFactory.getLogger(AbstractETLTest.class);

    private static final SimpleModule module = new SimpleModule().addDeserializer(
            Timestamp.class, new TimeStampDeserializer()
    );

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(
            new YAMLFactory()
                    .disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID))
            .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
            .registerModule(module)
            ;


    public void testYamlSerialization() throws IOException {
        ETL etl = etl();
        String yamlContent = OBJECT_MAPPER.writeValueAsString(etl);
        System.out.println(yamlContent);
        String encode = Base64Utils.encodeToString(yamlContent.getBytes(StandardCharsets.UTF_8));
        System.out.println(encode);
        ETL etlDeserialized = OBJECT_MAPPER.readValue(yamlContent, new TypeReference<>() {
        });
        ETL etlDeserialized2 = OBJECT_MAPPER.readValue(Base64Utils.decode(encode.getBytes(StandardCharsets.UTF_8)), new TypeReference<>() {});

        Assertions.assertEquals(etl.getClass(), etlDeserialized.getClass());
        Assertions.assertEquals(etl.getClass(), etlDeserialized2.getClass());
        Assertions.assertEquals(etl, etlDeserialized);
        Assertions.assertEquals(etl, etlDeserialized2);
    }


    Map<String, ScalarFunction> wrapFuncs(List<IUDFSampleFunction> functions) {
        Map<String, ScalarFunction> resultMap = new HashMap<>();
        for (IUDFSampleFunction function : functions) {
            resultMap.put(function.getClass().getSimpleName(),
                    ScalarFunctionImpl.create(Types.lookupMethod(function.getClass(), function.getMethodName(), function.getInputType())));
        }
        return resultMap;
    }

    public void testExecution() {
        initExecutor().execute();
    }


    protected abstract ETL etl();

    protected abstract RelExecutor<Table> initExecutor();


    public static final List<Type> TYPE_CANDIDATE_LIST = Arrays.stream(Type.values())
            .filter(type -> !Type.LOB.equals(type) && !Type.DATE_TIME.equals(type) && !Type.DOUBLE.equals(type))
            .collect(Collectors.toList());

    public static LogicalTable randomLogicalTable(List<Type> assignedTypes) {
        int columnCount = ThreadLocalRandom.current().nextInt(8, 16);
        int recordCount = ThreadLocalRandom.current().nextInt(8, 32);
        Schema schema = randomSchema(columnCount, assignedTypes);
        return LogicalTable.builder()
                .schema(schema)
                .recordList(IntStream.range(0, recordCount)
                        .mapToObj(index -> randomRecord(schema))
                        .collect(Collectors.toList()))
                .build();
    }

    public static Schema randomSchema(int columnCount, List<Type> assignedTypes) {
        return Schema.builder()
                .nameList(IntStream.range(0, columnCount)
                        .mapToObj(index -> RandomStringUtils.randomAlphabetic(8))
                        .collect(Collectors.toList()))
                .typeList(IntStream.range(0, columnCount)
                        .mapToObj(index -> assignedTypes.get(
                                ThreadLocalRandom.current().nextInt(0, assignedTypes.size())))
                        .collect(Collectors.toList()))
                .build();
    }

    public static Record randomRecord(Schema schema) {
        return Record.builder()
                .valueList(schema.getTypeList()
                        .stream()
                        .map(type -> {
                            switch (type) {
                                case STRING:
                                    return RandomStringUtils.randomAlphanumeric(8);
                                case BOOLEAN:
                                    return ThreadLocalRandom.current().nextBoolean();
                                case INTEGER:
                                    return ThreadLocalRandom.current().nextLong();
                                case FLOAT:
                                case DOUBLE:
                                    return ThreadLocalRandom.current().nextDouble();
                                case DATE_TIME:
                                    long min = Timestamp.valueOf("2000-01-01 00:00:00").getTime();
                                    long max = Timestamp.valueOf("2099-01-01 00:00:00").getTime();
                                    return new Timestamp(
                                            min + ThreadLocalRandom.current().nextLong(max - min) / 1000 * 1000);
                                case LOB:
                                    return RandomStringUtils.randomAlphanumeric(128)
                                            .getBytes(StandardCharsets.UTF_8);
                                default:
                                    throw new RuntimeException(String.format("not support type(%s)", type));
                            }
                        }).collect(Collectors.toList()))
                .build();
    }

    public static void writeCsvFile(File oneFile, LogicalTable csvTable) throws IOException {
        FileUtils.writeStringToFile(
                oneFile,
                String.format("%s\n%s",
                        IntStream.range(0, csvTable.getSchema().columnSize())
                                .mapToObj(index -> String.format(
                                        "%s:%s",
                                        csvTable.getSchema().name(index),
                                        csvTypeHint(csvTable.getSchema().type(index))
                                )).collect(Collectors.joining(",")),
                        csvTable.getRecordList().stream()
                                .map(record -> record.getValueList().stream()
                                        .map(value -> value instanceof byte[]
                                                ? new String((byte[]) value, StandardCharsets.UTF_8)
                                                : String.valueOf(value))
                                        .collect(Collectors.joining(",")))
                                .collect(Collectors.joining("\n"))
                ),
                StandardCharsets.UTF_8
        );
    }

    private static String csvTypeHint(Type type) {
        switch (type) {
            case STRING:
            case LOB:
                return "string";
            case INTEGER:
                return "long";
            case FLOAT:
            case DOUBLE:
                return "double";
            case BOOLEAN:
                return "boolean";
            case DATE_TIME:
                return "timestamp";
            default:
                throw new RuntimeException(String.format("not support type(%s)", type));
        }
    }

    private static class TimeStampDeserializer extends JsonDeserializer<Timestamp> {

        @Override
        public Timestamp deserialize(JsonParser parser, DeserializationContext context) throws IOException, JacksonException {
            String unixTimestamp = parser.getText().trim();
            return new Timestamp(Long.parseLong(unixTimestamp));
        }
    }
}
