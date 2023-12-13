package net.zjvis.flint.data.hub.util;

import lombok.*;

/**
 * @author AaronY
 * @version 1.0
 * @since 2023/10/23
 */
@Data
@EqualsAndHashCode
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataItem {

    private DataTypeEnum type;
    private String name;

    @Builder(builderMethodName = "twoStringBuilder")
    public DataItem(String type, String name){
        this.type = DataTypeEnum.valueOf(type.toUpperCase());
        this.name = name;
    }
}
