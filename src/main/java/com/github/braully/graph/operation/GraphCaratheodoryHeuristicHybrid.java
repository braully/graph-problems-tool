package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.Set;
import org.apache.log4j.Logger;

public class GraphCaratheodoryHeuristicHybrid
        extends GraphCaratheodoryHeuristicV2 {
    
    private static final Logger log = Logger.getLogger(GraphCaratheodoryHeuristicHybrid.class);
    
    static final String description = "Nº Caratheodory (Heuristic vHybrid)";
    
    GraphCaratheodoryHeuristicV3 caratheodoryHeuristicV3 = new GraphCaratheodoryHeuristicV3();
    
    @Override
    void beforeReturnSFind(UndirectedSparseGraphTO<Integer, Integer> graph, Set<Integer> s, int[] aux) {
        caratheodoryHeuristicV3.expandCaratheodorySet(graph, s, aux);
    }
    
    @Override
    public String getName() {
        return description;
    }
}
