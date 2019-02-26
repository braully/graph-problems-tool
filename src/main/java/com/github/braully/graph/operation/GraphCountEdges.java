package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author braully
 */
public class GraphCountEdges implements IGraphOperation {

    public static final String PARAM_NAME_EDGE_COUNT = "Nº edges";
    /*
    
     */
    static final String type = "General";
    static final String description = "Edges Count";

    public String getTypeProblem() {
        return type;
    }

    public String getName() {
        return description;
    }

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Integer m = -1;

        try {
            m = graph.getEdgeCount();
        } catch (Exception e) {

        }

        /* Processar a buscar pelo hullset e hullnumber */
        Map<String, Object> response = new HashMap<>();
        response.put(PARAM_NAME_EDGE_COUNT, m);
        response.put(IGraphOperation.DEFAULT_PARAM_NAME_RESULT, m);
        return response;
    }

}
