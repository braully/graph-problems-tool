package com.github.braully.graph.generator;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GraphGeneratorCirculant extends AbstractGraphGenerator {

    static final String N_VERTICES = "Nº Vertices";
    static final String STRING_EDGES = "List";
    static final String[] parameters = {N_VERTICES, STRING_EDGES};
    static final String description = "Circulant";
    static final Integer DEFAULT_NVERTICES = 5;

    @Override
    public String[] getParameters() {
        return parameters;
    }

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

        String strEdges = getStringParameter(parameters, STRING_EDGES);

        UndirectedSparseGraphTO<Integer, Integer> graph = new UndirectedSparseGraphTO<>();
        graph.setName("C(" + nvertices + ",{" + strEdges + "})");

        Integer[] vertexs = new Integer[nvertices];
        for (int i = 0; i < nvertices; i++) {
            vertexs[i] = i;
            graph.addVertex(vertexs[i]);
        }

        String[] edges = null;
        Set<Integer> list = new HashSet<>();
        if (strEdges != null && (edges = strEdges.trim().split(",")) != null) {
            for (String stredge : edges) {
                Integer l = Integer.parseInt(stredge.trim());
                list.add(l);
            }
        }

        int countEdge = 0;
        for (int i = 0; i < nvertices - 1; i++) {
            Integer source = vertexs[i];
            for (Integer l : list) {
                Integer target = vertexs[(i + l) % nvertices];
                graph.addEdge(countEdge++, source, target);
                target = vertexs[Math.abs(i + nvertices - l) % nvertices];
                graph.addEdge(countEdge++, source, target);
            }
        }
        return graph;
    }
}
