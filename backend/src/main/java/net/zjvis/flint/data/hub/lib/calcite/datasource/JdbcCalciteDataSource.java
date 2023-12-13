package net.zjvis.flint.data.hub.lib.calcite.datasource;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import org.jooq.SQLDialect;

import java.sql.Connection;


@Getter
@Builder
@EqualsAndHashCode
public class JdbcCalciteDataSource implements CalciteDataSource {
    private final String identifier;
    private final String host;
    // TODO rename
    private final Driver driver;
    private final int port;
    private final String databaseName;
    private final String databaseCatalog;
    private final String databaseSchema;
    private final String username;
    private final String password;
    @EqualsAndHashCode.Exclude
    private transient Connection connection;

    @Override
    public String identifier() {
        return identifier;
    }

    @Override
    public Schema calciteSchema(SchemaPlus parentSchema, String identifier) {
        return JdbcSchema.create(parentSchema, identifier, dataSource(), databaseCatalog, databaseSchema);
    }

    public javax.sql.DataSource dataSource() {
        return JdbcSchema.dataSource(
                connectionUrl(),
                driver.getClassName(),
                username,
                password
        );
    }

    private String connectionUrl() {
        return String.format(driver.getUrlTemplate(), host, port, databaseName);
    }

    @Getter
    public enum Driver {
        Postgresql(org.postgresql.Driver.class.getName(),
                "jdbc:postgresql://%s:%s/%s?stringtype=unspecified", SQLDialect.POSTGRES),
        Mysql(com.mysql.cj.jdbc.Driver.class.getName(),
                "jdbc:mysql://%s:%s/%s?characterEncoding=UTF-8", SQLDialect.MYSQL),
        ;

        private final String className;
        private final String urlTemplate;
        private final SQLDialect sqlDialect;

        Driver(String className, String urlTemplate, SQLDialect sqlDialect) {
            this.className = className;
            this.urlTemplate = urlTemplate;
            this.sqlDialect = sqlDialect;
        }
    }
}
