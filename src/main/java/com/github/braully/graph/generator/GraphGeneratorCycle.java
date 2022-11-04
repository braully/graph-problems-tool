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
        Integer nvertices = getIntegerParameter(parameters, N_VERTICES);

        if (nvertices == null) {
            nvertices = DEFAULT_NVERTICES;
        }
        return generateCycleGraph(nvertices);
    }

    public UndirectedSparseGraphTO<Integer, Integer> generateCycleGraph(Integer nvertices) {
        UndirectedSparseGraphTO<Integer, Integer> graph = super.generatePathGraph(nvertices);

        graph.setName("C" + nvertices);
        graph.addEdge(graph.getEdgeCount(), graph.getVertexCount() - 1, 0);
        return graph;
    }
}
