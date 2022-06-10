package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import util.PairShort;

public class GraphHullSetNC implements IGraphOperation {

    static final String type = "P3-Convexity";
    static final String description = "H(S) Analytic";

    public static final int NEIGHBOOR_COUNT_INCLUDED = 1;
    public static final int INCLUDED = 2;
    public static final int PROCESSED = 3;

    protected LinkedHashMap<Integer, List<Integer>> includedSequenceN = new LinkedHashMap<>();

    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        long totalTimeMillis = -1;
        Collection<Integer> set = graphRead.getSet();

        List<Integer> vertices = (List<Integer>) graphRead.getVertices();

        totalTimeMillis = System.currentTimeMillis();
        OperationConvexityGraphResult hsGraph = null;
        if (set.size() >= 2) {
            hsGraph = hsp3(graphRead, set);
        }
        totalTimeMillis = System.currentTimeMillis() - totalTimeMillis;

        /* Processar a buscar pelo caratheodoryset e caratheodorynumber */
        Map<String, Object> response = new HashMap<>();
        if (hsGraph == null) {
            hsGraph = new OperationConvexityGraphResult();
        }

        List<Integer> hslist = new ArrayList<Integer>();
        if (hsGraph.convexHull != null) {
            hslist.addAll(hsGraph.convexHull);
            Collections.sort(hslist);
        }

        List<Integer> frontier = new ArrayList<>();
        List<Integer> unreachable = new ArrayList<>();

        if (hsGraph.auxProcessor != null && hsGraph.auxProcessor.length > 0) {
            for (Integer i : vertices) {
                int val = hsGraph.auxProcessor[i];
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
//            response.put("N[" + set + "]", closedNeighbor);
//            response.put("V(G)-N[" + set + "]", complement);
        //System.out.println("\nIncludade sequence n:{");
        Set<Integer> includeat = new HashSet<>();
        for (Integer v : hsGraph.includedSequence) {
            //System.out.print(v + ":" + includedSequenceN.get(v) + ", ");
            includeat.add(v);
            Collection<Integer> ns = graphRead.getNeighborsUnprotected(v);
            for (Integer vn : ns) {
                Collection<Integer> vnn = graphRead.getNeighborsUnprotected(vn);
                if (vnn.size() > 3) {
                    if (includeat.containsAll(vnn)) {
                        //System.out.println("\n[" + vn + "]");
                    }
                }
            }
        }
        //System.out.println("}\n");

//        response.put("Viz", hsGraph.vizs);
//        response.put("Pot", hsGraph.verticesPotenciais);
        response.put("|H(S)|", hsGraph.convexHull.size());
        response.put(OperationConvexityGraphResult.PARAM_NAME_INCLUDED_SEQUENCE, hsGraph.includedSequence);

        return response;
    }

    public OperationConvexityGraphResult hsp3(UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] currentSet) {
        OperationConvexityGraphResult processedHullSet = null;
        processedHullSet = hsp3aux(graph, currentSet);
        return processedHullSet;
    }

    public OperationConvexityGraphResult hsp3aux(UndirectedSparseGraphTO<Integer, Integer> graph, int[] currentSet) {
        OperationConvexityGraphResult processedHullSet = new OperationConvexityGraphResult();

        includedSequenceN.clear();

        for (Integer v : (Collection< Integer>) graph.getVertices()) {
            includedSequenceN.put(v, new ArrayList<>(2));
        }

        Set<Integer> hsp3g = new HashSet<>();
        int[] aux = new int[(int) graph.maxVertex() + 1];
        int[] auxc = new int[(int) graph.maxVertex() + 1];
        List<Integer> includedSequence = new ArrayList<>();

        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
            auxc[i] = 0;
        }
        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        Queue<Integer> mustBeIncluded2 = new ArrayDeque<>();

        Set<Integer> vizs = new HashSet<>();
        int iteracao = 0;

//        //System.out.println("Iteracao-" + iteracao + ": ");
        for (Integer v : currentSet) {
            mustBeIncluded.add(v);
            aux[v] = PROCESSED;
            auxc[v] = 1;
            hsp3g.add(v);
            includedSequence.add(v);
            includedSequenceN.get(v).add(v);
            //System.out.print(v + ":" + includedSequenceN.get(v) + ", ");
            Collection<Integer> neighbors = graph.getNeighborsUnprotected(v);
            vizs.addAll(neighbors);
            for (int vertn : neighbors) {
                if (vertn == v) {
                    continue;
                }
                if (!v.equals(vertn) && aux[vertn] < INCLUDED) {
                    aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                    if (aux[vertn] == INCLUDED) {
                        mustBeIncluded2.add(vertn);
                        vizs.addAll(graph.getNeighborsUnprotected(vertn));
                    }
                    includedSequenceN.get(vertn).add(v);
                    auxc[vertn] = auxc[vertn] + auxc[v];
                }
            }
        }
//        //System.out.println();

        mustBeIncluded.addAll(mustBeIncluded2);
        mustBeIncluded2.clear();

        vizs.removeAll(mustBeIncluded);
        //Viz forma um grafo conexo induzido?
        processedHullSet.vizs = vizs;
        Set<Integer> verticesPotenciais = new HashSet<>();
        //Algum vertice v, contem todos os vizinhos em Viz?
        for (Integer v : (List<Integer>) graph.cacheVertices()) {
            if (!mustBeIncluded.contains(v)) {
                Collection<Integer> neighbor = graph.getNeighborsUnprotected(v);
                if (vizs.containsAll(neighbor)) {
                    //Todo os vizinhos de v estão em viz
                    verticesPotenciais.add(v);
                }
            }
        }
        processedHullSet.verticesPotenciais = verticesPotenciais;

//        //System.out.println("Vertice incluidos a priori: ");
//        //System.out.println(mustBeIncluded);
        while (!mustBeIncluded.isEmpty()) {
            Integer verti = mustBeIncluded.remove();
            if (aux[verti] == PROCESSED) {
                continue;
            }
            hsp3g.add(verti);
//            //System.out.print(verti + ":" + includedSequenceN.get(verti) + ", ");
            includedSequence.add(verti);
            Collection<Integer> neighbors = graph.getNeighborsUnprotected(verti);

            for (int vertn : neighbors) {
                if (vertn == verti) {
                    continue;
                }
                if (vertn != verti && aux[vertn] < INCLUDED) {
                    aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                    if (aux[vertn] == INCLUDED) {
                        mustBeIncluded2.add(vertn);
                    }
                    auxc[vertn] = auxc[vertn] + auxc[verti];
                    includedSequenceN.get(vertn).add(verti);
                }
            }
            aux[verti] = PROCESSED;
        }
        //System.out.println();
        iteracao++;
        //System.out.println("Iteracao-" + iteracao + ": ");
        while (!mustBeIncluded2.isEmpty()) {
            Integer verti = mustBeIncluded2.remove();
            if (aux[verti] == PROCESSED) {
                continue;
            }
            hsp3g.add(verti);
            //System.out.print(verti + ":" + includedSequenceN.get(verti) + ", ");
            includedSequence.add(verti);
            Collection<Integer> neighbors = graph.getNeighborsUnprotected(verti);

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
                    includedSequenceN.get(vertn).add(verti);
                }
            }
            if (mustBeIncluded2.isEmpty() && !mustBeIncluded.isEmpty()) {
                mustBeIncluded2.addAll(mustBeIncluded);
                mustBeIncluded.clear();
                //System.out.println();
                iteracao++;
                //System.out.println("Iteracao-" + iteracao + ": ");
            }
            aux[verti] = PROCESSED;
        }
        //System.out.println();

        Set<Integer> setCurrent = new HashSet<>();
        for (int i : currentSet) {
            setCurrent.add(i);
        }
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
