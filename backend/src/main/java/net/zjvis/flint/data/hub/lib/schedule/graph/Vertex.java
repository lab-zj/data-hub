package net.zjvis.flint.data.hub.lib.schedule.graph;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

@EqualsAndHashCode
@ToString
public class Vertex<Result extends Vertex.SupplierResult> implements Serializable {
    private static final long serialVersionUID = 5119769293485134641L;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Getter
    @ToString
    @EqualsAndHashCode
    public static class SupplierResult implements Serializable {
        private static final long serialVersionUID = 3337010712073051819L;
        private final boolean success;
        private final String message;
        private final Exception exception;

        public boolean failed() {
            return !success;
        }

        @Builder
        @Jacksonized
        public SupplierResult(Boolean success, String message, Exception exception) {
            this.success = null == success || success;
            this.message = message;
            this.exception = exception;
        }
    }

    @Getter
    private final String name;
    @Getter
    private final SerializableSupplier<Result> supplier;
    @Getter
    @Setter
    @EqualsAndHashCode.Exclude
    private transient CompletableFuture<Result> completableFuture;

    public Vertex(String name, SerializableSupplier<Result> supplier) {
        this.name = name;
        this.supplier = supplier;
    }
}
