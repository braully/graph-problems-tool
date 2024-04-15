package com.github.braully.graph.operation;

import com.github.braully.graph.GraphWS;
import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

public class GraphTmpDetect implements IGraphOperation {

    static final String type = "General";
    static final String description = "Tmp Detect";

    private static final Logger log = Logger.getLogger(GraphWS.class);

    GraphCaratheodoryNumberOptm caratop = new GraphCaratheodoryNumberOptm();
    GraphHullNumberOptm hullop = new GraphHullNumberOptm();
    GraphCaratheodoryAllSetOfSize allsizes = new GraphCaratheodoryAllSetOfSize();
    GraphStatistics stats = new GraphStatistics();

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        /* Processar a buscar pelo hullset e hullnumber */
        Map<String, Object> response = new HashMap<>();
        Integer size = null;

        try {
            String inputData = graph.getInputData();
            size = Integer.parseInt(inputData);
        } catch (Exception e) {

        }
        Set<Integer> findMinHullSetGraph = hullop.findMinHullSetGraph(graph);
        Integer hsize = findMinHullSetGraph.size();

        OperationConvexityGraphResult carat = caratop.findCaratheodroySetBruteForce(graph, hsize + 1);

        if (carat != null && carat.caratheodoryNumber != null && carat.caratheodoryNumber > hsize) {

            graph.setInputData("" + carat.caratheodoryNumber);
            Map<String, Object> doOperation = allsizes.doOperation(graph);
            Integer menorhs = (Integer) doOperation.get("Menor |H(S)");
            if (menorhs.intValue() < graph.getVertexCount()) {
                System.out.println("EUREKAAAAAA ************** ");
                System.out.println("ES: " + graph.getEdgeString());
                System.out.println("graph: " + graph.toResumedString());
                stats.basicstats(graph, doOperation);
                System.out.println(doOperation);
                response.put(IGraphOperation.DEFAULT_PARAM_NAME_RESULT, hsize);
            }
        }
        response.put("not found", 0);

//        response.put(IGraphOperation.DEFAULT_PARAM_NAME_RESULT, cycleSize);
        return response;
    }

    public String getTypeProblem() {
        return type;
    }

    public String getName() {
        return description;
    }

}
