package net.zjvis.flint.data.hub.service;

import net.zjvis.flint.data.hub.entity.filesystem.database.S3VirtualFile;
import net.zjvis.flint.data.hub.lib.data.Record;
import net.zjvis.flint.data.hub.lib.data.csv.CsvFileReader;
import net.zjvis.flint.data.hub.lib.minio.MinioException;
import net.zjvis.flint.data.hub.lib.minio.MinioManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class VirtualFileService {
    private final CsvFileReader csvFileReader;

    public VirtualFileService() {
        this.csvFileReader = CsvFileReader.builder().build();
    }

    public Map<String, String> getStatObject(S3VirtualFile s3VirtualFile) throws MinioException {
        return MinioManager.builder()
                .minioConnection(s3VirtualFile.getNamedStreamResources())
                .build()
                .objectStat(s3VirtualFile.getBucket(), s3VirtualFile.getObjectKey());
    }

    public Page<Record> previewCsv(S3VirtualFile s3VirtualFile, Pageable pageable) throws MinioException {
        Iterable<Record> recordIterable = csvFileReader.read(
                MinioManager.builder()
                        .minioConnection(s3VirtualFile.getNamedStreamResources())
                        .build()
                        .objectGet(s3VirtualFile.getBucket(), s3VirtualFile.getObjectKey()));
        List<Record> recordList = StreamSupport.stream(recordIterable.spliterator(), false)
                .collect(Collectors.toList());
        int count = recordList.size();
        return new PageImpl<>(
                recordList.stream()
                        .skip(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .collect(Collectors.toList()),
                pageable,
                count
        );
    }

    public InputStream download(S3VirtualFile s3VirtualFile) throws IOException {
        return MinioManager.builder()
                .minioConnection(s3VirtualFile.getNamedStreamResources())
                .build()
                .objectGet(s3VirtualFile.getBucket(), s3VirtualFile.getObjectKey());
    }
}
