package com.github.braully.graph.generator;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

public class GraphGeneratorStriclyBinaryTree extends AbstractGraphGenerator {

    static final String N_VERTICES = "Nº Vertices";
    static final String[] parameters = {N_VERTICES};

    static final String description = "Binary (Stricly) Tree";

    @Override
    public String[] getParameters() {
        return parameters;
    }

    public String getDescription() {
        return description;
    }

    public UndirectedSparseGraphTO<Integer, Integer> generateGraph(Map parameters) {
        Integer nvertices = getIntegerParameter(parameters, N_VERTICES);

        if (nvertices == null) {
            nvertices = 3;
        }
        double lognv = Math.log(nvertices + 1) / Math.log(2);
        double pow = Math.pow(2, Math.ceil(lognv)) - 1;
        int nvert = (int) pow;
        UndirectedSparseGraphTO<Integer, Integer> graph = new UndirectedSparseGraphTO<>();
        graph.setName("T" + nvertices);

        Queue<Integer> frontier = new ArrayDeque<>();
        graph.addVertex(0);
        int countVertice = 1;
        frontier.add(0);

        while (!frontier.isEmpty() && countVertice < nvert) {
            Integer verti = frontier.remove();
            Integer target1 = countVertice++;
            Integer target2 = countVertice++;
            graph.addEdge(verti, target1);
            graph.addEdge(verti, target2);
            if (countVertice < nvert) {
                frontier.add(target1);
                frontier.add(target2);
            }
        }
        return graph;
    }
}
