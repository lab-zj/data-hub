package net.zjvis.flint.data.hub.lib.calcite.datasource;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import org.apache.calcite.adapter.csv.CsvSchema;
import org.apache.calcite.adapter.csv.CsvTable;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;

import java.nio.file.Paths;
import java.util.Map;

@Getter
@EqualsAndHashCode
@ToString
public class CsvCalciteDataSource implements CalciteDataSource {
    private final String identifier;
    private final Map<String, String> namedFilePaths;

    @Builder
    @Jacksonized
    public CsvCalciteDataSource(
            String identifier,
            @Singular("namedFilePath")
            Map<String, String> namedFilePaths
    ) {
        this.identifier = identifier;
        this.namedFilePaths = namedFilePaths;
    }

    @Override
    public String identifier() {
        return identifier;
    }

    @Override
    public Schema calciteSchema(SchemaPlus parentSchema, String identifier) {
        String tempPath = namedFilePaths.values().stream().findFirst().get();
        return new CsvSchema(Paths.get(tempPath).getParent().toFile(), CsvTable.Flavor.SCANNABLE);
    }
}
