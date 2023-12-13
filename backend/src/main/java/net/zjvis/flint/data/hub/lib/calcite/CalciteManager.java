package net.zjvis.flint.data.hub.lib.calcite;

import com.google.common.base.Preconditions;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.lib.calcite.datasource.CalciteDataSource;
import net.zjvis.flint.data.hub.lib.calcite.func.UserDefinedFunction;
import net.zjvis.flint.data.hub.util.RelUtils;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.dialect.CalciteSqlDialect;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.RelBuilder;


@EqualsAndHashCode
@ToString
public class CalciteManager {
    public static String asCalciteSql(RelNode node) {
        SqlDialect sqlDialect = CalciteSqlDialect.DEFAULT;
        return new RelToSqlConverter(sqlDialect)
                .visitRoot(node)
                .asStatement()
                .toSqlString(sqlDialect)
                .getSql();
    }

    private final Map<String, String> connectionProperties;
    private final List<CalciteDataSource> dataSourceList;
    private final List<UserDefinedFunction> userDefinedFunctionList;
    private transient CalciteConnection calciteConnection;

    @Builder(toBuilder = true)
    @Jacksonized
    public CalciteManager(
            Map<String, String> connectionProperties,
            @Singular("dataSource")
            List<CalciteDataSource> dataSourceList,
            @Singular("userDefinedFunction")
            List<UserDefinedFunction> userDefinedFunctionList
    ) {
        validateDataSources(dataSourceList);
        validateUserDefinedFunctions(userDefinedFunctionList);
        this.connectionProperties = null != connectionProperties ? connectionProperties : new HashMap<>();
        this.dataSourceList = dataSourceList;
        this.userDefinedFunctionList = userDefinedFunctionList;
    }

    public <T> T query(RelNode relNode, Function<ResultSet, T> extractor) throws SQLException {
        try (PreparedStatement preparedStatement = getConnection().createPrepareContext()
                .getRelRunner()
                .prepareStatement(relNode)) {
            return extractor.apply(preparedStatement.executeQuery());
        }
    }

    public void execute(RelNode relNode, Consumer<ResultSet> consumer) throws SQLException {
        try (PreparedStatement preparedStatement = getConnection().createPrepareContext()
                .getRelRunner()
                .prepareStatement(relNode)) {
            System.out.println(RelUtils.relToSql(relNode));
            consumer.accept(preparedStatement.executeQuery());
        }
    }

    public void executeUpdate(RelNode relNode) throws SQLException {
        try (PreparedStatement preparedStatement = getConnection().createPrepareContext()
                .getRelRunner()
                .prepareStatement(relNode)) {
            preparedStatement.executeUpdate();
        }
    }

    public <T> T query(String sql, Function<ResultSet, T> extractor) throws SQLException {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
            return extractor.apply(preparedStatement.executeQuery());
        }
    }

    public void execute(String sql, Consumer<ResultSet> consumer) throws SQLException {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
            consumer.accept(preparedStatement.executeQuery());
        }
    }

    public void executeUpdate(String sql) throws SQLException {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
            preparedStatement.execute();
        }
    }

    @Deprecated
    public RelBuilder constructRelBuilder() throws SQLException {
        return constructRelBuilder(false);
    }

    public RelBuilder constructRelBuilder(boolean refreshConnection) throws SQLException {
        if (refreshConnection) {
            reconnect();
        }
        return RelBuilder.create(
                Frameworks.newConfigBuilder()
                        .parserConfig(SqlParser.Config.DEFAULT)
                        .defaultSchema(getConnection().getRootSchema())
                        .build()
        );
    }

    public CalciteCatalogReader constructCatalogReader() throws SQLException {
        CalciteConnection connection = getConnection();
        return new CalciteCatalogReader(
                CalciteSchema.from(connection.getRootSchema()),
                dataSourceList.stream()
                        .map(CalciteDataSource::identifier)
                        .collect(Collectors.toList()),
                connection.getTypeFactory(),
                connection.config()
        );
    }

    private CalciteConnection connect() throws SQLException {
        Properties properties = new Properties();
        properties.setProperty(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), Boolean.TRUE.toString());
        properties.setProperty(CalciteConnectionProperty.CREATE_MATERIALIZATIONS.camelName(), Boolean.TRUE.toString());
        properties.setProperty(CalciteConnectionProperty.MATERIALIZATIONS_ENABLED.camelName(), Boolean.TRUE.toString());
        connectionProperties.forEach(properties::setProperty);
        CalciteConnection connection = DriverManager.getConnection("jdbc:calcite:", properties)
                .unwrap(CalciteConnection.class);
        SchemaPlus rootSchema = connection.getRootSchema();
        for (CalciteDataSource dataSource : dataSourceList) {
            rootSchema.add(dataSource.identifier(), dataSource.calciteSchema(rootSchema, dataSource.identifier()));
        }
        for (UserDefinedFunction userDefinedFunction : userDefinedFunctionList) {
            rootSchema.add(userDefinedFunction.getName(), userDefinedFunction.createFunction());
        }
        return connection;
    }

    private void validateDataSources(List<CalciteDataSource> calciteDataSourceList) {
        Preconditions.checkNotNull(calciteDataSourceList, "calciteDataSourceList cannot be null");
        List<String> duplicatedNameList = duplicatedNameList(calciteDataSourceList, CalciteDataSource::identifier);
        if (!duplicatedNameList.isEmpty()) {
            throw new IllegalArgumentException(String.format(
                    "duplicated calcite data source name detected: %s", duplicatedNameList));
        }
        List<List<String>> sameCalciteDataSourceList = calciteDataSourceList.stream()
                .collect(Collectors.groupingBy(
                        dataSource -> dataSource,
                        Collectors.mapping(CalciteDataSource::identifier, Collectors.toList())))
                .values()
                .stream()
                .filter(strings -> strings.size() > 1)
                .collect(Collectors.toList());
        if (!sameCalciteDataSourceList.isEmpty()) {
            throw new IllegalArgumentException(String.format(
                    "same calciteDataSourceList detected: %s", sameCalciteDataSourceList));
        }
    }

    private void validateUserDefinedFunctions(List<UserDefinedFunction> userDefinedFunctionList) {
        Preconditions.checkNotNull(userDefinedFunctionList, "userDefinedFunctionList cannot be null");
        List<String> duplicatedUserDefinedFunctionNameList
                = duplicatedNameList(userDefinedFunctionList, UserDefinedFunction::getName);
        if (!duplicatedUserDefinedFunctionNameList.isEmpty()) {
            throw new IllegalArgumentException(String.format(
                    "duplicated userDefinedFunction name detected: %s", duplicatedUserDefinedFunctionNameList));
        }
    }

    private <T> List<String> duplicatedNameList(List<T> list, Function<T, String> getNameFunction) {
        return list.stream()
                .map(getNameFunction)
                .collect(Collectors.groupingBy(name -> name, Collectors.counting()))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private void reconnect() throws SQLException {
        if (null != calciteConnection) {
            calciteConnection.close();
        }
        calciteConnection = connect();
    }

    private CalciteConnection getConnection() throws SQLException {
        if (null == calciteConnection) {
            calciteConnection = connect();
        }
        return calciteConnection;
    }

    public Schema schema(String schemaName) throws SQLException {
        return  getConnection().getRootSchema().getSubSchema(schemaName);
    }

    @Deprecated
    public Map<String, CalciteDataSource> getDataSources() {
        return dataSourceList.stream()
                .collect(Collectors.toMap(
                        CalciteDataSource::identifier,
                        Function.identity()
                ));
    }
}
