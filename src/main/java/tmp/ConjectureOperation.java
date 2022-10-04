/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import static com.github.braully.graph.operation.GraphHullNumber.PARAM_NAME_HULL_NUMBER;
import static com.github.braully.graph.operation.GraphHullNumber.PARAM_NAME_HULL_SET;
import com.github.braully.graph.operation.GraphHullNumberOptm;
import com.github.braully.graph.operation.GraphStatistics;
import com.github.braully.graph.operation.IGraphOperation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author strike
 */
public class ConjectureOperation extends GraphHullNumberOptm implements IGraphOperation {

    static final String description = "Perfect Conjecture";

    @Override
    public String getName() {
        return description;
    }

    GraphStatistics statistics = new GraphStatistics();

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Set<Integer> minHullSet = findMinHullSetGraph(graph);
        int diameter = (int) statistics.diameter(graph);
        Map<String, Object> response = new HashMap<>();
        response.put("Diameter", diameter);
        response.put(PARAM_NAME_HULL_SET, minHullSet);
        boolean ok = minHullSet.size() == diameter;
        response.put(IGraphOperation.DEFAULT_PARAM_NAME_RESULT, minHullSet.size() - diameter);
        return response;
    }
}
