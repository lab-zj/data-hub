package net.zjvis.flint.data.hub.entity.etl.udf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.type.ReturnTypes;
import org.apache.calcite.sql.type.SqlReturnTypeInference;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * @author AaronY
 * @version 1.0
 * @since 2022/12/16
 */
@JsonTypeInfo(property = "identifier", use = JsonTypeInfo.Id.MINIMAL_CLASS)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MySampleFunction.class),
})
@NoArgsConstructor
public abstract class IUDFSampleFunction<I, O> implements Serializable {

    private static final String METHOD_NAME = "eval";

    @Getter
    private String inputName;

    @Getter
    private Integer sortType;

    protected IUDFSampleFunction(String columnName) {
        this.inputName = columnName;
        this.sortType = 1;
    }

    protected IUDFSampleFunction(String columnName, Integer sortType) {
        this.inputName = columnName;
        Preconditions.checkState(sortType == 1 || sortType == -1, "please specify sortType = 1 (asc) or -1 (desc).");
        this.sortType = sortType;
    }

    public abstract Class<I> getInputType();

    public abstract Class<O> getReturnType();

    public abstract O eval(I columnValue);

    public String getMethodName() {
        return IUDFSampleFunction.METHOD_NAME;
    }

    @JsonIgnore
    public SqlReturnTypeInference getRelReturnType(){
        Class<O> returnType = this.getReturnType();
        if (Integer.class.equals(returnType)) {
            return ReturnTypes.INTEGER;
        } else if (Boolean.class.equals(returnType)) {
            return ReturnTypes.BOOLEAN;
        }else  if(Date.class.equals(returnType)){
            return ReturnTypes.DATE;
        }else if (Timestamp.class.equals(returnType)){
            return ReturnTypes.TIMESTAMP;
        }else if (Double.class.equals(returnType)){
            return ReturnTypes.DOUBLE;
        }
        throw new RuntimeException("cannot find this return type");
    }


}
