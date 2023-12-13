package net.zjvis.flint.data.hub.util;

import java.io.File;
import net.zjvis.flint.data.hub.lib.minio.MinioManager;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;


public class FileUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        FileUtils.class);
    private static List<String> fileTypeList = Arrays.asList("csv", "txt", "png", "mp4","my", "ps", "s3");
    private static String FILE_TYPE_UNKNOWN = "unknown";

    public static String getFileType(String filePath) {
        String extension = FilenameUtils.getExtension(filePath);
        if (StringUtils.isEmpty(extension)) {
            return FILE_TYPE_UNKNOWN;
        }
        if (fileTypeList.contains(extension.toLowerCase())) {
            return extension.toLowerCase();
        } else {
            return FILE_TYPE_UNKNOWN;
        }
    }

    public static String getFileContent(String fileName) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(fileName);
        InputStream inputStream = classPathResource.getInputStream();
        LOGGER.info("[getFileContent] class path={}", classPathResource);
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
    }

    public static String getMinioFileContent(MinioManager minioManager, String bucketName, String inputObjectKey) throws IOException {
        File targetFile = File.createTempFile("tempFile", "txt");
        org.apache.commons.io.FileUtils.copyInputStreamToFile(minioManager.objectGet(bucketName, inputObjectKey), targetFile);
        return org.apache.commons.io.FileUtils.readFileToString(targetFile, StandardCharsets.UTF_8);
    }

}
