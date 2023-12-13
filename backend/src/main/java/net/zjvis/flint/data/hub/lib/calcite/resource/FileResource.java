package net.zjvis.flint.data.hub.lib.calcite.resource;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.io.*;

@EqualsAndHashCode
@ToString
public class FileResource implements StreamResource {
    // TODO File file
    @Getter
    private final File file;

    @Builder
    @Jacksonized
    public FileResource(String path) {
        this.file = new File(path);
    }

    @Override
    public InputStream openInputStream() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public OutputStream openOutputStream(boolean append) {
        try {
            return new FileOutputStream(file, append);
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String resourceLocator() {
        return file.getAbsolutePath();
    }
}
