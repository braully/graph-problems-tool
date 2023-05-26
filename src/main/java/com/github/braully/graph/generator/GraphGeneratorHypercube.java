package com.github.braully.graph.generator;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GraphGeneratorHypercube extends AbstractGraphGenerator {

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

        return generate(nvertices);
    }

    public UndirectedSparseGraphTO<Integer, Integer> generate(Integer n) throws NumberFormatException {
        UndirectedSparseGraphTO<Integer, Integer> graph = new UndirectedSparseGraphTO<>();
        graph.setName("Q" + n);
        int nvertices = (int) Math.pow(2, n);
        Integer[] vertexs = new Integer[nvertices];
        for (int i = 0; i < nvertices; i++) {
            vertexs[i] = i;
            graph.addVertex(vertexs[i]);
        }
        int countEdge = 0;

        for (int i = 0; i < nvertices; i++) {
            for (int j = i + 1; j < nvertices; j++) {
                for (int z = 0; z < n; z++) {
                    if ((j ^ i) == (1 << z)) {
                        graph.addEdge(countEdge++, vertexs[i], vertexs[j]);
                        break;
                    }
                }
            }
        }

        return graph;
    }

}
