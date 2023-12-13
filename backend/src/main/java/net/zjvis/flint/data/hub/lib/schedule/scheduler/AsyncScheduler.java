package net.zjvis.flint.data.hub.lib.schedule.scheduler;

import com.google.common.base.Preconditions;
import lombok.Builder;
import net.zjvis.flint.data.hub.lib.schedule.exception.SourceFailedException;
import net.zjvis.flint.data.hub.lib.schedule.graph.Graph;
import net.zjvis.flint.data.hub.lib.schedule.graph.Vertex;
import net.zjvis.flint.data.hub.lib.schedule.graph.Vertex.SupplierResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Builder(toBuilder = true)
public class AsyncScheduler implements Scheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncScheduler.class);

    @Builder.Default
    private Executor executor = Executors.newSingleThreadExecutor();

    @Override
    public void schedule(Graph<SupplierResult> graph) {
        Iterator<Vertex<SupplierResult>> topologicalIterator = graph.topologicalIterator();
        while (topologicalIterator.hasNext()) {
            Vertex<SupplierResult> vertex = topologicalIterator.next();
            try {
                consumeResult(graph, vertex);
            } catch (Exception e) {
                handleException(graph, vertex, e);
            }
        }
    }

    public void consumeResult(Graph<SupplierResult> graph, Vertex<SupplierResult> vertex) {
        Set<Vertex<SupplierResult>> sourceVertexSet = graph.sourceVertexSet(vertex);
        Preconditions.checkArgument(
                sourceVertexSet.stream()
                        .noneMatch(sourceVertex -> null == sourceVertex.getCompletableFuture()),
                "DAG topological iteration and this method should set completableFuture for all source"
        );
        CompletableFuture<SupplierResult> completableFuture = CompletableFuture.allOf(
                sourceVertexSet.stream()
                        .map(Vertex::getCompletableFuture)
                        .toArray(CompletableFuture[]::new)
        ).thenApplyAsync(
                voidInput -> {
                    if (sourceVertexSet.stream()
                            .map(Vertex::getCompletableFuture)
                            .anyMatch(sourceCompletableFuture -> {
                                try {
                                    // use get() won't block: all source nodes have been finished
                                    return sourceCompletableFuture.get().failed();
                                } catch (InterruptedException | ExecutionException e) {
                                    throw new RuntimeException(e);
                                }
                            })) {
                        return SupplierResult.builder()
                                .success(false)
                                .message("source failed")
                                .exception(new SourceFailedException("source failed"))
                                .build();
                    }
                    return vertex.getSupplier().get();
                },
                executor
        );
        vertex.setCompletableFuture(completableFuture);
    }
}
