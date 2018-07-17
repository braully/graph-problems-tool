package com.github.braully.graph.generator;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.Map;

public class GraphGeneratorCycle extends GraphGeneratorPath {

    static final String description = "Cycle";

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public UndirectedSparseGraphTO<Integer, Integer> generateGraph(Map parameters) {
        UndirectedSparseGraphTO<Integer, Integer> graph = super.generateGraph(parameters);
        Integer nvertices = getIntegerParameter(parameters, N_VERTICES);

        if (nvertices == null) {
            nvertices = DEFAULT_NVERTICES;
        }
        graph.setName("C" + nvertices);
        graph.addEdge(graph.getEdgeCount(), graph.getVertexCount() - 1, 0);
        return graph;
    }
}
