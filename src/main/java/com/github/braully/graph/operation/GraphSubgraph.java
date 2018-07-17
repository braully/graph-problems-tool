package com.github.braully.graph.operation;

import com.github.braully.graph.GraphWS;
import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.algorithms.filters.Filter;
import edu.uci.ics.jung.algorithms.filters.KNeighborhoodFilter;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

public class GraphSubgraph implements IGraphOperation {

    static final String type = "Graph Class";
    static final String description = "Subgraph";

    private static final Logger log = Logger.getLogger(GraphWS.class);

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Integer khop = 0;
        Set<Integer> setN = new HashSet<>();
        setN.addAll(graph.getSet());

        /* Processar a buscar pelo hullset e hullnumber */
        Map<String, Object> response = new HashMap<>();

        Filter<Integer, Integer> filter = new KNeighborhoodFilter<Integer, Integer>(setN, khop, KNeighborhoodFilter.EdgeType.IN_OUT);
        Graph<Integer, Integer> newGraph = filter.apply(graph);

        UndirectedSparseGraphTO subgraph = new UndirectedSparseGraphTO();
        Collection<Integer> vertices = newGraph.getVertices();

        for (Integer v : vertices) {
            subgraph.addVertex(v);
        }
        Collection<Integer> edges = newGraph.getEdges();

        for (Integer e : edges) {
            Pair endpoints = newGraph.getEndpoints(e);
            Integer first = (Integer) endpoints.getFirst();
            Integer second = (Integer) endpoints.getSecond();
            subgraph.addEdge(first, second);
        }

        try {
            response.put("ES-Subgraph", subgraph.getEdgeString());
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
