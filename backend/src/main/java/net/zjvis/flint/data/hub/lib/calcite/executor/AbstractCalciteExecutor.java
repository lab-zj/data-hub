package net.zjvis.flint.data.hub.lib.calcite.executor;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.zjvis.flint.data.hub.exception.NotSupportException;
import net.zjvis.flint.data.hub.lib.calcite.CalciteManager;
import net.zjvis.flint.data.hub.lib.calcite.connector.CalciteConnector;
import net.zjvis.flint.data.hub.lib.calcite.func.UserDefinedFunction;
import net.zjvis.flint.data.hub.lib.data.Record;
import net.zjvis.flint.data.hub.lib.data.Schema;
import net.zjvis.flint.data.hub.lib.data.sql.ResultSetExtractor;
import net.zjvis.flint.data.hub.lib.data.sql.ResultSetSpliterator;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.validate.SqlUserDefinedFunction;
import org.apache.calcite.tools.RelBuilder;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@EqualsAndHashCode
@ToString
public abstract class AbstractCalciteExecutor implements Executor {

    public static SqlUserDefinedFunction sqlUserDefinedFunction(
        RelDataTypeFactory relDataTypeFactory,
        UserDefinedFunction userDefinedFunction
    ) {
        return new SqlUserDefinedFunction(
            new SqlIdentifier(
                Collections.singletonList(userDefinedFunction.getName()),
                null,
                SqlParserPos.ZERO,
                null),
            SqlKind.OTHER_FUNCTION,
            userDefinedFunction.returnType(relDataTypeFactory),
            userDefinedFunction.operandType(relDataTypeFactory),
            null,
            userDefinedFunction.createFunction()
        );
    }

    private final CalciteManager queryManager;
    private final CalciteConnector sinkConnector;
    private transient RelBuilder relBuilder;

    public AbstractCalciteExecutor(
        List<CalciteConnector> sourceConnectorList,
        List<UserDefinedFunction> userDefinedFunctionList,
        CalciteConnector sinkConnector
    ) {
        this.sinkConnector = sinkConnector;
        queryManager = CalciteManager.builder()
            .dataSourceList(sourceConnectorList.stream()
                .map(CalciteConnector::asDataSource)
                .collect(Collectors.toList()))
            .userDefinedFunctionList(userDefinedFunctionList)
            .build();
    }

    public RelBuilder relBuilder(boolean forceNew, boolean refreshConnection) throws SQLException {
        if (forceNew || null == relBuilder) {
            relBuilder = queryManager.constructRelBuilder(refreshConnection);
        }
        return relBuilder;
    }

    @Override
    public void execute() {
        try {
            doExecute(queryManager, resultSetConsumer(sinkConnector, operands()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected Object[] operands() {
        return new Object[0];
    }

    protected abstract void doExecute(CalciteManager calciteManager, Consumer<ResultSet> consumer)
        throws SQLException;

    private Consumer<ResultSet> resultSetConsumer(CalciteConnector sinkConnector,
        Object... operands) {
        return resultSet -> {
            try {
                Schema schema = ResultSetExtractor.schemaFromResultSet(resultSet);
                Iterator<Record> iterator = StreamSupport.stream(
                    ResultSetSpliterator.<Record>builder()
                        .resultSet(resultSet)
                        .dataExtractor(resultSetInner
                            -> ResultSetExtractor.recordFromResultSet(resultSet, schema))
                        .build(),
                    false
                ).iterator();
                sinkConnector.setup(schema, operands);
                while (iterator.hasNext()) {
                    sinkConnector.insert(iterator.next(), operands);
                }
            } catch (SQLException | NotSupportException | IOException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
