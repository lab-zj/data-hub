package net.zjvis.flint.data.hub;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

@Builder
@Getter
@AllArgsConstructor
public class StandardResponse<DataType> implements Serializable {
    private static final long serialVersionUID = -5740273676686389382L;
    @Builder.Default
    @Schema(description = "code to indicate state", example = "200")
    private long code = HttpStatus.OK.value();
    @Schema(description = "data to response, may be null/empty if not need")
    private DataType data;
    @Schema(description = "exception message, may be null/empty if not need", example = "null/empty or error message")
    private String message;
}
