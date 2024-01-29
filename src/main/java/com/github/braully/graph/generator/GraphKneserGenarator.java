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

public class GraphKneserGenarator extends AbstractGraphGenerator {

    static final String N_VERTICES = "N";
    static final String K_VERTICES = "K";
    static final String[] parameters = {N_VERTICES, K_VERTICES};
    static final String description = "K(n,k)";
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
        Integer kvertices = getIntegerParameter(parameters, K_VERTICES);

        if (nvertices == null) {
            nvertices = DEFAULT_NVERTICES;
        }

        if (kvertices == null) {
            kvertices = 2;
        }

        UndirectedSparseGraphTO<Integer, Integer> graph = generate(nvertices, kvertices);

        return graph;
    }

    public UndirectedSparseGraphTO<Integer, Integer> generate(Integer nvertices, Integer kvertices) {
        UndirectedSparseGraphTO<Integer, Integer> graph = new UndirectedSparseGraphTO<>();
        graph.setName("K(" + nvertices + "," + kvertices + ")");
        Map<Integer, Set> mvt = new HashMap<>();
        Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(nvertices, kvertices);
        while (combinationsIterator.hasNext()) {
            int[] currentSet = combinationsIterator.next();
            Set<Integer> set = new HashSet<>(currentSet.length);
            for (int i : currentSet) {
                set.add(i);
            }
            mvt.put(graph.addVertex(), set);
        }
        Set<Map.Entry<Integer, Set>> entrySet = mvt.entrySet();
        for (Map.Entry<Integer, Set> e : entrySet) {
            Integer v0 = e.getKey();
            Set sv0 = e.getValue();
            for (Integer v : mvt.keySet()) {
                if (!v.equals(v0) && Collections.disjoint(sv0, mvt.get(v))) {
                    graph.addEdge(v0, v);
                }
            }
        }

        return graph;
    }
}
