package com.github.braully.graph.operation;

import com.github.braully.graph.GraphWS;
import com.github.braully.graph.UndirectedSparseGraphTO;
import static com.github.braully.graph.operation.GraphCaratheodoryCheckSet.PROCESSED;
import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.log4j.Logger;

public class GraphHullNumber implements IGraphOperation {

    protected boolean verbose = true;

    static final String type = "P3-Convexity";
    static final String description = "Hull Number (Java)";

    private static final Logger log = Logger.getLogger(GraphWS.class);

    public static GraphHullNumberHeuristicV1 heuristic = new GraphHullNumberHeuristicV1();

    public static final String PARAM_NAME_HULL_NUMBER = "number";
    public static final String PARAM_NAME_HULL_SET = "set";
    public static final String PARAM_NAME_SERIAL_TIME = "serial";
    public static final String PARAM_NAME_PARALLEL_TIME = "parallel";
    public static final String COMMAND_GRAPH_HN = com.github.braully.graph.DatabaseFacade.DATABASE_DIRECTORY + "Workspace/pesquisa/graph-hull-number-parallel/graph-test/";
    public final int INCLUDED = 2;
    public final int NEIGHBOOR_COUNT_INCLUDED = 1;

    Integer sizeStart = null;

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Integer hullNumber = -1;
        Set<Integer> minHullSet = null;
        sizeStart = null;
        try {
            String inputData = graph.getInputData();
            sizeStart = Integer.parseInt(inputData);
        } catch (Exception e) {

        }

        try {
            minHullSet = calcMinHullNumberGraph(graph);
            if (minHullSet != null && !minHullSet.isEmpty()) {
                hullNumber = minHullSet.size();
            }
        } catch (Exception ex) {
            log.error(null, ex);
        }

        /* Processar a buscar pelo hullset e hullnumber */
        Map<String, Object> response = new HashMap<>();
        response.put(PARAM_NAME_HULL_NUMBER, hullNumber);
        response.put(PARAM_NAME_HULL_SET, minHullSet);
        response.put(IGraphOperation.DEFAULT_PARAM_NAME_RESULT, hullNumber);
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
            Collection<Integer> neighbors = graph.getNeighborsUnprotected(verti);
            for (Integer vertn : neighbors) {
                if (vertn.equals(verti)) {
                    continue;
                }
                if (!vertn.equals(verti) && ++aux[vertn] == INCLUDED) {
                    mustBeIncluded.add(vertn);
                }
            }
            countIncluded++;
        }
        return countIncluded;
    }

    public Set<Integer> calcMinHullNumberGraph(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Set<Integer> ceilling = calcCeillingHullNumberGraph(graph);
        Set<Integer> hullSet = ceilling;
        if (graph == null || graph.getVertexCount() == 0) {
            return ceilling;
        }
        int maxSizeSet = ceilling.size();
        int currentSize = 1;
        int countOneNeigh = 0;

        Collection<Integer> vertices = graph.getVertices();

        log.debug("Graph: " + graph.getName() + " n=" + vertices.size());

        for (Integer i : vertices) {
            if (graph.degree(i) == 1) {
                countOneNeigh++;
            }
        }
        currentSize = Math.max(currentSize, countOneNeigh);

        if (sizeStart != null) {
            log.info("Start size (input param):" + sizeStart);
            currentSize = sizeStart;
        }

        while (currentSize < maxSizeSet) {
            log.debug("trying size: " + currentSize);
            Set<Integer> hs = findHullSetBruteForce(graph, currentSize);
            if (hs != null && !hs.isEmpty()) {
                hullSet = hs;
                break;
            }
            log.debug("not found");
            currentSize++;
        }
        return hullSet;
    }

    private Set<Integer> calcCeillingHullNumberGraph(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Set<Integer> ceilling = new HashSet<>();
        if (graph != null) {
            Collection<Integer> vertices = heuristic.buildOptimizedHullSet(graph);

            if (vertices == null || vertices.isEmpty()) {
                vertices = graph.getVertices();
            }

            if (vertices != null) {
                ceilling.addAll(vertices);
            }
        }
        return ceilling;
    }

    public Set<Integer> findHullSetBruteForce(UndirectedSparseGraphTO<Integer, Integer> graph,
            int currentSetSize) {
        Set<Integer> hullSet = null;
        if (graph == null || graph.getVertexCount() <= 0) {
            return hullSet;
        }
        Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(graph.getVertexCount(), currentSetSize);
        while (combinationsIterator.hasNext()) {
            int[] currentSet = combinationsIterator.next();
            if (checkIfHullSet(graph, currentSet)) {
                hullSet = new HashSet<>(currentSetSize);
                for (int i : currentSet) {
                    hullSet.add(graph.verticeByIndex(i));
                }
                break;
            }
        }
        return hullSet;
    }

    public boolean checkIfHullSet(UndirectedSparseGraphTO<Integer, Integer> graph,
            Integer... currentSet) {
        if (currentSet == null || currentSet.length == 0) {
            return false;
        }
        Set<Integer> fecho = new HashSet<>();
        int[] aux = new int[(Integer) graph.maxVertex() + 1];
        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
        }

        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        for (Integer iv : currentSet) {
            Integer v = graph.verticeByIndex(iv);
            mustBeIncluded.add(v);
            aux[v] = INCLUDED;
        }
        while (!mustBeIncluded.isEmpty()) {
            Integer verti = mustBeIncluded.remove();
            fecho.add(verti);
            Collection<Integer> neighbors = graph.getNeighborsUnprotected(verti);
            for (Integer vertn : neighbors) {
                if (vertn.equals(verti)) {
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

    public boolean checkIfHullSet(UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] currentSet) {
        if (currentSet == null || currentSet.length == 0) {
            return false;
        }
        Set<Integer> fecho = new HashSet<>();
        int[] aux = new int[(Integer) graph.maxVertex() + 1];
        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
        }

        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        for (Integer iv : currentSet) {
            Integer v = graph.verticeByIndex(iv);
            mustBeIncluded.add(v);
            aux[v] = INCLUDED;
        }
        while (!mustBeIncluded.isEmpty()) {
            Integer verti = mustBeIncluded.remove();
            fecho.add(verti);
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

    public void setVerbose(boolean b) {
        verbose = b;
    }

    public String getTypeProblem() {
        return type;
    }

    public String getName() {
        return description;
    }

    public Map<Integer, Integer> numConnectedComponents(UndirectedSparseGraphTO<Integer, Integer> graph) {
        int ret = 0;
        Map<Integer, Integer> map = new TreeMap<>();
        BFSDistanceLabeler<Integer, Integer> bdl = new BFSDistanceLabeler<>();
        if (graph != null && graph.getVertexCount() > 0) {
            Collection<Integer> vertices = graph.getVertices();
            TreeSet<Integer> verts = new TreeSet<>(vertices);
            while (!verts.isEmpty()) {
                Integer first = verts.first();
                bdl.labelDistances(graph, first);
                int contn = 1;
                for (Integer v : vertices) {
                    if (bdl.getDistance(graph, v) >= 0) {
                        verts.remove(v);
                        contn++;
                    }
                }
                ret++;

                map.put(ret, contn);
            }
        }
        return map;
    }

}
