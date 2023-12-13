package net.zjvis.flint.data.hub;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.domain.Page;

import java.util.List;

@Builder
@Getter
@Jacksonized
@EqualsAndHashCode
@ToString
public class PagedData<DataType> {
    public static <T> PagedData<T> fromPage(Page<T> page) {
        return PagedData.<T>builder()
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .content(page.getContent())
                .build();
    }

    @Schema(description = "size of total elements")
    private long totalElements;
    @Schema(description = "size of total pages")
    private long totalPages;
    @Schema(description = "data list in current page")
    private List<DataType> content;
}
