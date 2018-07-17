package com.github.braully.graph.operation;

import com.github.braully.graph.GraphWS;
import com.github.braully.graph.UndirectedSparseGraphTO;
import static com.github.braully.graph.operation.GraphCheckCaratheodorySet.PROCESSED;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.log4j.Logger;

public class GraphHullTestVs implements IGraphOperation {

    static final String type = "P3-Convexity";
    static final String description = "Hull TestVs (Java)";

    private static final Logger log = Logger.getLogger(GraphWS.class);

    public static final String PARAM_NAME_HULL_NUMBER = "number";
    public static final String PARAM_NAME_HULL_SET = "set";
    public static final String PARAM_NAME_SERIAL_TIME = "serial";
    public static final String PARAM_NAME_PARALLEL_TIME = "parallel";
    public static final String COMMAND_GRAPH_HN = "/home/strike/Workspace/pesquisa/graph-hull-number-parallel/graph-test/";
    public final int INCLUDED = 2;
    public final int NEIGHBOOR_COUNT_INCLUDED = 1;

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Set<Integer> hullSet = new HashSet(graph.getVertices());
        Integer minimumHS = 0;

        try {
            int currentSize = 3;
            Collection<Integer> vertices = graph.getVertices();
            minimumHS = vertices.size();

            Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(graph.getVertexCount(), currentSize);
            while (combinationsIterator.hasNext()) {
                int[] currentSet = combinationsIterator.next();
                int a = currentSet[0];
                int b = currentSet[1];
                int c = currentSet[2];
                if (graph.isNeighbor(a, b) || graph.isNeighbor(a, c) || graph.isNeighbor(b, c)) {
                } else {
                    int contHs = countHs(graph, currentSet);
                    if (contHs < minimumHS) {
                        hullSet.clear();
                        for (int i : currentSet) {
                            hullSet.add(i);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error(null, ex);
        }

        /* Processar a buscar pelo hullset e hullnumber */
        Map<String, Object> response = new HashMap<>();
        response.put("Minimum H(S)", minimumHS);
        response.put("S", hullSet);
        return response;
    }

    public int addVertToS(Integer verti, Set<Integer> s,
            UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux) {
        int countIncluded = 0;
        if (verti == null || aux[verti] >= INCLUDED) {
            return countIncluded;
        }

        aux[verti] = aux[verti] + INCLUDED;
        if (s != null) {
            s.add(verti);
        }

        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        mustBeIncluded.add(verti);
        while (!mustBeIncluded.isEmpty()) {
            verti = mustBeIncluded.remove();
            Collection<Integer> neighbors = graph.getNeighbors(verti);
            for (int vertn : neighbors) {
                if (vertn == verti) {
                    continue;
                }
                if (vertn != verti && ++aux[vertn] == INCLUDED) {
                    mustBeIncluded.add(vertn);
                }
            }
            countIncluded++;
        }
        return countIncluded;
    }

    private Set<Integer> calcCeillingHullNumberGraph(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Set<Integer> ceilling = new HashSet<>();
        if (graph != null) {
            Collection<Integer> vertices = graph.getVertices();

            if (vertices != null) {
                ceilling.addAll(vertices);
            }
        }
        return ceilling;
    }

    public Set<Integer> findHullSetBruteForce(UndirectedSparseGraphTO<Integer, Integer> graph, int currentSetSize) {
        Set<Integer> hullSet = null;
        if (graph == null || graph.getVertexCount() <= 0) {
            return hullSet;
        }
        Collection vertices = graph.getVertices();
        Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(graph.getVertexCount(), currentSetSize);
        while (combinationsIterator.hasNext()) {
            int[] currentSet = combinationsIterator.next();
            if (checkIfHullSet(graph, currentSet)) {
                hullSet = new HashSet<>(currentSetSize);
                for (int i : currentSet) {
                    hullSet.add(i);
                }
                break;
            }
        }
        return hullSet;
    }

    public boolean checkIfHullSet(UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] currentSet) {
        if (currentSet == null || currentSet.length == 0) {
            return false;
        }
        Set<Integer> fecho = new HashSet<>();
        Collection vertices = graph.getVertices();
        int[] aux = new int[graph.getVertexCount()];
        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
        }

        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        for (Integer v : currentSet) {
            mustBeIncluded.add(v);
            aux[v] = INCLUDED;
        }
        while (!mustBeIncluded.isEmpty()) {
            Integer verti = mustBeIncluded.remove();
            fecho.add(verti);
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
        }
        return fecho.size() == graph.getVertexCount();
    }

    public void includeVertex(UndirectedSparseGraphTO<Integer, Integer> graph, Set<Integer> fecho, int[] aux, int i) {
        fecho.add(i);
        aux[i] = INCLUDED;
        Collection<Integer> neighbors = graph.getNeighbors(i);
        for (int vert : neighbors) {
            if (vert != i) {
                int previousValue = aux[vert];
                aux[vert] = aux[vert] + NEIGHBOOR_COUNT_INCLUDED;
                if (previousValue < INCLUDED && aux[vert] >= INCLUDED) {
                    includeVertex(graph, fecho, aux, vert);
                }
            }
        }
    }

    public String getTypeProblem() {
        return type;
    }

    public String getName() {
        return description;
    }

    private int countHs(UndirectedSparseGraphTO<Integer, Integer> graph, int[] currentSet) {
        int[] aux = new int[graph.getVertexCount()];
        int cont = 0;
        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
        }
        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        for (Integer v : currentSet) {
            mustBeIncluded.add(v);
            aux[v] = INCLUDED;
        }
        while (!mustBeIncluded.isEmpty()) {
            Integer verti = mustBeIncluded.remove();
            cont++;
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
        }
        return cont;
    }
}
