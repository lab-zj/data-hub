package net.zjvis.flint.data.hub.lib.calcite.executor;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.lib.calcite.CalciteManager;
import net.zjvis.flint.data.hub.lib.calcite.connector.CalciteConnector;
import net.zjvis.flint.data.hub.lib.calcite.func.UserDefinedFunction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SqlExecutor extends AbstractCalciteExecutor {
    private final String sinkTableName;
    private final String selectSql;

    @Builder
    @Jacksonized
    public SqlExecutor(
            @Singular("sourceConnector")
            List<CalciteConnector> sourceConnectorList,
            @Singular("userDefinedFunction") List<UserDefinedFunction> userDefinedFunctionList,
            CalciteConnector sinkConnector,
            String sinkTableName,
            String selectSql
    ) {
        super(sourceConnectorList, userDefinedFunctionList, sinkConnector);
        this.sinkTableName = sinkTableName;
        this.selectSql = selectSql;
    }

    @Override
    protected Object[] operands() {
        return new Object[]{sinkTableName, false};
    }

    @Override
    protected void doExecute(CalciteManager calciteManager, Consumer<ResultSet> consumer) throws SQLException {
        calciteManager.execute(selectSql, consumer);
    }
    protected String sinkTableName(){
        return sinkTableName;
    }
    protected String selectSql(){
        return selectSql;
    }
}
