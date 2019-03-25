package com.github.braully.graph.generator;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

public class GraphGeneratorBlock extends AbstractGraphGenerator {

    static final String N_VERTICES = "Nº Blocks";
    static final String MAX_CLIQUE = "Max Clique";
    static final String[] parameters = {N_VERTICES, MAX_CLIQUE};
    static final String description = "Block";
    static final Integer DEFAULT_NVERTICES = 5;
    static final Integer DEFAULT_MAX_CLIQUE = 5;

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
        Integer nblocks = getIntegerParameter(parameters, N_VERTICES);
        Integer maxclique = getIntegerParameter(parameters, MAX_CLIQUE);

        if (nblocks == null) {
            nblocks = DEFAULT_NVERTICES;
        }

        if (maxclique == null) {
            maxclique = DEFAULT_MAX_CLIQUE;
        }

        Integer verticeCount = 0;
        UndirectedSparseGraphTO<Integer, Integer> graph = new UndirectedSparseGraphTO<>();
        graph.setName("Block-" + nblocks + "-" + maxclique);

        Integer[] joinPoints = new Integer[nblocks];
        for (int b = 0; b < nblocks; b++) {
            Integer nvertices = (int) (Math.round(Math.random() * (DEFAULT_MAX_CLIQUE - 1)) + 1);
            Integer[] vertexs = new Integer[nvertices];
            for (int i = 0; i < nvertices; i++) {
                vertexs[i] = verticeCount + i;
                graph.addVertex(vertexs[i]);

            }
            joinPoints[b] = vertexs[0];

            for (int i = 0; i < nvertices; i++) {
                for (int j = i; j < nvertices - 1; j++) {
                    Integer source = vertexs[i];
                    Integer target = vertexs[j] + 1;
                    graph.addEdge(source, target);
                }
            }
            verticeCount = verticeCount + nvertices;
        }

        Queue<Integer> frontier = new ArrayDeque<>();
        graph.addVertex(0);
        int countVertice = 1;
        frontier.add(joinPoints[0]);

        while (!frontier.isEmpty()) {
            Integer verti = frontier.remove();
            Integer target1 = null;
            Integer target2 = null;

            long type = Math.round(Math.random() * 2);
            if (type == 0 && countVertice < joinPoints.length) {
                target1 = joinPoints[countVertice++];
                graph.addEdge(verti, target1);
                frontier.add(target1);
            } else if (type == 1 && countVertice < joinPoints.length) {
                target2 = joinPoints[countVertice++];
                graph.addEdge(verti, target2);
                frontier.add(target2);
            } else {
                if (countVertice < joinPoints.length) {
                    target1 = joinPoints[countVertice++];
                    graph.addEdge(verti, target1);
                    frontier.add(target1);
                }
                if (countVertice < joinPoints.length) {
                    target2 = joinPoints[countVertice++];
                    frontier.add(target2);
                    graph.addEdge(verti, target2);
                }
            }
        }
        return graph;
    }
}
