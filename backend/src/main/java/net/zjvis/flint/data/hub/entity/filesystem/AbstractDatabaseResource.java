package net.zjvis.flint.data.hub.entity.filesystem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public abstract class AbstractDatabaseResource implements VirtualResource {
    @Schema(example = "192.168.1.203")
    private final String host;
    @Schema(example = "5432")
    private final int port;
    @Schema(example = "test")
    private final String databaseName;
    @Schema(example = "root")
    private final String username;
    @Schema(example = "pass4root")
    private final String password;
    @Schema(example = "test_table")
    private final String table;
}
