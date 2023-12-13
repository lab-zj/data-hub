package net.zjvis.flint.data.hub.lib.calcite.datasource;

import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.calcite.adapter.cassandra.CassandraSchema;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;

import java.net.InetSocketAddress;

@Getter
@Builder
@EqualsAndHashCode
public class CassandraCalciteDataSource implements CalciteDataSource {
    private final String identifier;
    private final String host;
    private final int port;
    private final String keyspace;
    private final String username;
    private final String password;
    private final String tableName;
    private final String datacenter;

    @Override
    public String identifier() {
        return identifier;
    }

    @Override
    public Schema calciteSchema(SchemaPlus superSchema, String userDefinedSchema) {
        return new CassandraSchema(
                new CqlSessionBuilder()
                        .addContactPoint(new InetSocketAddress(this.getHost(), this.getPort()))
                        .withKeyspace(this.getKeyspace())
                        .withAuthCredentials(this.getUsername(), this.getPassword())
                        .withLocalDatacenter(this.getDatacenter())
                        .build(),
                superSchema,
                userDefinedSchema);
    }
}
