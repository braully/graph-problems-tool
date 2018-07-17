package com.github.braully.graph.generator;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GraphGeneratorFromEdge extends AbstractGraphGenerator {

    static final String N_VERTICES = "Nº Vertices";
    static final String STRING_EDGES = "Edge-string";
    static final String[] parameters = {N_VERTICES, STRING_EDGES};
    static final String description = "From Edges String";
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
        graph.setName("ES" + N_VERTICES);
        List<Integer> vertexElegibles = new ArrayList<>(nvertices);
//        Integer[] vertexs = new Integer[nvertices];
//        for (int i = 0; i < nvertices; i++) {
//            vertexElegibles.add(i);
//            vertexs[i] = i;
//            graph.addVertex(vertexs[i]);
//        }

        String[] edges = null;

        if (strEdges != null && (edges = strEdges.trim().split(",")) != null) {
            try {
                int countEdge = 0;
                for (String stredge : edges) {
                    String[] vs = stredge.split("-");
                    if (vs != null && vs.length >= 2) {
                        Integer source = Integer.parseInt(vs[0].trim());
                        Integer target = Integer.parseInt(vs[1].trim());
                        graph.addEdge(countEdge++, source, target);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return graph;
    }
}
