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

public class GraphAlgorithmHullSet implements IGraphOperation {

    static final String type = "P3-Convexity";
    static final String description = "H(S) Hull Algorithm";

    public static final int NEIGHBOOR_COUNT_INCLUDED = 1;
    public static final int INCLUDED = 2;
    public static final int PROCESSED = 3;

    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        Collection<Integer> set = graphRead.getSet();
        List<Integer> setC = new ArrayList<>(set);
        List<Integer> setN = new ArrayList<>();
        List<Integer> setE = new ArrayList<>();

        Set<Integer> setNset = new HashSet<>();
        for (Integer v : set) {
            setNset.addAll(graphRead.getNeighbors(v));
        }

        setN.addAll(setNset);
        Collections.sort(setN);
        setE.addAll(graphRead.getVertices());
        setE.removeAll(setN);
        setE.removeAll(set);
        setN.removeAll(set);
        Collections.sort(setE);

        OperationConvexityGraphResult caratheodoryNumberGraph = hsp3(graphRead, set);

        List<Integer> hslist = new ArrayList<Integer>();
        if (caratheodoryNumberGraph.convexHull != null) {
            hslist.addAll(caratheodoryNumberGraph.convexHull);
            Collections.sort(hslist);
        }

        List<Integer> frontier = new ArrayList<>();
        List<Integer> unreachable = new ArrayList<>();
        int[] aux = caratheodoryNumberGraph.auxProcessor;

        if (aux != null && aux.length > 0) {
            for (int i = 0; i < aux.length; i++) {
                int val = aux[i];
                if (val == 0) {
                    unreachable.add(i);
                } else if (val == NEIGHBOOR_COUNT_INCLUDED) {
                    frontier.add(i);
                }
            }
        }
        List<Integer> unreachableFail = new ArrayList<>();
        if (!unreachable.isEmpty()) {
            int[] auxb = aux.clone();

            for (Integer i : unreachable) {
                addVertToAux(auxb, graphRead, i);
                for (Integer j : aux) {
                    if (auxb[j] == 0) {
                        unreachableFail.add(i);
                        break;
                    }
                }
                System.arraycopy(aux, 0, auxb, 0, auxb.length);
            }
        }

        List<String> unreachableDetail = new ArrayList<>();

        for (Integer u : unreachable) {
            setNset.clear();
            setNset.addAll(graphRead.getNeighbors(u));
            setNset.retainAll(frontier);
            String key = "" + u + "(" + setNset.size() + "," + (graphRead.getNeighborCount(u) - setNset.size()) + ")";
            unreachableDetail.add(key);
        }

        Map response = new HashMap();
        response.put("Frontier H(S)|" + frontier.size() + "|", frontier);
        response.put("Unreachable|" + unreachable.size() + "|", unreachable);
        response.put("Unreachable-Fail-Dmt", unreachableFail);
        response.put("Unreachable-Detail", unreachableDetail);
        response.put("H(S)|" + hslist.size() + "|", hslist);
        response.put("C(S)|" + set.size() + "|", set);
        response.put("N(C)|" + setN.size() + "|", setN);
        response.put("E|" + setE.size() + "|", setE);
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
