package net.zjvis.flint.data.hub.util;

import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.dialect.CalciteSqlDialect;

/**
 * @author AaronY
 * @version 1.0
 * @since 2023/10/23
 */
public class RelUtils {

    public static String relToSql(RelNode relNode){
        return relToSql(CalciteSqlDialect.DEFAULT, relNode);
    }

    public static String relToSql(SqlDialect sqlDialect, RelNode relNode){
        SqlNode statement = new RelToSqlConverter(sqlDialect).visitRoot(relNode).asStatement();
        return statement.toString();
    }
}
