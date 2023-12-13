package net.zjvis.flint.data.hub.entity.filesystem.database;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.controller.filesystem.SizedInputStreamResource;
import net.zjvis.flint.data.hub.controller.filesystem.vo.TableVO;
import net.zjvis.flint.data.hub.lib.calcite.connector.CalciteConnector;
import net.zjvis.flint.data.hub.lib.calcite.connector.csv.CsvS3;
import net.zjvis.flint.data.hub.lib.calcite.resource.S3Resource;
import net.zjvis.flint.data.hub.lib.data.Record;
import net.zjvis.flint.data.hub.lib.data.csv.CsvFileReader;
import net.zjvis.flint.data.hub.lib.minio.MinioConnection;
import net.zjvis.flint.data.hub.lib.minio.MinioManager;
import net.zjvis.flint.data.hub.lib.minio.MinioTagKey;
import net.zjvis.flint.data.hub.util.DataTypeEnum;
import net.zjvis.flint.data.hub.util.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Builder
@Getter
@Jacksonized
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class S3VirtualFile implements VirtualFile {
    private String name;
    private MinioConnection namedStreamResources;
    private String bucket;
    private String objectKey;
    private static CsvFileReader csvFileReader = CsvFileReader.builder().build();

    @Override
    public CalciteConnector toConnector(String identifier) {
        return CsvS3.builder()
                .identifier(identifier)
                .namedObject(
                        this.getName(),
                        S3Resource.builder()
                                .minioConnection(
                                        this.getNamedStreamResources())
                                .bucket(this.getBucket())
                                .objectKey(this.getObjectKey())
                                .build())
                .build();
    }

    @Override
    public Object previewData(String type) throws IOException {
        MinioManager minioManager = MinioManager.builder()
                .minioConnection(namedStreamResources)
                .build();
        if (type.equalsIgnoreCase(DataTypeEnum.INHERIT.getDesc())){
            type = FilenameUtils.getExtension(this.getObjectKey());
        }
        InputStream inputStream = null;
        if (MinioManager.isDirectoryPath(this.getObjectKey())) {
            File zipFile = minioManager.directoryDownloadAsZipFile(this.getBucket(), StringUtils.removeStart(this.getObjectKey(), "/"));
            inputStream = new FileInputStream(zipFile);
        } else {
            inputStream = minioManager.objectGet(this.getBucket(), this.getObjectKey());
        }
        switch (type){
            case "png":
            case "jpeg":
            case "gif":
            case "jpg":
                return inputStream;
            case "csv":
                Iterable<Record> recordIterable = csvFileReader.read(inputStream);
                List<Record> recordList = StreamSupport.stream(recordIterable.spliterator(), false)
                        .collect(Collectors.toList());
                PageImpl<Record> recordPage = new PageImpl<>(
                        recordList.stream()
                                .skip(0)
                                .limit(100)
                                .collect(Collectors.toList()),
                        PageRequest.of(0, 100),
                        recordList.size()
                );
                return TableVO.wrapFromPage(recordPage);
            case "txt":
                return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            default:
                throw new RuntimeException("Not Supported yet.");
        }
    }

}

