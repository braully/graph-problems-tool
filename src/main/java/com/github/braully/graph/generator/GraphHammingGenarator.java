package com.github.braully.graph.generator;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.math3.util.CombinatoricsUtils;

public class GraphHammingGenarator extends GraphGeneratorCartesianProduct {

    static final String D = "N";
    static final String Q = "K";
    static final String[] parameters = {D, Q};
    static final String description = "H(d,q)";
    static final Integer DEFAULT_D = 5;
    static final Integer DEFAULT_Q = 5;

    GraphGeneratorComplete kgenerator = new GraphGeneratorComplete();

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
        Integer d = getIntegerParameter(parameters, D);
        Integer q = getIntegerParameter(parameters, Q);

        if (d == null) {
            d = DEFAULT_D;
        }

        if (q == null) {
            q = DEFAULT_Q;
        }

        UndirectedSparseGraphTO<Integer, Integer> graph = generate(d, q);

        return graph;
    }

    public UndirectedSparseGraphTO<Integer, Integer> generate(Integer d, Integer q) {
        UndirectedSparseGraphTO<Integer, Integer> kq = kgenerator.generate(q);
        UndirectedSparseGraphTO graph = kq.clone();

        List<String> labels = new ArrayList<>();
        List<String> labelsaux = new ArrayList<>();

        for (int i = 0; i < q; i++) {
            labels.add("" + i);
        }

        for (int i = 1; i < d; i++) {
//            graphs[i] =  kq.clone();
            graph = this.cartesianProduct(graph, kq);
            for (String lbl : labels) {
                for (int j = 0; j < q; j++) {
                    labelsaux.add(lbl + "," + j);
        }
            }
            List aux = labels;
            labels = labelsaux;
            labelsaux = aux;
            aux.clear();

        }
        graph.setLabels(labels);

//        UndirectedSparseGraphTO graph = this.cartesianProduct(graphs);
        graph.setName("H(" + d + "," + q + ")");
//        System.out.println("Labels: " + labels);
//        for (Integer v : (List<Integer>) graph.getVertices()) {
//            System.out.println(v + ": " + labels.get(v.intValue()));
//        }
        return graph;
    }
    //    UndirectedSparseGraphTO<Integer, Integer> cartesianProduct(UndirectedSparseGraphTO<Integer, Integer>...graphs) {
    //        int nvertices0 = graph0.getVertexCount();
    //        int nvertices1 = graph1.getVertexCount();
    //
    //        // We order the vertices of the cartesian product by each element aij = (vi, uj) as its vertexes, a pair of vi in G and uj in H
    //        // To know (vi, uj) from aij, vi = aij/nvertices1, uj = aij%nvertices1
    //        // To know aij from (vi, uj), aij = vi*nvertices1 + uj
    //        int nvertices = nvertices0 * nvertices1;
    //
    //        UndirectedSparseGraphTO<Integer, Integer> graph = new UndirectedSparseGraphTO<>();
    //        graph.setName("G x H");
    //
    //        List<Integer> vertexElegibles = new ArrayList<>(nvertices);
    //        Integer[] vertexs = new Integer[nvertices];
    //        for (int i = 0; i < nvertices; i++) {
    //            vertexElegibles.add(i);
    //            vertexs[i] = i;
    //            graph.addVertex(vertexs[i]);
    //        }
    //
    //        List<Integer> edges0 = new ArrayList<>();
    //        edges0.addAll((Collection<Integer>) graph0.getEdges());
    //        List<Integer> edges1 = new ArrayList<>();
    //        edges1.addAll((Collection<Integer>) graph1.getEdges());
    //
    //        int edge_count = 0;
    //
    //        // For each edge (vi, vj) in G, make (aim, ajm) for m from 0 to nvertices1
    //        for (Integer e : edges0) {
    //            Pair p = (Pair) graph0.getEndpoints(e);
    //            int i = (int) p.getFirst();
    //            int j = (int) p.getSecond();
    //
    //            for (int m = 0; m < nvertices1; m++) {
    //                int line = i * nvertices1 + m;
    //                int column = j * nvertices1 + m;
    //                graph.addEdge(edge_count++, line, column);
    //            }
    //        }
    //
    //        // For each edge (ui, uj) in H, make (ami, amj) for m from 0 to nvertices0
    //        for (Integer e : edges1) {
    //            Pair p = (Pair) graph1.getEndpoints(e);
    //            int i = (int) p.getFirst();
    //            int j = (int) p.getSecond();
    //
    //            for (int m = 0; m < nvertices0; m++) {
    //                int line = m * nvertices1 + i;
    //                int column = m * nvertices1 + j;
    //                graph.addEdge(edge_count++, line, column);
    //            }
    //        }
    //
    //        return graph;
    //    }

    public static void main(String... args) {
        GraphHammingGenarator hm = new GraphHammingGenarator();
        UndirectedSparseGraphTO<Integer, Integer> generate = hm.generate(2, 3);
//
        System.out.println(generate);
    }
}
