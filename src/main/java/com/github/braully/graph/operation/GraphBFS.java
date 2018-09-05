package com.github.braully.graph.operation;

import com.github.braully.graph.GraphWS;
import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

            for (Integer i = 0; i < vertexCount; i++) {
                int distance = bdl.getDistance(graph, i);
                response.put(i.toString(), distance);
            }
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
