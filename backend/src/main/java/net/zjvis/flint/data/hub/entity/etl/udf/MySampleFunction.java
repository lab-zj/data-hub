package net.zjvis.flint.data.hub.entity.etl.udf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

/**
 * @author AaronY
 * @version 1.0
 * @since 2022/12/16
 */
@Getter
@Setter
@Jacksonized
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MySampleFunction extends IUDFSampleFunction<String, Integer> {

    public MySampleFunction(String columnName) {
        super(columnName);
    }

    public MySampleFunction(String columnName, Integer sortType){
        super(columnName, sortType);
    }

    @Override
    public Class<String> getInputType() {
        return String.class;
    }

    @Override
    public Class<Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    public Integer eval(String columnValue) {
        return columnValue.chars().sum();
    }

}
