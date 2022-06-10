package com.github.braully.graph.operation;

import com.github.braully.graph.GraphWS;
import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.log4j.Logger;

public class GraphSubgraph implements IGraphOperation {

    static final String type = "General";
    static final String description = "Subgraph";

    private static final Logger log = Logger.getLogger(GraphWS.class);

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Set<Integer> setN = new HashSet<>();
        setN.addAll(graph.getSet());

        /* Processar a buscar pelo hullset e hullnumber */
        Map<String, Object> response = new HashMap<>();
        UndirectedSparseGraphTO subgraph = subGraphInduced(graph, setN);

        try {
            response.put("Subgraph", "N" + subgraph.getVertexCount() + ",M" + subgraph.getEdgeCount());
            response.put("ES-Subgraph", subgraph.getEdgeString());
        } catch (Exception ex) {
            log.error(null, ex);
        }
        return response;
    }

    public UndirectedSparseGraphTO subGraphInduced(UndirectedSparseGraphTO graph, Set<Integer> setN) {
        UndirectedSparseGraphTO subgraph = new UndirectedSparseGraphTO();

        for (Integer v : setN) {
            subgraph.addVertex(v);
        }

        for (Integer v : setN) {
            TreeSet<Integer> nvs = new TreeSet<>(graph.getNeighborsUnprotected(v));
            nvs.retainAll(setN);
            for (Integer t : nvs) {
                subgraph.addEdge(v, t);
            }
        }
        return subgraph;
    }

    public String getTypeProblem() {
        return type;
    }

    public String getName() {
        return description;
    }
}
