package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author braully
 */
public class GraphDensity implements IGraphOperation {

    /*
    
     */
    static final String type = "General";
    static final String description = "Density";

    public String getTypeProblem() {
        return type;
    }

    public String getName() {
        return description;
    }

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        double d = 0.0;

        try {
            double m = graph.getEdgeCount();
            double n = graph.getVertexCount();
            d = (2 * m) / (n * (n - 1));
        } catch (Exception e) {

        }

        /* Processar a buscar pelo hullset e hullnumber */
        Map<String, Object> response = new HashMap<>();
        response.put(IGraphOperation.DEFAULT_PARAM_NAME_RESULT, d);
        return response;
    }

}
