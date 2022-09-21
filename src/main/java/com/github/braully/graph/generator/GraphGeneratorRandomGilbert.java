package com.github.braully.graph.generator;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.Map;

public class GraphGeneratorRandomGilbert extends AbstractGraphGenerator {

    static final String N_VERTICES = "Nº Vertices";
    static final String PROBABILITY = "Probability";

    static final Integer DEFAULT_NVERTICES = 5;
    static final Double DEFAULT_PROBABILITY = 0.3;

    static final String[] parameters = {N_VERTICES, PROBABILITY};
    static final String description = "Random G(n,p)";

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
        Double probability = getDoubleParameter(parameters, PROBABILITY);

        if (nvertices == null) {
            nvertices = DEFAULT_NVERTICES;
        }
        if (probability == null) {
            probability = DEFAULT_PROBABILITY;
        }
        UndirectedSparseGraphTO<Integer, Integer> graph = generate(nvertices, probability);

        return graph;
    }

    public UndirectedSparseGraphTO<Integer, Integer> generate(Integer nvertices, Double probability) {
        UndirectedSparseGraphTO<Integer, Integer> graph = new UndirectedSparseGraphTO<>();
        graph.setName("G(" + nvertices + "," + probability + ")");
        Integer[] vertexs = new Integer[nvertices];
        for (int i = 0; i < nvertices; i++) {
            vertexs[i] = i;
            graph.addVertex(vertexs[i]);
        }
        Integer countEdge = 0;
        for (int i = 0; i < nvertices; i++) {
            for (int j = i + 1; j < nvertices; j++) {
//                https://stackoverflow.com/questions/8183840/probability-in-java
                if (Math.random() <= probability) {
                    graph.addEdge(countEdge++, i, j);
                }
            }
        }
        return graph;
    }

}
