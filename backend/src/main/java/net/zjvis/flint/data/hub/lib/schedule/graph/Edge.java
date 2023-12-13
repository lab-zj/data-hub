package net.zjvis.flint.data.hub.lib.schedule.graph;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jgrapht.graph.DefaultEdge;

import java.util.List;
import java.util.Map;

@Builder
@ToString
@EqualsAndHashCode(callSuper = false)
public class Edge extends DefaultEdge {

    private static final long serialVersionUID = -3627820097706105965L;
    @Getter
    private String name;
    @Getter
    private List<Map<String, String>> params;
}
