package net.zjvis.flint.data.hub.lib.schedule.scheduler;

import net.zjvis.flint.data.hub.lib.schedule.graph.Graph;
import net.zjvis.flint.data.hub.lib.schedule.graph.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

public interface Scheduler {
    Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);

    default void schedule(Graph<Vertex.SupplierResult> graph) {
        Iterator<Vertex<Vertex.SupplierResult>> topologicalIterator = graph.topologicalIterator();
        while (topologicalIterator.hasNext()) {
            Vertex<Vertex.SupplierResult> vertex = topologicalIterator.next();
            try {
                vertex.setCompletableFuture(
                        CompletableFuture.completedFuture(
                                vertex.getSupplier().get()));
            } catch (Exception e) {
                handleException(graph, vertex, e);
            }
        }
    }

    default void handleException(
            Graph<Vertex.SupplierResult> graph,
            Vertex<Vertex.SupplierResult> vertex,
            Exception exception
    ) {
        LOGGER.warn(String.format(
                        "run supplier(%s) from vertex(%s) in graph(%s) failed: %s",
                        vertex.getSupplier(), vertex, graph, exception.getMessage()
                ),
                exception
        );
    }

    default boolean finished(Graph<Vertex.SupplierResult> graph) {
        return graph.vertexSet()
                .stream()
                .anyMatch(vertex -> {
                    CompletableFuture<Vertex.SupplierResult> completableFuture = vertex.getCompletableFuture();
                    return null == completableFuture || completableFuture.isDone();
                });
    }
}
