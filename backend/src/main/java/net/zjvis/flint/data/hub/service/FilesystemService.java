package net.zjvis.flint.data.hub.service;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterators;
import lombok.Getter;
import net.zjvis.flint.data.hub.lib.data.Record;
import net.zjvis.flint.data.hub.lib.data.csv.CsvFileReader;
import net.zjvis.flint.data.hub.lib.minio.MinioException;
import net.zjvis.flint.data.hub.lib.minio.MinioFile;
import net.zjvis.flint.data.hub.lib.minio.MinioManager;
import net.zjvis.flint.data.hub.lib.minio.MinioTagKey;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static net.zjvis.flint.data.hub.util.AlgorithmServerConstant.ALGO_FILE_TAR;
import static net.zjvis.flint.data.hub.util.AlgorithmServerConstant.ALGO_FILE_VALUES;

@Service
public class FilesystemService {
    public static String objectPathIsDirectory(String objectPath) {
        return String.format("objectPath(%s) is a directory", objectPath);
    }

    public static String directoryAlreadyExists(String directoryPath) {
        return String.format("directory(%s) already exists", directoryPath);
    }

    public static String targetPathIsNotDirectory(String targetDirectoryPath) {
        return String.format("targetDirectoryPath(%s) is not a directory", targetDirectoryPath);
    }

    public static String targetDirectoryPathNotExists(String targetDirectoryPath) {
        return String.format("targetDirectoryPath(%s) not exists", targetDirectoryPath);
    }

    @Getter
    private final MinioManager minioManager;
    private final String bucketName;
    private final CsvFileReader csvFileReader;

    public FilesystemService(
            MinioManager minioManager,
            @Value("${application.s3.minio.bucket.filesystem}") String bucketName
    ) {
        this.minioManager = minioManager;
        this.bucketName = bucketName;
        this.csvFileReader = CsvFileReader.builder().build();
        try {
            minioManager.bucketCreate(bucketName, true);
        } catch (MinioException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void objectUpload(InputStream inputStream, String objectPath) throws MinioException {
        if (isDirectory(objectPath)) {
            throw new IllegalArgumentException(objectPathIsDirectory(objectPath));
        }
        minioManager.objectUpload(inputStream, bucketName, objectPath,
                Map.of(MinioTagKey.CREATE_TIME.getKey(), String.valueOf(System.currentTimeMillis())));
    }

    public Page<MinioFile> directoryList(String directoryPath, Pageable pageable) {
        int count = Iterators.size(
                minioManager.directoryList(bucketName, directoryPath)
                        .iterator());
        // NOTE: invoke `minioManager.directoryList(bucketName, directoryPath)` again to void iterator runs out
        List<MinioFile> minioFileList = StreamSupport.stream(
                        minioManager.directoryList(bucketName, directoryPath)
                                .spliterator(),
                        false
                ).skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .collect(Collectors.toList());
        return new PageImpl<>(minioFileList, pageable, count);
    }

    public Page<MinioFile> searchDirectoryList(
            String directoryPath,
            Pageable pageable,
            String keyword,
            String rootDirectory
    ) {
        String completedDirectoryPath = completeDirectoryPath(directoryPath);
        String completedRootDirectory = completeDirectoryPath(rootDirectory);
        List<MinioFile> minioFileList = StreamSupport.stream(
                        minioManager.objectList(bucketName, completedDirectoryPath, true)
                                .spliterator(),
                        false)
                .map(minioFile -> {
                    if (!minioFile.isDirectory()
                            && StringUtils.endsWith(minioFile.getObjectKey(), "/")
                            && 0 == minioFile.getSize()) {
                        return minioFile.toBuilder()
                                .isDirectory(true)
                                .build();
                    }
                    return minioFile;
                })
                .flatMap(minioFile -> extendMinioFile(minioFile).stream())
                .collect(Collectors.toSet())
                .stream()
                .filter(minioFile -> {
                    String path = minioFile.getObjectKey();
                    if (!StringUtils.startsWith(path, completedRootDirectory)
                            || StringUtils.equals(path, completedRootDirectory)) {
                        return false;
                    }
                    String filename = FilenameUtils.getName(
                            isDirectory(path)
                                    ? StringUtils.removeEnd(path, "/")
                                    : path);
                    return filename.contains(keyword);
                }).collect(Collectors.toList());
        int count = minioFileList.size();
        return new PageImpl<>(
                minioFileList.stream()
                        .skip(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .collect(Collectors.toList()),
                pageable,
                count
        );
    }

    public void directoryCreate(String directoryPath) throws MinioException {
        if (minioManager.directoryExists(bucketName, directoryPath)) {
            throw new MinioException(directoryAlreadyExists(directoryPath));
        }
        minioManager.directoryCreate(bucketName, directoryPath, true, true);
    }

    public void batchRemove(List<String> pathList, boolean recursive) throws MinioException {
        for (String path : pathList) {
            remove(path, recursive);
        }
    }

    public void move(String sourcePath, String targetPath, boolean override) throws MinioException {
        if (isDirectory(sourcePath)) {
            if (!isDirectory(targetPath)) {
                throw new MinioException(targetPathIsNotDirectory(targetPath));
            }
            minioManager.directoryCopy(bucketName, sourcePath, targetPath, override);
            minioManager.directoryRemove(bucketName, sourcePath, true, false);
        } else {
            if (isDirectory(targetPath)) {
                throw new MinioException(objectPathIsDirectory(targetPath));
            }
            minioManager.objectCopy(bucketName, sourcePath, targetPath, override);
            minioManager.objectRemove(bucketName, sourcePath, false);
        }
    }

    public void batchMoveToDirectory(
            List<String> sourcePathList,
            String targetDirectoryPath,
            boolean override
    ) throws MinioException {
        for (String sourcePath : sourcePathList) {
            moveToDirectory(sourcePath, targetDirectoryPath, override);
        }
    }

    public InputStream download(String path) throws IOException {
        if (isDirectory(path)) {
            File zipFile = minioManager.directoryDownloadAsZipFile(
                    bucketName, StringUtils.removeStart(path, "/"));
            return new FileInputStream(zipFile);
        } else {
            return minioManager.objectGet(bucketName, path);
        }
    }

    public InputStream batchDownload(List<String> path) throws IOException {
        File zipFile = minioManager.batchDownloadAsZipFile(bucketName, path);
        return new FileInputStream(zipFile);
    }

    public Map<String, String> getStatObject(String path) throws MinioException {
        return minioManager.objectStat(bucketName, path);
    }

    public boolean checkAlgorithmDirectory(String path) {
        Page<MinioFile> pagedMinioFiles = directoryList(path, PageRequest.of(0, 30));
        Optional<MinioFile> algorithmFileOptional = pagedMinioFiles.getContent()
                .stream()
                .filter(minioFile -> StringUtils.endsWith(minioFile.getObjectKey(), ALGO_FILE_TAR))
                .findAny();
        Optional<MinioFile> valuesFileOptional = pagedMinioFiles.getContent()
                .stream()
                .filter(minioFile -> minioFile.getObjectKey().contains(ALGO_FILE_VALUES))
                .findAny();
        return algorithmFileOptional.isPresent() && valuesFileOptional.isPresent();
    }

    public String previewText(String path, int size) throws IOException {
        try (InputStream inputStream = minioManager.objectGet(bucketName, path, size)) {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        }
    }

    public Page<Record> previewCsv(String path, Pageable pageable) throws MinioException {
        Iterable<Record> recordIterable = csvFileReader.read(minioManager.objectGet(bucketName, path));
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


    public boolean objectExists(String path) throws MinioException {
        return minioManager.objectExists(bucketName, path);
    }

    public boolean directoryExists(String path) throws MinioException {
        return minioManager.directoryExists(bucketName, path);
    }

    public static boolean isDirectory(String path) {
        return MinioManager.isDirectoryPath(path);
    }

    public static String completeDirectoryPath(String directoryPath) {
        return MinioManager.completeDirectoryPath(directoryPath);
    }

    private void moveToDirectory(String sourcePath, String targetDirectoryPath, boolean override) throws MinioException {
        if (!isDirectory(targetDirectoryPath)) {
            throw new MinioException(targetPathIsNotDirectory(targetDirectoryPath));
        }
        if (!directoryExists(targetDirectoryPath)) {
            throw new MinioException(targetDirectoryPathNotExists(targetDirectoryPath));
        }
        String sourcePathWithoutStartSlash = StringUtils.removeStart(sourcePath, "/");
        if (isDirectory(sourcePathWithoutStartSlash)) {
            String directoryName = FilenameUtils.getName(StringUtils.removeEnd(sourcePathWithoutStartSlash, "/"));
            String targetPath = String.format("%s/%s",
                    StringUtils.removeEnd(targetDirectoryPath, "/"), directoryName);
            minioManager.directoryCopy(
                    bucketName, sourcePathWithoutStartSlash, targetPath, override);
            minioManager.directoryRemove(
                    bucketName, sourcePathWithoutStartSlash, true, false);
        } else {
            String filename = FilenameUtils.getName(sourcePathWithoutStartSlash);
            String targetPath = String.format("%s/%s",
                    StringUtils.removeEnd(targetDirectoryPath, "/"), filename);
            minioManager.objectCopy(bucketName, sourcePathWithoutStartSlash, targetPath, override);
            minioManager.objectRemove(bucketName, sourcePathWithoutStartSlash, false);
        }
    }

    private void remove(String path, boolean recursive) throws MinioException {
        if (isDirectory(path)) {
            minioManager.directoryRemove(bucketName, path, recursive, true);
        } else {
            minioManager.objectRemove(bucketName, path, true);
        }
    }

    private List<MinioFile> extendMinioFile(MinioFile minioFile) {
        String objectKey = minioFile.getObjectKey();
        Splitter splitter = Splitter.on("/")
                .trimResults()
                .omitEmptyStrings();
        StringBuilder builder = new StringBuilder();
        String objectKeyWithoutSlash = StringUtils.removeEnd(objectKey, "/");
        return StreamSupport.stream(splitter.split(objectKey).spliterator(), false)
                .map(element -> {
                    if (StringUtils.isNotBlank(builder.toString())) {
                        builder.append("/");
                    }
                    builder.append(element);
                    return builder.toString();
                }).map(path -> {
                    if (StringUtils.equals(objectKeyWithoutSlash, path)) {
                        return minioFile;
                    }
                    return MinioFile.builder()
                            .size(0)
                            .isDirectory(true)
                            .objectKey(completeDirectoryPath(path))
                            .build();
                }).collect(Collectors.toList());
    }
}
