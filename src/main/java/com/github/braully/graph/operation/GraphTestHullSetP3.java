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

public class GraphTestHullSetP3 implements IGraphOperation {

    static final String type = "P3-Convexity";
    static final String description = "H(S) Test Hull Frontier";

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

        List<Integer> frontierFail = new ArrayList<>();
        List<Integer> unreachableFail = new ArrayList<>();

        if (caratheodoryNumberGraph.auxProcessor != null && caratheodoryNumberGraph.auxProcessor.length > 0) {
            for (int i = 0; i < caratheodoryNumberGraph.auxProcessor.length; i++) {
                int val = caratheodoryNumberGraph.auxProcessor[i];
                if (val == 0) {
                    unreachable.add(i);
                } else if (val == NEIGHBOOR_COUNT_INCLUDED) {
                    frontier.add(i);
                }
            }

            int remain = frontier.size() + unreachable.size();
            int[] auxb = caratheodoryNumberGraph.auxProcessor.clone();
            if (remain > 0) {
                for (Integer i : unreachable) {
                    int added = addVertToAux(auxb, graphRead, i);
                    if (added < remain) {
                        unreachableFail.add(i);
                    }
                    System.arraycopy(caratheodoryNumberGraph.auxProcessor, 0, auxb, 0, auxb.length);
                }
                for (Integer i : frontier) {
                    int added = addVertToAux(auxb, graphRead, i);
                    if (added < remain) {
                        frontierFail.add(i);
                    }
                    System.arraycopy(caratheodoryNumberGraph.auxProcessor, 0, auxb, 0, auxb.length);
                }
            }
        }

        response.put(OperationConvexityGraphResult.PARAM_NAME_CONVEX_HULL, hslist);
        response.put("Frontier H(S)", frontier);
        response.put("Unreachable", unreachable);
        response.put("Set(S)", set);
        response.put("Frontier-Fail", frontierFail);
        response.put("Unreachable-Fail", unreachableFail);

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
        int[] aux = new int[graph.getVertexCount()];
        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
        }
        addVertToAux(aux, graph, currentSet);

        Set<Integer> hsp3g = new HashSet<>();
        for (int i = 0; i < aux.length; i++) {
            if (aux[i] >= PROCESSED) {
                hsp3g.add(i);
            }
        }

        processedHullSet = new OperationConvexityGraphResult();
        processedHullSet.auxProcessor = aux;
        processedHullSet.convexHull = hsp3g;
        return processedHullSet;
    }

    public int addVertToAux(int[] aux, UndirectedSparseGraphTO<Integer, Integer> graph, int v) {
        return addVertToAux(aux, graph, new int[]{v});
    }

    public int addVertToAux(int[] aux, UndirectedSparseGraphTO<Integer, Integer> graph, int[] currentSet) {
        int cont = 0;
        if (currentSet == null || currentSet.length == 0) {
            return cont;
        }

        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        for (Integer v : currentSet) {
            if (aux[v] < INCLUDED) {
                mustBeIncluded.add(v);
                aux[v] = INCLUDED;
            }
        }

        while (!mustBeIncluded.isEmpty()) {
            Integer verti = mustBeIncluded.remove();
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
                }
            }
            aux[verti] = PROCESSED;
            cont++;
        }
        return cont;
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
