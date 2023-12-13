package net.zjvis.flint.data.hub.lib.calcite.executor;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.lib.calcite.CalciteManager;
import net.zjvis.flint.data.hub.lib.calcite.connector.CalciteConnector;
import net.zjvis.flint.data.hub.lib.calcite.func.UserDefinedFunction;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.tools.RelBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RelExecutor<DataType> extends AbstractCalciteExecutor {

    private final Function<RelBuilder, RelNode> relBuildProcess;
    private final String sinkTableName;
    @Builder
    @Jacksonized
    public RelExecutor(
        @Singular("connection")
        List<CalciteConnector> connectionList,
        @Singular("userDefinedFunction")
        List<UserDefinedFunction> userDefinedFunctionList,
        CalciteConnector outputCalciteConnector,
        Function<RelBuilder, RelNode> relBuildProcess,
        String sinkTableName
    ) {
        super(connectionList, userDefinedFunctionList, outputCalciteConnector);
        this.relBuildProcess = relBuildProcess;
        this.sinkTableName = sinkTableName;
    }

    @Override
    protected Object[] operands() {
        return new Object[]{sinkTableName, false};
    }
    @Override
    protected void doExecute(CalciteManager calciteManager, Consumer<ResultSet> consumer)
        throws SQLException {
        calciteManager.execute(
            relBuildProcess.apply(calciteManager.constructRelBuilder(false)),
            consumer
        );
    }

}
