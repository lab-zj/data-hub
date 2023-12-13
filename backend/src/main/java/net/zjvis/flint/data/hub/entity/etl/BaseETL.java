package net.zjvis.flint.data.hub.entity.etl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.util.DataItem;
import net.zjvis.flint.data.hub.util.DataTypeEnum;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author AaronY
 * @version 1.0
 * @since 2023/10/23
 */
@Builder(builderMethodName = "baseETLBuilder")
@Getter
@Setter
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public class BaseETL {

    private Params params;


    @Builder(builderMethodName = "baseParamBuilder")
    @Getter
    @Setter
    @Jacksonized
    @EqualsAndHashCode
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Params {

        @Singular("input")
        private List<DataItem> inputs;
        @Singular("output")
        private List<DataItem> outputs;
    }

    /**
     * acceptable input type
     *
     * @return
     */
    public Set<DataTypeEnum> acceptableType(){
        return Set.of();
    }

    public Boolean isParamValid(){
        Set<DataTypeEnum> inputType = this.params.getInputs().stream().map(DataItem::getType).collect(Collectors.toSet());
        return acceptableType().containsAll(inputType);
    }

    public List<DataTypeEnum> outputType() {
        return this.params.getOutputs().stream().map(DataItem::getType).collect(Collectors.toList());
    }
}
