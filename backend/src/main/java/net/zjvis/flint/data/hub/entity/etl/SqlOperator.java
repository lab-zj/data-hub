package net.zjvis.flint.data.hub.entity.etl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;

import java.io.Serializable;

/**
 * @author AaronY
 * @version 1.0
 * @since 2022/12/7
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown=true)
public enum SqlOperator implements Serializable {

    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL("<="),
    EQUALS("="),
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUAL(">="),
    NOT_EQUALS("<>"),
    IS_NOT_NULL("IS NOT NULL"),
    IS_NULL("IS NULL"),
    SIMILAR_TO("SIMILAR_TO")
    ;

    private String symbol;

    SqlOperator(String symbol) {
        this.symbol = symbol;
    }

    public org.apache.calcite.sql.SqlOperator toSqlStdOperator(){
        switch (this){
            case LESS_THAN:
                return SqlStdOperatorTable.LESS_THAN;
            case LESS_THAN_OR_EQUAL:
                return SqlStdOperatorTable.LESS_THAN_OR_EQUAL;
            case EQUALS:
                return SqlStdOperatorTable.EQUALS;
            case GREATER_THAN:
                return SqlStdOperatorTable.GREATER_THAN;
            case GREATER_THAN_OR_EQUAL:
                return SqlStdOperatorTable.GREATER_THAN_OR_EQUAL;
            case NOT_EQUALS:
                return SqlStdOperatorTable.NOT_EQUALS;
            case IS_NOT_NULL:
                return SqlStdOperatorTable.IS_NOT_NULL;
            case IS_NULL:
                return SqlStdOperatorTable.IS_NULL;
            case SIMILAR_TO:
                return SqlStdOperatorTable.SIMILAR_TO;
            default:
                throw new RuntimeException("cannot find this sql operator");
        }
    }

}
