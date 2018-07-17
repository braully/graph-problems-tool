package com.github.braully.graph.generator;

import com.github.braully.graph.UndirectedSparseGraphTO;
import static com.github.braully.graph.generator.GraphGeneratorKP.K_VERTICES;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GraphGeneratorCompleteBipartite extends AbstractGraphGenerator {

    static final String N_VERTICES = "N";
    static final String P_VERTICES = "M";
    static final String[] parameters = {K_VERTICES, P_VERTICES};
    static final String description = "Complete Bipartite";
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
        Integer nvertices = getIntegerParameter(parameters, K_VERTICES);
        Integer pvertices = getIntegerParameter(parameters, P_VERTICES);

        if (nvertices == null) {
            nvertices = DEFAULT_NVERTICES;
        }

        if (pvertices == null) {
            pvertices = DEFAULT_NVERTICES;
        }

        UndirectedSparseGraphTO<Integer, Integer> graph = new UndirectedSparseGraphTO<>();
        graph.setName("K" + nvertices + "," + pvertices);

        Integer[] vertexs = new Integer[nvertices + pvertices];
        for (int i = 0; i < nvertices + pvertices; i++) {
            vertexs[i] = i;
            graph.addVertex(vertexs[i]);
        }
        int countEdge = 0;
        for (int i = 0; i < nvertices; i++) {
            for (int j = nvertices; j < nvertices + pvertices; j++) {
                Integer source = vertexs[i];
                Integer target = vertexs[j];
                graph.addEdge(countEdge++, source, target);
            }
        }
        return graph;
    }
}
