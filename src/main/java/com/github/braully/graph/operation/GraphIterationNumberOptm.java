/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import static com.github.braully.graph.operation.GraphCaratheodoryCheckSet.PROCESSED;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.math3.util.CombinatoricsUtils;

/**
 *
 * @author Braully Rocha da Silva
 */
public class GraphIterationNumberOptm extends GraphHullNumberOptm {

    static final String type = "P3-Convexity";
    static final String description = "Iteration Number";

    public static String PARAM_NAME_ITERATION_NUMBER = "Iteration number";
    public static String PARAM_NAME_SET = "Set";

    public int lastResult = 0;

    public String getTypeProblem() {
        return type;
    }

    public String getName() {
        return description;
    }

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Map<String, Object> response = new HashMap<>();

        Set<Integer> firstMinHullSetGraph = this.findMinHullSetGraph(graph);
        int currentSize = firstMinHullSetGraph.size();

        //
        int maxNumberOfIterations = 0;
        int currentSetSize = currentSize;
        Set<Integer> maxSet = new HashSet<>();
        Map<Integer, Integer> maxIntervalOperation = null;
        if (graph == null || graph.getVertexCount() <= 0) {
            return response;
        }
        Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(graph.getVertexCount(), currentSetSize);
        while (combinationsIterator.hasNext()) {
            int[] currentSet = combinationsIterator.next();
            if (checkIfHullSet(graph, currentSet)) {
                Map<Integer, Integer> intervalOperation = intervalOperation(graph, currentSet);
                Integer maxIteracoes = Collections.max(intervalOperation.values());
                if (maxIteracoes != null && maxIteracoes > maxNumberOfIterations) {
                    maxNumberOfIterations = maxIteracoes;
                    maxIntervalOperation = intervalOperation;
                    maxSet.clear();
                    for (int i : currentSet) {
                        maxSet.add(i);
                    }
                }
            }
        }

        response.put(PARAM_NAME_ITERATION_NUMBER, maxNumberOfIterations);
        response.put(PARAM_NAME_SET, maxSet);
        response.put("Vertice:Iteracao", new TreeMap<>(maxIntervalOperation));
        response.put(IGraphOperation.DEFAULT_PARAM_NAME_RESULT, maxNumberOfIterations);
        lastResult = maxNumberOfIterations;

        return response;

    }

    public Map<Integer, Integer> intervalOperation(UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] currentSet) {
        Map<Integer, Integer> vIncludeIteracao = new HashMap<>();
        if (currentSet == null || currentSet.length == 0) {
            return vIncludeIteracao;
        }
        int[] aux = new int[graph.getVertexCount()];
        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
        }

        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        Queue<Integer> mustBeIncluded2 = new ArrayDeque<>();
        int iteracao = 0;

        for (Integer v : currentSet) {
            mustBeIncluded2.add(v);
            aux[v] = INCLUDED;
        }

        while (!mustBeIncluded2.isEmpty()) {
            Integer verti = mustBeIncluded2.remove();
            if (aux[verti] == PROCESSED) {
                continue;
            }
            vIncludeIteracao.put(verti, iteracao);
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
            if (mustBeIncluded2.isEmpty() && !mustBeIncluded.isEmpty()) {
                mustBeIncluded2.addAll(mustBeIncluded);
                mustBeIncluded.clear();
                //System.out.println();
                iteracao++;
                //System.out.println("Iteracao-" + iteracao + ": ");
            }
            aux[verti] = PROCESSED;
        }
        return vIncludeIteracao;
    }
}
