package com.github.braully.graph.generator;

import com.github.braully.graph.UndirectedSparseGraphTO;
/*
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;
 */

import edu.uci.ics.jung.graph.util.Pair;
import java.util.*;

public class GraphGeneratorCartesianProduct extends AbstractGraphGenerator {

    static final String STRING_EDGES0 = "Edge-string of graph G";
    static final String STRING_EDGES1 = "Edge-string of graph H";
    static final String[] parameters = {STRING_EDGES0, STRING_EDGES1};
    static final String description = "Castesian Product G x H";

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

        String strEdges0 = getStringParameter(parameters, STRING_EDGES0);
        String strEdges1 = getStringParameter(parameters, STRING_EDGES1);

        UndirectedSparseGraphTO<Integer, Integer> graph0 = new UndirectedSparseGraphTO<>();
        graph0.setName("G");
        graph0.addEdgesFromString(strEdges0);

        UndirectedSparseGraphTO<Integer, Integer> graph1 = new UndirectedSparseGraphTO<>();
        graph1.setName("H");
        graph1.addEdgesFromString(strEdges1);

        // Seting vertex positions
        int nvertices0 = graph0.getVertexCount();
        int nvertices1 = graph1.getVertexCount();
        int[] positionX = new int[(nvertices0 * nvertices1)+2];
        int[] positionY = new int[(nvertices0 * nvertices1)+2];
        for (int i = 0; i < nvertices0; i++) {
            for (int j = 0; j < nvertices1; j++) {
                positionX[i * nvertices0 + j] = i;
                positionY[i * nvertices0 + j] = j;
            }
        }

        // Positions (x,y) to a vertex v are (positionX[v], positionY[v])
        UndirectedSparseGraphTO<Integer, Integer> graph = cartesianProduct(graph0, graph1);
        graph.setPositionX(positionX);
        graph.setPositionY(positionY);
        return graph;
    }

    // Calculates the cartesian product G x H of two graphs G and H
    UndirectedSparseGraphTO<Integer, Integer> cartesianProduct(UndirectedSparseGraphTO<Integer, Integer> graph0, UndirectedSparseGraphTO<Integer, Integer> graph1) {
        int nvertices0 = graph0.getVertexCount();
        int nvertices1 = graph1.getVertexCount();

        // We order the vertices of the cartesian product by each element aij = (vi, uj) as its vertexes, a pair of vi in G and uj in H
        // To know (vi, uj) from aij, vi = aij/nvertices1, uj = aij%nvertices1
        // To know aij from (vi, uj), aij = vi*nvertices1 + uj
        int nvertices = nvertices0 * nvertices1;

        UndirectedSparseGraphTO<Integer, Integer> graph = new UndirectedSparseGraphTO<>();
        graph.setName("G x H");

        List<Integer> vertexElegibles = new ArrayList<>(nvertices);
        Integer[] vertexs = new Integer[nvertices];
        for (int i = 0; i < nvertices; i++) {
            vertexElegibles.add(i);
            vertexs[i] = i;
            graph.addVertex(vertexs[i]);
        }

        List<Integer> edges0 = new ArrayList<>();
        edges0.addAll((Collection<Integer>) graph0.getEdges());
        List<Integer> edges1 = new ArrayList<>();
        edges1.addAll((Collection<Integer>) graph1.getEdges());

        int edge_count = 0;

        // For each edge (vi, vj) in G, make (aim, ajm) for m from 0 to nvertices1
        for (Integer e : edges0) {
            Pair p = (Pair) graph0.getEndpoints(e);
            int i = (int) p.getFirst();
            int j = (int) p.getSecond();

            for (int m = 0; m < nvertices1; m++) {
                int line = i * nvertices1 + m;
                int column = j * nvertices1 + m;
                graph.addEdge(edge_count++, line, column);
            }
        }

        // For each edge (ui, uj) in H, make (ami, amj) for m from 0 to nvertices0
        for (Integer e : edges1) {
            Pair p = (Pair) graph1.getEndpoints(e);
            int i = (int) p.getFirst();
            int j = (int) p.getSecond();

            for (int m = 0; m < nvertices0; m++) {
                int line = m * nvertices1 + i;
                int column = m * nvertices1 + j;
                graph.addEdge(edge_count++, line, column);
            }
        }

        return graph;
    }

}
