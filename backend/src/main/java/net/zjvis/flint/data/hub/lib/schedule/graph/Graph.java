package net.zjvis.flint.data.hub.lib.schedule.graph;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jgrapht.graph.DirectedAcyclicGraph;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@EqualsAndHashCode
@ToString
public class Graph<CallableResultType extends Vertex.SupplierResult> implements Serializable {
    private static final long serialVersionUID = -6130432385056713585L;
    private final DirectedAcyclicGraph<Vertex<CallableResultType>, Edge> graph;
    @Getter
    private final String name;

    @Builder
    public Graph(String name, EdgeSupplier edgeSupplier) {
        this.name = name;
        graph = DirectedAcyclicGraph.<Vertex<CallableResultType>, Edge>createBuilder(
                Optional.ofNullable(edgeSupplier)
                        .orElse(EdgeSupplier.builder()
                                .namePrefix(name)
                                .build())).build();
    }

    /**
     * @param vertex vertex to add
     * @return If this graph already contains such vertex, the call leaves this graph unchanged and returns false.
     */
    public boolean addVertex(Vertex<CallableResultType> vertex) {
        return graph.addVertex(vertex);
    }

    /**
     * This method creates the new edge e using graph's edge supplier
     *
     * @param source source vertex
     * @param target target vertex
     */
    public void addEdge(Vertex<CallableResultType> source, Vertex<CallableResultType> target) {
        graph.addEdge(source, target);
    }

    public void addEdge(Vertex<CallableResultType> source, Vertex<CallableResultType> target,
        Edge edge) {
        graph.addEdge(source, target, edge);
    }

    public Edge getEdge(Vertex<CallableResultType> source, Vertex<CallableResultType> target) {
        return graph.getEdge(source, target);
    }

    public Set<Vertex<CallableResultType>> sourceVertexSet(Vertex<CallableResultType> targetVertex) {
        return graph.incomingEdgesOf(targetVertex)
                .stream()
                .map(graph::getEdgeSource)
                .collect(Collectors.toSet());
    }

    public Set<Vertex<CallableResultType>> outGoingVertexSet(Vertex<CallableResultType> sourceVertex) {
        return graph.outgoingEdgesOf(sourceVertex)
                .stream()
                .map(graph::getEdgeTarget)
                .collect(Collectors.toSet());
    }

    public Iterator<Vertex<CallableResultType>> topologicalIterator() {
        return graph.iterator();
    }

    public Set<Vertex<CallableResultType>> vertexSet() {
        return graph.vertexSet();
    }
}
