package net.zjvis.flint.data.hub.entity.filesystem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Builder
@EqualsAndHashCode
@Jacksonized
@Getter
public class HttpResource implements VirtualResource {
    @Schema(example = "https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png")
    private String url;
}
