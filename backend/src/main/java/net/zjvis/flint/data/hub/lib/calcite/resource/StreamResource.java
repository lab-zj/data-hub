package net.zjvis.flint.data.hub.lib.calcite.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface StreamResource {
    InputStream openInputStream() throws IOException;

    OutputStream openOutputStream(boolean append) throws IOException;

    String resourceLocator();
}
