package com.github.braully.graph.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.braully.graph.UndirectedSparseGraphTO;

public class GraphGenaratorHamming extends GraphGeneratorCartesianProduct {

    static final String Q = "Kis";
    static final String[] parameters = { Q };
    static final String description = "Hamming";
    static final String DEFAULT_Q = "1,2";

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
        String q = getStringParameter(parameters, Q);

        if (q == null) {
            q = DEFAULT_Q;
        }

        // splitar o parametro q e converter em uma lista de inteiros
        List<Integer> kis = new ArrayList<>();
        for (String s : q.split(",")) {
            kis.add(Integer.parseInt(s.trim()));
        }

        UndirectedSparseGraphTO<Integer, Integer> graph = generate(kis);

        return graph;
    }

    public UndirectedSparseGraphTO<Integer, Integer> generate(List<Integer> q) {
        UndirectedSparseGraphTO<Integer, Integer> kq = kgenerator.generate(q.get(0));
        UndirectedSparseGraphTO graph = kq.clone();

        for (int i = 1; i < q.size(); i++) {
            kq = kgenerator.generate(q.get(i));
            // cartesian product
            graph = this.cartesianProduct(graph, kq);
        }
        graph.setName("H" + q);
        return graph;
    }

    public static void main(String... args) {
        GraphGenaratorHamming hm = new GraphGenaratorHamming();
        UndirectedSparseGraphTO<Integer, Integer> generate = hm.generate(List.of(2, 3));
        //
        System.out.println(generate);
    }
}
