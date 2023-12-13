package net.zjvis.flint.data.hub.lib.data;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author AaronY
 * @version 1.0
 * @since 2023/10/23
 */
@Builder
@Getter
@Jacksonized
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class LogicalTable {

    private Schema schema;
    private List<Record> recordList;

    public String getRandomColName(){
        return schema.name(ThreadLocalRandom.current().nextInt(0, schema.columnSize()));
    }

    public Object getRandomColValue(String colName) {
        Record record = recordList.get(ThreadLocalRandom.current().nextInt(1, 8));
        int index = schema.getNameList().indexOf(colName);
        return  record.getValueList().get(index);
    }
}

