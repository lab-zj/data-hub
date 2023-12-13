package net.zjvis.flint.data.hub.lib.calcite.executor;

import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;

public enum FilterType {
    EQUALS(SqlStdOperatorTable.EQUALS),
    NOT_EQUALS(SqlStdOperatorTable.NOT_EQUALS),
    GREATER_THAN(SqlStdOperatorTable.GREATER_THAN),
    GREATER_THAN_OR_EQUAL(SqlStdOperatorTable.GREATER_THAN_OR_EQUAL),
    LESS_THAN(SqlStdOperatorTable.LESS_THAN),
    LESS_THAN_OR_EQUAL(SqlStdOperatorTable.LESS_THAN_OR_EQUAL),
    IN(SqlStdOperatorTable.IN),
    NOT_IN(SqlStdOperatorTable.NOT_IN),
    LIKE(SqlStdOperatorTable.LIKE),
    NOT_LIKE(SqlStdOperatorTable.NOT_LIKE),
    IS_NULL(SqlStdOperatorTable.IS_NULL),
    IS_NOT_NULL(SqlStdOperatorTable.IS_NOT_NULL),
    BETWEEN(SqlStdOperatorTable.BETWEEN),
    NOT_BETWEEN(SqlStdOperatorTable.NOT_BETWEEN),
    AND(SqlStdOperatorTable.AND),
    OR(SqlStdOperatorTable.OR),
    NOT(SqlStdOperatorTable.NOT),
    UNKNOWN(null);
    private final SqlOperator sqlOperator;

    FilterType(SqlOperator sqlOperator) {
        this.sqlOperator = sqlOperator;
    }

    public SqlOperator sqlOperator() {
        return sqlOperator;
    }
}