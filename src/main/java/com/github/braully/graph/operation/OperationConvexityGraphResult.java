/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph.operation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Braully Rocha da Silva
 */
public class OperationConvexityGraphResult {

    public static final String PARAM_NAME_CARATHEODORY_NUMBER = "Caratheodroy number(c)";
    public static final String PARAM_NAME_CARATHEODORY_SET = "Caratheodroy set(S)";
    public static final String PARAM_NAME_CONVEX_HULL = "Convex hull(H(S))";
    public static final String PARAM_NAME_TOTAL_TIME_MS = "Time(s)";
    public static final String PARAM_NAME_PARTIAL_DERIVATED = "∂H(S)=H(S)\\⋃p∈SH(S\\{p})";
    public static final String PARAM_NAME_INCLUDED_SEQUENCE = "Included sequence";

    Set<Integer> caratheodorySet;
    public Set<Integer> convexHull;
    int[] auxProcessor;
    Set<Integer> partial;
    List<Integer> includedSequence;
    long totalTimeMillis;
    Integer caratheodoryNumber;
    Set<Integer> vizs;
    Set<Integer> verticesPotenciais;
    int iteracoes;

    Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();

        if (caratheodoryNumber != null && caratheodoryNumber > 0) {
            result.put(PARAM_NAME_CARATHEODORY_NUMBER, caratheodoryNumber);
            result.put(IGraphOperation.DEFAULT_PARAM_NAME_RESULT, caratheodoryNumber);
        } else {
            result.put(PARAM_NAME_CARATHEODORY_NUMBER, -1);
            result.put(IGraphOperation.DEFAULT_PARAM_NAME_RESULT, -1);
        }

        if (caratheodorySet != null && !caratheodorySet.isEmpty()) {
            result.put(PARAM_NAME_CARATHEODORY_SET, caratheodorySet);
        } else {
            result.put(PARAM_NAME_CARATHEODORY_SET, "∄");
        }

        result.put(PARAM_NAME_CONVEX_HULL, convexHull);

        if (totalTimeMillis > 0) {
            result.put(PARAM_NAME_TOTAL_TIME_MS, (double) ((double) totalTimeMillis / 1000));
        }

        if (partial != null && !partial.isEmpty()) {
            result.put(PARAM_NAME_PARTIAL_DERIVATED, partial);
        }
        return result;
    }
}
