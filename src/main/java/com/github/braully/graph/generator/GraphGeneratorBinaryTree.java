package com.github.braully.graph.generator;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

public class GraphGeneratorBinaryTree extends AbstractGraphGenerator {

    static final String N_VERTICES = "Nº Vertices";
    static final String[] parameters = {N_VERTICES};

    static final String description = "Binary Tree";

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
            Integer target1 = null;
            Integer target2 = null;

            long type = Math.round(Math.random() * 2);
//            System.out.println("Random type: " + type);
            if (type == 0 && countVertice < nvertices) {
                target1 = countVertice++;
                graph.addEdge(verti, target1);
                frontier.add(target1);
            } else if (type == 1 && countVertice < nvertices) {
                target2 = countVertice++;
                graph.addEdge(verti, target2);
                frontier.add(target2);
            } else {
                if (countVertice < nvertices) {
                    target1 = countVertice++;
                    graph.addEdge(verti, target1);
                    frontier.add(target1);
                }
                if (countVertice < nvertices) {
                    target2 = countVertice++;
                    frontier.add(target2);
                    graph.addEdge(verti, target2);
                }
            }
        }
        return graph;
    }
}
