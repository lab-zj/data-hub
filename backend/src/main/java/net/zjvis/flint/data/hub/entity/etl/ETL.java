package net.zjvis.flint.data.hub.entity.etl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import net.zjvis.flint.data.hub.entity.etl.udf.IUDFSampleFunction;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.tools.RelBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@JsonTypeInfo(property = "type", use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Filter.class, name = "Filter"),
//        @JsonSubTypes.Type(value = Pivot.class, name = "Pivot"),
//        @JsonSubTypes.Type(value = Join.class, name = "Join"),
//        @JsonSubTypes.Type(value = Union.class, name = "Union"),
//        @JsonSubTypes.Type(value = Sample.class, name = "Sample"),
//        @JsonSubTypes.Type(value = SQL.class, name = "SQL"),
//        @JsonSubTypes.Type(value = Aggr.class, name = "Aggr"),
//        @JsonSubTypes.Type(value = Minus.class, name = "Minus"),
})
public interface ETL {

    @JsonIgnore
    boolean isValid();

    default List<IUDFSampleFunction> getUDFs(){
        return new ArrayList<>();
    }

    Function<RelBuilder, RelNode> apply(List<Pair<String, String>> inputInfo);

//    default RelBuilder scanInput(List<Pair<String, String>> inputInfo ){
//        for (int i = 0; i < inputConnectors.size(); i++) {
//            Connector connector = inputConnectors.get(i);
//            if (connector instanceof CsvFile) {
//                queryManager.getRelBuilder().scan(queryManager.getSchemas().get(i).getKey(),
//                        FilenameUtils.removeExtension(Path.of(((CsvFile) connector).getFilePath()).getFileName().toString()));
//            } else if (connector instanceof Postgresql) {
//                queryManager.getRelBuilder().scan(queryManager.getSchemas().get(i).getKey(), ((Postgresql) connector).getReadTableName());
//            }
//            connector.exec(Connector.ACTION.INPUT, null);
//
//        }
//    }


}
