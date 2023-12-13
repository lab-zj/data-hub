package net.zjvis.flint.data.hub.lib.schedule.graph;

import java.io.Serializable;
import java.util.function.Supplier;

public interface SerializableSupplier<Result extends Vertex.SupplierResult> extends Supplier<Result>, Serializable {
}