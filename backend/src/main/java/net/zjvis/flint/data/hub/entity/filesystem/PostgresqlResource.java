package net.zjvis.flint.data.hub.entity.filesystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PostgresqlResource extends AbstractDatabaseResource {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @Schema(description = "postgresql schema, required parameter", required = true)
    private final String schema;

    @Builder
    @Jacksonized
    public PostgresqlResource(
            String host,
            int port,
            String databaseName,
            String username,
            String password,
            String schema,
            String table
    ) {
        super(host, port, databaseName, username, password, table);
        this.schema = schema;
    }
}
