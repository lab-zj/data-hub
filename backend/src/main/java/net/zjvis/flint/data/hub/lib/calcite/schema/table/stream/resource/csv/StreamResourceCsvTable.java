package net.zjvis.flint.data.hub.lib.calcite.schema.table.stream.resource.csv;

import com.google.common.base.Preconditions;
import net.zjvis.flint.data.hub.lib.calcite.resource.StreamResource;
import org.apache.calcite.adapter.file.CsvEnumerator;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.util.Source;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class StreamResourceCsvTable extends AbstractTable implements Serializable {
    protected final StreamResource streamResource;
    protected final RelProtoDataType protoRowType;
    private RelDataType rowType;
    private List<RelDataType> fieldTypes;

    public StreamResourceCsvTable(StreamResource streamResource, @Nullable RelProtoDataType protoRowType) {
        this.streamResource = streamResource;
        this.protoRowType = protoRowType;
    }

    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        validateTypeFactory(typeFactory);
        if (null != protoRowType) {
            return protoRowType.apply(typeFactory);
        }
        if (null == rowType) {
            rowType = CsvEnumerator.deduceRowType(
                    (JavaTypeFactory) typeFactory,
                    extractSource(streamResource),
                    null,
                    isStream());
        }
        return rowType;
    }

    public List<RelDataType> getFieldTypes(RelDataTypeFactory typeFactory) {
        validateTypeFactory(typeFactory);
        if (fieldTypes == null) {
            fieldTypes = new ArrayList<>();
            CsvEnumerator.deduceRowType((JavaTypeFactory) typeFactory,
                    extractSource(streamResource),
                    fieldTypes,
                    isStream());
        }
        return fieldTypes;
    }

    protected boolean isStream() {
        return false;
    }

    protected void validateTypeFactory(RelDataTypeFactory typeFactory) {
        Preconditions.checkArgument(
                typeFactory instanceof JavaTypeFactory,
                "StreamResourceTable is only suitable for %s",
                JavaTypeFactory.class.getName());
    }

    protected Source extractSource(StreamResource streamResource) {
        return new Source() {
            @Override
            public InputStream openStream() throws IOException {
                return streamResource.openInputStream();
            }

            @Override
            public String protocol() {
                return "stream";
            }

            @Override
            public URL url() {
                throw unsupported(protocol());
            }

            @Override
            public File file() {
                throw unsupported(protocol());
            }

            @Override
            public String path() {
                return streamResource.resourceLocator();
            }

            @Override
            public Reader reader() throws IOException {
                return new InputStreamReader(openStream());
            }

            @Override
            public Source trim(String suffix) {
                throw unsupported(protocol());
            }

            @Override
            @Nullable
            public Source trimOrNull(String suffix) {
                throw unsupported(protocol());
            }

            @Override
            public Source append(Source child) {
                throw unsupported(protocol());
            }

            @Override
            public Source relative(Source source) {
                throw unsupported(protocol());
            }

            private UnsupportedOperationException unsupported(String protocol) {
                return new UnsupportedOperationException(
                        String.format(Locale.ROOT, "Invalid operation for '%s' protocol", protocol));
            }
        };
    }
}
