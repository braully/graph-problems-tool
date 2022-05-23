package com.github.braully.graph.operation;

import com.github.braully.graph.GraphWS;
import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

public class GraphSubgraphNeighborHoodExcludeS extends GraphSubgraph implements IGraphOperation {

    static final String description = "Subgraph N(S)-S";

    private static final Logger log = Logger.getLogger(GraphWS.class);

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Set<Integer> setN = new HashSet<>();
        Set<Integer> setS = new HashSet<>();

        setS.addAll(graph.getSet());

        for (Integer v : setS) {
            setN.addAll(graph.getNeighborsUnprotected(v));
        }
        setN.removeAll(setS);

        /* Processar a buscar pelo hullset e hullnumber */
        Map<String, Object> response = new HashMap<>();
        UndirectedSparseGraphTO subgraph = subGraphInduced(graph, setN);

        try {
            response.put("ES-Subgraph", subgraph.getEdgeString());
        } catch (Exception ex) {
            log.error(null, ex);
        }
        return response;
    }

    public String getName() {
        return description;
    }
}
