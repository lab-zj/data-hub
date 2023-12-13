package net.zjvis.flint.data.hub.controller.filesystem.vo;

import com.google.common.base.Preconditions;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.PagedData;
import net.zjvis.flint.data.hub.lib.data.Record;
import net.zjvis.flint.data.hub.lib.data.Schema;
import net.zjvis.flint.data.hub.lib.data.Type;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
@ToString
@EqualsAndHashCode
public class TableVO {
    private final List<ColumnVO> columnVOList;
    private final PagedData<List<Object>> valueList;

    @Builder
    @Jacksonized
    public TableVO(@Singular("columnVO") List<ColumnVO> columnVOList, PagedData<List<Object>> valueList) {
        this.columnVOList = columnVOList;
        this.valueList = valueList;
    }

    public static TableVO wrapFromPage(Page<Record> page) {
        List<Record> recordList = page.getContent();
        if (recordList.isEmpty()) {
            return TableVO.builder().build();
        } else {
            Schema dataSchema = recordList.get(0).getSchema();
            List<ColumnVO> columnVOList = IntStream.range(0, dataSchema.columnSize())
                    .mapToObj(index -> ColumnVO.builder()
                            .name(dataSchema.name(index))
                            .type(dataSchema.type(index))
                            .build())
                    .collect(Collectors.toList());
            List<List<Object>> valueList = recordList.stream()
                    .map(record -> IntStream.range(0, dataSchema.columnSize())
                            .mapToObj(index -> {
                                Type type = dataSchema.type(index);
                                Object value = record.value(index);
                                if (Type.DATE_TIME.equals(type)) {
                                    Preconditions.checkArgument(
                                            value instanceof LocalDateTime,
                                            "value, whose type is Type.DateTime, is not an instance of LocalDateTime");
                                    return ((LocalDateTime) value).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                                }
                                return value;
                            }).collect(Collectors.toList()))
                    .collect(Collectors.toList());
            return TableVO.builder()
                    .columnVOList(columnVOList)
                    .valueList(PagedData.<List<Object>>builder()
                            .content(valueList)
                            .totalPages(page.getTotalPages())
                            .totalElements(page.getTotalElements())
                            .build())
                    .build();
        }

    }
}
