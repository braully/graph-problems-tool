package com.github.braully.graph.operation;

import com.github.braully.graph.GraphWS;
import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

public class GraphNeighborhoodSet implements IGraphOperation {

    static final String type = "Graph Class";
    static final String description = "N(S)";

    private static final Logger log = Logger.getLogger(GraphWS.class);

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Collection<Integer> set = graph.getSet();
        List<Integer> setx = new ArrayList<>();
        List<Integer> setN = new ArrayList<>();
        List<Integer> setV = new ArrayList<>();
        List<Integer> intersection = new ArrayList<>();

        for (Integer v : set) {
            setN.addAll(graph.getNeighbors(v));
            setx.add(v);
        }

        intersection.addAll(graph.getNeighbors(setx.get(0)));
        for (int i = 1; i < setx.size(); i++) {
            intersection.retainAll(graph.getNeighbors(setx.get(i)));
        }

        setV.addAll(graph.getVertices());
        setV.removeAll(setN);

        /* Processar a buscar pelo hullset e hullnumber */
        Map<String, Object> response = new HashMap<>();

        try {
            Collections.sort(setN);
            Collections.sort(intersection);
            Collections.sort(setV);
            response.put("N" + set + "|" + setN.size() + "|", setN);
            response.put("∩|" + intersection.size() + "|", intersection);
            response.put("V-N|" + setV.size() + "|", setV);
        } catch (Exception ex) {
            log.error(null, ex);
        }
        return response;
    }

    public String getTypeProblem() {
        return type;
    }

    public String getName() {
        return description;
    }
}
