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

        for (int i = 1; i < d; i++) {
            graph = this.cartesianProduct(graph, kq);
        }
        graph.setName("H(" + d + "," + q + ")");
        return graph;
    }

    public static void main(String... args) {
        GraphHammingGenarator hm = new GraphHammingGenarator();
        UndirectedSparseGraphTO<Integer, Integer> generate = hm.generate(2, 3);
//
        System.out.println(generate);
    }
}
