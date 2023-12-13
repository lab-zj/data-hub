package net.zjvis.flint.data.hub.entity.filesystem;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MySqlResource extends AbstractDatabaseResource {
    @Builder
    @Jacksonized
    public MySqlResource(
            String host,
            int port,
            String databaseName,
            String username,
            String password,
            String table
    ) {
        super(host, port, databaseName, username, password, table);
    }
}
