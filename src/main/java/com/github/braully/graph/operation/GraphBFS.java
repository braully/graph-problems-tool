package com.github.braully.graph.operation;

import com.github.braully.graph.GraphWS;
import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.log4j.Logger;

public class GraphBFS implements IGraphOperation {

    static final String type = "General";
    static final String description = "B.F.S.";

    private static final Logger log = Logger.getLogger(GraphWS.class);

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        /* Processar a buscar pelo hullset e hullnumber */
        Map<String, Object> response = new HashMap<>();
        try {
            Set<Integer> roots = new HashSet<>();
            roots.addAll(graph.getSet());
            BFSDistanceLabeler<Integer, Integer> bdl = new BFSDistanceLabeler<>();
            bdl.labelDistances(graph, roots);
            int vertexCount = graph.getVertexCount();

            TreeMap<String, TreeSet<Integer>> tset = new TreeMap<>();

            for (Integer i = 0; i < vertexCount; i++) {
                Integer v = graph.verticeByIndex(i);
                String strDist = "" + bdl.getDistance(graph, v);
                tset.putIfAbsent(strDist, new TreeSet<>());
                tset.get(strDist).add(v);
            }
            response.putAll(tset);

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
