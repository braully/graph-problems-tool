package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class GraphHullSetP3 implements IGraphOperation {

    static final String type = "P3-Convexity";
    static final String description = "H(S)";

    public static final int NEIGHBOOR_COUNT_INCLUDED = 1;
    public static final int INCLUDED = 2;
    public static final int PROCESSED = 3;

    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        long totalTimeMillis = -1;
        Collection<Integer> set = graphRead.getSet();

        totalTimeMillis = System.currentTimeMillis();
        OperationConvexityGraphResult caratheodoryNumberGraph = null;
        if (set.size() >= 2) {
            caratheodoryNumberGraph = hsp3(graphRead, set);
        }
        totalTimeMillis = System.currentTimeMillis() - totalTimeMillis;

        /* Processar a buscar pelo caratheodoryset e caratheodorynumber */
        Map<String, Object> response = new HashMap<>();
        if (caratheodoryNumberGraph == null) {
            caratheodoryNumberGraph = new OperationConvexityGraphResult();
        }

        List<Integer> hslist = new ArrayList<Integer>();
        if (caratheodoryNumberGraph.convexHull != null) {
            hslist.addAll(caratheodoryNumberGraph.convexHull);
            Collections.sort(hslist);
        }

        List<Integer> frontier = new ArrayList<>();
        List<Integer> unreachable = new ArrayList<>();

        if (caratheodoryNumberGraph.auxProcessor != null && caratheodoryNumberGraph.auxProcessor.length > 0) {
            for (int i = 0; i < caratheodoryNumberGraph.auxProcessor.length; i++) {
                int val = caratheodoryNumberGraph.auxProcessor[i];
                if (val == 0) {
                    unreachable.add(i);
                } else if (val == NEIGHBOOR_COUNT_INCLUDED) {
                    frontier.add(i);
                }
            }
        }

        Set<Integer> closed = new HashSet();
        for (Integer i : set) {
            closed.add(i);
            closed.addAll((Collection<Integer>) graphRead.getNeighbors(i));
        }
        List<Integer> closedNeighbor = new ArrayList<Integer>(closed);
        Collections.sort(closedNeighbor);

        List<Integer> complement = new ArrayList<Integer>(graphRead.getVertices());
        complement.removeAll(closedNeighbor);
        Collections.sort(complement);

        response.put(OperationConvexityGraphResult.PARAM_NAME_CONVEX_HULL, hslist);
        response.put("Frontier H(S)", frontier);
        response.put("Unreachable", unreachable);
        response.put("Set(S)", set);
        response.put("N[" + set + "]", closedNeighbor);
        response.put("V(G)-N[" + set + "]", complement);
        response.put(OperationConvexityGraphResult.PARAM_NAME_INCLUDED_SEQUENCE, caratheodoryNumberGraph.includedSequence);
        return response;
    }

    public OperationConvexityGraphResult hsp3(UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] currentSet) {
        OperationConvexityGraphResult processedHullSet = null;
        processedHullSet = hsp3aux(graph, currentSet);
        return processedHullSet;
    }

    public OperationConvexityGraphResult hsp3aux(UndirectedSparseGraphTO<Integer, Integer> graph, int[] currentSet) {
        OperationConvexityGraphResult processedHullSet = null;
        Set<Integer> hsp3g = new HashSet<>();
        int[] aux = new int[graph.getVertexCount()];
        int[] auxc = new int[graph.getVertexCount()];
        List<Integer> includedSequence = new ArrayList<>();

        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
            auxc[i] = 0;
        }
        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        for (Integer v : currentSet) {
            mustBeIncluded.add(v);
            aux[v] = INCLUDED;
            auxc[v] = 1;
        }
        while (!mustBeIncluded.isEmpty()) {
            Integer verti = mustBeIncluded.remove();
            hsp3g.add(verti);
            includedSequence.add(verti);
            Collection<Integer> neighbors = graph.getNeighbors(verti);

            for (int vertn : neighbors) {
                if (vertn == verti) {
                    continue;
                }
                if (vertn != verti && aux[vertn] < INCLUDED) {
                    aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                    if (aux[vertn] == INCLUDED) {
                        mustBeIncluded.add(vertn);
                    }
                    auxc[vertn] = auxc[vertn] + auxc[verti];
                }
            }
            aux[verti] = PROCESSED;
        }

        Set<Integer> setCurrent = new HashSet<>();
        for (int i : currentSet) {
            setCurrent.add(i);
        }
        processedHullSet = new OperationConvexityGraphResult();
        processedHullSet.auxProcessor = aux;
        processedHullSet.convexHull = hsp3g;
        processedHullSet.includedSequence = includedSequence;
        return processedHullSet;
    }

    public OperationConvexityGraphResult hsp3(UndirectedSparseGraphTO<Integer, Integer> graphRead, Collection<Integer> set) {
        int[] arr = new int[set.size()];
        int i = 0;
        for (Integer v : set) {
            arr[i] = v;
            i++;
        }
        return hsp3(graphRead, arr);
    }

    @Override
    public String getTypeProblem() {
        return type;
    }

    @Override
    public String getName() {
        return description;
    }

}
