package net.zjvis.flint.data.hub.lib.calcite.resource;

import lombok.Builder;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.function.Consumer;

public class CachedOutputStream extends OutputStream {
    private final ByteArrayOutputStream cache;
    private final FileOutputStream upgradeCache;
    private final Consumer<InputStream> closeCallback;

    @Builder
    public CachedOutputStream(Consumer<InputStream> closeCallback) throws IOException {
        this.closeCallback = closeCallback;
        // TODO lazy and combined together
        cache = new ByteArrayOutputStream();
        File cacheFile = File.createTempFile("temp-", ".cache");
        upgradeCache = new FileOutputStream(cacheFile);
    }

    public InputStream openCached() {
        // TODO switch
        return new ByteArrayInputStream(cache.toByteArray());
    }

    @Override
    public void write(int b) throws IOException {
        // TODO switch
        cache.write(b);
    }

    @Override
    public void write(@NotNull byte[] b) throws IOException {
        // TODO switch
        cache.write(b);
    }

    @Override
    public void write(@NotNull byte[] b, int off, int len) throws IOException {
        // TODO switch
        cache.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        // TODO switch
        cache.flush();
    }

    @Override
    public void close() throws IOException {
        closeCallback.accept(openCached());
        if (null != cache) {
            cache.close();
        }
        if (null != upgradeCache) {
            upgradeCache.close();
        }
    }
}