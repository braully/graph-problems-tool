/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.math3.util.CombinatoricsUtils;

/**
 *
 * @author Braully Rocha da Silva
 */
public class GraphCaratheodoryNumberBinary extends GraphCaratheodoryCheckSet {

    static final String type = "P3-Convexity";
    static final String description = "Caratheodory Number";

    public static final int THRESHOLD_HEURISTIC_FEED = 15;

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Map<String, Object> result = null;
        if (graph == null) {
            return result;
        }
        OperationConvexityGraphResult processedCaratheodroySet = null;

        int vertexCount = graph.getVertexCount();
        int maxSizeSet = (vertexCount + 1) / 2;
        int currentSize = maxSizeSet;
        int left = 0;
        int rigth = maxSizeSet;
        result = new HashMap<>();

        if (vertexCount >= THRESHOLD_HEURISTIC_FEED) {
            GraphCaratheodoryHeuristicHybrid graphCaratheodoryHeuristicHybrid = new GraphCaratheodoryHeuristicHybrid();
            Set<Integer> caratheodorySet = graphCaratheodoryHeuristicHybrid.buildMaxCaratheodorySet(graph);
            if (caratheodorySet != null) {
                left = caratheodorySet.size() + 1;
                processedCaratheodroySet = hsp3(graph, caratheodorySet);
                result.putAll(processedCaratheodroySet.toMap());
            }
            processedCaratheodroySet = findCaratheodroySetBruteForce(graph, left);
            if (processedCaratheodroySet == null || processedCaratheodroySet.caratheodorySet == null) {
                return result;
            } else {
                result.clear();
                result.putAll(processedCaratheodroySet.toMap());
                left = left + 1;
            }
        }

        while (left <= rigth) {
            currentSize = (left + rigth) / 2;
            processedCaratheodroySet = findCaratheodroySetBruteForce(graph, currentSize);
            if (processedCaratheodroySet != null
                    && processedCaratheodroySet.caratheodorySet != null
                    && !processedCaratheodroySet.caratheodorySet.isEmpty()) {
                result.clear();
                result.putAll(processedCaratheodroySet.toMap());
                left = currentSize + 1;
            } else {
                rigth = currentSize - 1;
            }
        }
        return result;
    }

    public OperationConvexityGraphResult findCaratheodroySetBruteForce(UndirectedSparseGraphTO<Integer, Integer> graph, int currentSize) {
        OperationConvexityGraphResult processedHullSet = null;
        if (graph == null || graph.getVertexCount() <= 0) {
            return processedHullSet;
        }
        Collection vertices = graph.getVertices();
        int veticesCount = vertices.size();
        Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(graph.getVertexCount(), currentSize);
        while (combinationsIterator.hasNext()) {
            int[] currentSet = combinationsIterator.next();
            OperationConvexityGraphResult hsp3g = hsp3(graph, currentSet);
            if (hsp3g != null) {
                processedHullSet = hsp3g;
                break;
            }
        }
        return processedHullSet;
    }

    public String getTypeProblem() {
        return type;
    }

    public String getName() {
        return description;
    }
}
