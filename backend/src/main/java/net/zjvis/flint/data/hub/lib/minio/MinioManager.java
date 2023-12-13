package net.zjvis.flint.data.hub.lib.minio;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import io.minio.messages.Tags;
import lombok.Builder;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MinioManager {
    public static final Logger LOGGER = LoggerFactory.getLogger(MinioManager.class);
    private final MinioClient minioClient;

    public static String bucketAlreadyExistsMessage(String bucketName) {
        return String.format("bucket(%s) already exists", bucketName);
    }

    public static String bucketNotExistsMessage(String bucketName) {
        return String.format("bucket(%s) not exists", bucketName);
    }

    public static String parentPathNotExists(String parentPath) {
        return String.format("parentPath(%s) not exists", parentPath);
    }

    public static String objectAlreadyExists(String objectKey) {
        return String.format("object(%s) already exists", objectKey);
    }

    public static String objectNotExists(String objectKey) {
        return String.format("object(%s) not exists", objectKey);
    }

    public static String directoryPathNotExists(String directoryPath) {
        return String.format("directoryPath(%s) not exists", completeDirectoryPath(directoryPath));
    }

    public static String directoryPathAlreadyExists(String directoryPath) {
        return String.format("directoryPath(%s) already exists", completeDirectoryPath(directoryPath));
    }

    public static String directoryIsNotEmpty(String directoryPath) {
        return String.format("directoryPath is not empty(%s)", completeDirectoryPath(directoryPath));
    }

    public static boolean isDirectoryPath(String path) {
        return path.endsWith("/");
    }

    public static String completeDirectoryPath(String directoryPath) {
        return StringUtils.endsWith(directoryPath, "/") ? directoryPath : String.format("%s/", directoryPath);
    }

    @Builder
    public MinioManager(MinioConnection minioConnection) {
        String endpoint = minioConnection.getEndpoint();
        String accessKey = minioConnection.getAccessKey();
        String accessSecret = minioConnection.getAccessSecret();
        Preconditions.checkArgument(
                StringUtils.isNotBlank(endpoint), "endpoint(%s) cannot be blank", endpoint);
        Preconditions.checkArgument(
                StringUtils.isNotBlank(endpoint), "accessKey(%s) cannot be blank", endpoint);
        Preconditions.checkArgument(
                StringUtils.isNotBlank(endpoint), "accessSecret(%s) cannot be blank", endpoint);
        minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, accessSecret)
                .build();
    }

    public boolean bucketExists(String bucketName) throws MinioException {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (ErrorResponseException
                 | InternalException
                 | InsufficientDataException
                 | InvalidKeyException
                 | InvalidResponseException
                 | IOException
                 | NoSuchAlgorithmException
                 | ServerException
                 | XmlParserException e) {
            throw new MinioException(e);
        }
    }

    public void bucketCreate(String bucketName, boolean ignoreExists) throws MinioException {
        if (bucketExists(bucketName)) {
            if (ignoreExists) {
                LOGGER.debug("bucket({}) already exists: skip creating", bucketName);
                return;
            }
            throw new MinioException(bucketAlreadyExistsMessage(bucketName));
        }
        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (ErrorResponseException
                 | InsufficientDataException
                 | InternalException
                 | InvalidKeyException
                 | InvalidResponseException
                 | IOException
                 | NoSuchAlgorithmException
                 | ServerException
                 | XmlParserException e) {
            throw new MinioException(e);
        }
    }

    public void bucketRemove(String bucketName, boolean ignoreNotExists) throws MinioException {
        if (!bucketExists(bucketName)) {
            if (ignoreNotExists) {
                LOGGER.debug("bucket({}) not exists: skip removing", bucketName);
                return;
            }
            throw new MinioException(bucketNotExistsMessage(bucketName));
        }
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (ErrorResponseException
                 | InsufficientDataException
                 | InternalException
                 | InvalidKeyException
                 | InvalidResponseException
                 | IOException
                 | NoSuchAlgorithmException
                 | ServerException
                 | XmlParserException e) {
            throw new MinioException(e);
        }
    }

    /**
     * @param bucketName name of bucket
     * @param prefix     prefix for searching. NOTE: prefix should end with "/" if searching directory
     * @param recursive  WARN: recursive is not compatible with delimiter, therefore no virtual directory will be listed
     *                   WARN: recursive will not return virtual directories
     *                   <a href="https://github.com/minio/minio/issues/16663">issue at github</a>
     * @return files (with directories if recursive is false)
     */
    public Iterable<MinioFile> objectList(String bucketName, String prefix, boolean recursive) {
        Iterable<Result<Item>> itemResultIterable = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .startAfter(prefix)
                .recursive(recursive)
                .prefix(prefix)
                .build());
        Iterator<Result<Item>> itemResultIterator = itemResultIterable.iterator();
        return () -> new Iterator<>() {
            @Override
            public boolean hasNext() {
                return itemResultIterator.hasNext();
            }

            @Override
            public MinioFile next() {
                try {
                    Item item = itemResultIterator
                            .next()
                            .get();
                    if (null == item) {
                        return null;
                    }
                    return MinioFile.builder()
                            .objectKey(item.objectName())
                            .isDirectory(item.isDir())
                            .size(item.size())
                            .build();
                } catch (ErrorResponseException
                         | InsufficientDataException
                         | InternalException
                         | InvalidKeyException
                         | InvalidResponseException
                         | IOException
                         | NoSuchAlgorithmException
                         | ServerException
                         | XmlParserException e) {
                    throw new UncheckedIOException(new MinioException(e));
                }
            }
        };
    }

    public void objectUpload(
            InputStream inputStream,
            String bucketName,
            String objectKey,
            Map<String, String> tags
    ) throws MinioException {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .tags(tags)
                    .bucket(bucketName)
                    .object(objectKey)
                    .stream(inputStream, -1, 10485760)
                    .build());
        } catch (ServerException
                 | InsufficientDataException
                 | ErrorResponseException
                 | IOException
                 | NoSuchAlgorithmException
                 | InvalidKeyException
                 | InvalidResponseException
                 | XmlParserException
                 | InternalException e) {
            throw new MinioException(e);
        }
    }

    public void objectUpload(InputStream inputStream, String bucketName, String objectKey)
            throws MinioException {
        objectUpload(inputStream, bucketName, objectKey, Map.of());
    }

    public Map<String, String> objectStat(String bucketName, String objectKey) throws MinioException {
        try {
            StatObjectResponse statObjectResponse = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build());
            Tags objectTags = minioClient.getObjectTags(GetObjectTagsArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build());
            return ImmutableMap.<String, String>builder()
                    .put(MinioTagKey.LAST_MODIFIED.getKey(),
                            String.valueOf(Long.valueOf(statObjectResponse.lastModified().toInstant().toEpochMilli())))
                    .put(MinioTagKey.SIZE.getKey(), String.valueOf(Long.valueOf(statObjectResponse.size())))
                    .putAll(objectTags.get())
                    .build();
        } catch (InsufficientDataException
                 | InternalException
                 | InvalidKeyException
                 | InvalidResponseException
                 | IOException
                 | NoSuchAlgorithmException
                 | ServerException
                 | XmlParserException
                 | ErrorResponseException
                e) {
            throw new MinioException(e);
        }
    }

    public boolean objectExists(String bucketName, String objectKey) throws MinioException {
        Preconditions.checkNotNull(bucketName, "bucketName is null");
        Preconditions.checkNotNull(objectKey, "objectKey is null");
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build());
            return true;
        } catch (InsufficientDataException
                 | InternalException
                 | InvalidKeyException
                 | InvalidResponseException
                 | IOException
                 | NoSuchAlgorithmException
                 | ServerException
                 | XmlParserException e) {
            throw new MinioException(e);
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw new MinioException(e);
        }
    }

    public void objectRemove(String bucketName, String objectKey, boolean ignoreNotExists) throws MinioException {
        if (!ignoreNotExists && !objectExists(bucketName, objectKey)) {
            throw new MinioException(objectNotExists(objectKey));
        }
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build());
        } catch (ErrorResponseException
                 | InsufficientDataException
                 | InternalException
                 | InvalidKeyException
                 | InvalidResponseException
                 | IOException
                 | NoSuchAlgorithmException
                 | ServerException
                 | XmlParserException e) {
            throw new MinioException(e);
        }
    }

    public InputStream objectGet(String bucketName, String objectKey) throws MinioException {
        if (!objectExists(bucketName, objectKey)) {
            throw new MinioException(objectNotExists(objectKey));
        }
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build());
        } catch (ErrorResponseException
                 | InsufficientDataException
                 | InternalException
                 | InvalidKeyException
                 | InvalidResponseException
                 | IOException
                 | NoSuchAlgorithmException
                 | ServerException
                 | XmlParserException e) {
            throw new MinioException(e);
        }
    }

    public InputStream objectGet(String bucketName, String objectKey, long limit) throws MinioException {
        return new BoundedInputStream(
                objectGet(bucketName, objectKey),
                limit);
    }

    public void objectCopy(
            String bucketName,
            String sourceObjectKey,
            String targetObjectKey,
            boolean override
    ) throws MinioException {
        if (!objectExists(bucketName, sourceObjectKey)) {
            throw new MinioException(objectNotExists(sourceObjectKey));
        }
        if (objectExists(bucketName, targetObjectKey) && !override) {
            throw new MinioException(objectAlreadyExists(targetObjectKey));
        }
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .source(CopySource.builder()
                            .bucket(bucketName)
                            .object(sourceObjectKey)
                            .build())
                    .bucket(bucketName)
                    .object(targetObjectKey)
                    .build());
        } catch (ErrorResponseException
                 | InsufficientDataException
                 | InternalException
                 | InvalidKeyException
                 | InvalidResponseException
                 | IOException
                 | NoSuchAlgorithmException
                 | ServerException
                 | XmlParserException e) {
            throw new MinioException(e);
        }
    }

    public Iterable<MinioFile> directoryList(String bucketName, String directoryPath) {
        return objectList(bucketName, completeDirectoryPath(directoryPath), false);
    }

    public void directoryCreate(
            String bucketName,
            String directoryPath,
            boolean createParent,
            boolean ignoreExists
    ) throws MinioException {
        String completeDirectoryPath = completeDirectoryPath(directoryPath);
        if (!createParent) {
            Optional<String> optional = parentPathOptional(completeDirectoryPath);
            if (optional.isPresent()) {
                String parentPath = optional.get();
                if (!directoryExists(bucketName, parentPath)) {
                    throw new MinioException(parentPathNotExists(parentPath));
                }
            }
        }
        if (directoryExists(bucketName, completeDirectoryPath)) {
            if (ignoreExists) {
                return;
            }
            throw new MinioException(objectAlreadyExists(completeDirectoryPath));
        }
        objectUpload(
                new ByteArrayInputStream(new byte[]{}),
                bucketName,
                completeDirectoryPath
        );
    }

    public void directoryRemove(
            String bucketName,
            String directoryPath,
            boolean recursive,
            boolean ignoreNotExists
    ) throws MinioException {
        String completeDirectoryPath = completeDirectoryPath(directoryPath);
        if (!ignoreNotExists && !directoryExists(bucketName, completeDirectoryPath)) {
            throw new MinioException(directoryPathNotExists(completeDirectoryPath));
        }
        if (!recursive) {
            if (directoryIsEmpty(bucketName, completeDirectoryPath)) {
                throw new MinioException(String.format("directoryPath is not empty(%s)", completeDirectoryPath));
            }
            objectRemove(bucketName, completeDirectoryPath, true);
            return;
        }
        List<MinioFile> minioFileList = StreamSupport.stream(
                objectList(bucketName, completeDirectoryPath, true).spliterator(),
                false
        ).collect(Collectors.toList());
        Iterable<Result<DeleteError>> deleteErrors = minioClient.removeObjects(RemoveObjectsArgs.builder()
                .bucket(bucketName)
                .objects(Stream.concat(
                                minioFileList.stream()
                                        .map(MinioFile::getObjectKey),
                                Stream.of(completeDirectoryPath)
                        ).map(DeleteObject::new)
                        .collect(Collectors.toList()))
                .build());
        List<DeleteError> deleteErrorList = StreamSupport.stream(deleteErrors.spliterator(), false)
                .map(deleteErrorResult -> {
                    try {
                        return deleteErrorResult.get();
                    } catch (ErrorResponseException
                             | InsufficientDataException
                             | InternalException
                             | InvalidKeyException
                             | InvalidResponseException
                             | IOException
                             | NoSuchAlgorithmException
                             | ServerException
                             | XmlParserException e) {
                        throw new UncheckedIOException(new MinioException(e));
                    }
                })
                .collect(Collectors.toList());
        if (!deleteErrorList.isEmpty()) {
            throw new MinioException(String.format("delete object errors: %s", deleteErrorList));
        }
    }

    public boolean directoryExists(String bucketName, String directoryPath) throws MinioException {
        String completeDirectoryPath = completeDirectoryPath(directoryPath);
        if (objectExists(bucketName, completeDirectoryPath)) {
            return true;
        }
        return objectList(bucketName, completeDirectoryPath, true)
                .iterator()
                .hasNext();
    }

    public void directoryCopy(
            String bucketName,
            String sourceDirectoryPath,
            String targetDirectoryPath,
            boolean override
    ) throws MinioException {
        String source = completeDirectoryPath(sourceDirectoryPath);
        String target = completeDirectoryPath(targetDirectoryPath);
        if (!directoryExists(bucketName, source)) {
            throw new MinioException(directoryPathNotExists(source));
        }
        if (!override && directoryExists(bucketName, target)) {
            throw new MinioException(directoryPathAlreadyExists(target));
        }
        if (objectExists(bucketName, source)) {
            // source directory itself should be copied
            objectCopy(bucketName, source, target, override);
        }
        Iterable<MinioFile> minioFiles = objectList(bucketName, source, true);
        List<MinioFile> minioFileList = StreamSupport.stream(minioFiles.spliterator(), false)
                .collect(Collectors.toList());
        for (MinioFile minioFile : minioFileList) {
            String sourceObjectKey = minioFile.getObjectKey();
            String suffix = StringUtils.removeStart(sourceObjectKey, source)
                    .replaceAll("//+$", "/")
                    .replaceAll("^/+", "");
            String targetObjectKey = String.format("%s/%s", target.replaceAll("/+$", ""), suffix);
            objectCopy(bucketName, minioFile.getObjectKey(), targetObjectKey, override);
        }
    }

    public File directoryDownloadAsZipFile(String bucketName, String directoryPath) throws IOException {
        return batchDownloadAsZipFile(bucketName, Collections.singletonList(directoryPath));
    }

    public File batchDownloadAsZipFile(String bucketName, List<String> directoryPathList) throws IOException {
        File tempFile = File.createTempFile("temp-", ".zip");
        if (tempFile.exists()) {
            FileUtils.deleteQuietly(tempFile);
        }
        try (ZipFile zipFile = new ZipFile(tempFile)) {
            for (String directoryPath : directoryPathList) {
                directoryPath = StringUtils.removeStart(directoryPath, "/");
                if (isDirectoryPath(directoryPath)) {
                    if (!directoryExists(bucketName, directoryPath)) {
                        throw new MinioException(directoryPathNotExists(directoryPath));
                    }
                    Iterable<MinioFile> minioFiles = objectList(bucketName, directoryPath, true);
                    List<MinioFile> minioFileList = StreamSupport.stream(minioFiles.spliterator(), false)
                            .collect(Collectors.toList());
                    String newPath = StringUtils.removeEnd(directoryPath, "/");
                    if (minioFileList.isEmpty()) {
                        ZipParameters zipParameters = new ZipParameters();
                        zipParameters.setFileNameInZip("/");
                        zipFile.addStream(new ByteArrayInputStream(new byte[]{}), zipParameters);
                    }
                    for (MinioFile minioFile : minioFileList) {
                        String objectKey = minioFile.getObjectKey();
                        ZipParameters zipParameters = new ZipParameters();
                        zipParameters.setFileNameInZip(StringUtils.removeStart(objectKey, newPath.substring(0, newPath.lastIndexOf("/") + 1)));
                        try (InputStream inputStream = objectGet(bucketName, objectKey)) {
                            zipFile.addStream(inputStream, zipParameters);
                        }
                    }
                } else {
                    if (!objectExists(bucketName, directoryPath)) {
                        throw new MinioException(directoryPathNotExists(directoryPath));
                    }
                    ZipParameters zipParameters = new ZipParameters();
                    zipParameters.setFileNameInZip(directoryPath.substring(directoryPath.lastIndexOf("/") + 1));
                    try (InputStream inputStream = objectGet(bucketName, directoryPath)) {
                        zipFile.addStream(inputStream, zipParameters);
                    }
                }
            }
        }
        return tempFile;
    }

    private Optional<String> parentPathOptional(String path) {
        Preconditions.checkArgument(StringUtils.isNotBlank(path), "path cannot be blank");
        String pathWithLastSlashPartRemoved = path.endsWith("/")
                ? path.substring(0, path.length() - 1)
                : path;
        int lastIndexOfSlash = pathWithLastSlashPartRemoved.lastIndexOf("/");
        if (-1 == lastIndexOfSlash) {
            return Optional.empty();
        }
        return Optional.of(path.substring(0, lastIndexOfSlash));
    }

    private boolean directoryIsEmpty(String bucketName, String completeDirectoryPath) {
        Iterable<MinioFile> minioFiles = objectList(bucketName, completeDirectoryPath, true);
        return minioFiles.iterator()
                .hasNext();
    }
}
