package net.zjvis.flint.data.hub.entity.filesystem.database;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import net.zjvis.flint.data.hub.controller.filesystem.vo.VirtualFileVO;
import net.zjvis.flint.data.hub.lib.calcite.connector.CalciteConnector;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;

@JsonTypeInfo(property = "type", use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MysqlVirtualFile.class, name = "mysql"),
        @JsonSubTypes.Type(value = PostgresqlVirtualFile.class, name = "postgresql"),
        @JsonSubTypes.Type(value = S3VirtualFile.class, name = "s3"),
})
public interface VirtualFile {
    default Pair<Boolean, String> connectionTest() {
        throw new NotImplementedException(
                String.format("connection test of virtualFile(%s) not support", this.getClass().getSimpleName()));
    }

    CalciteConnector toConnector(String identifier);

    default Object previewData() throws IOException {
        return previewData("inherit");
    }
    Object previewData(String type) throws IOException;
}
