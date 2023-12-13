package net.zjvis.flint.data.hub.lib.calcite.connector.csv;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.lib.calcite.datasource.CsvCalciteDataSource;
import net.zjvis.flint.data.hub.lib.calcite.resource.FileResource;
import net.zjvis.flint.data.hub.lib.calcite.resource.StreamResource;

import java.util.Map;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CsvFile extends AbstractCsv {
    @Builder
    @Jacksonized
    public CsvFile(
            String identifier,
            @Singular("namedFilePath")
            Map<String, String> namedFilePaths
    ) {
        super(
                identifier,
                namedFilePaths.entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> FileResource.builder()
                                        .path(entry.getValue())
                                        .build()
                        )));
    }

    public CsvCalciteDataSource asDataSource() {
        CsvCalciteDataSource csvCalciteDataSource = CsvCalciteDataSource.builder()
                .identifier(getIdentifier())
                .namedFilePaths(getNamedStreamResources()
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> {
                                    StreamResource streamResource = entry.getValue();
                                    Preconditions.checkArgument(streamResource instanceof FileResource);
                                    return ((FileResource) streamResource)
                                            .getFile()
                                            .getAbsolutePath();
                                }
                        )))
                .build();
        return csvCalciteDataSource;
    }
}
