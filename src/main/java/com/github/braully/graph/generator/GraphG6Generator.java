package com.github.braully.graph.generator;

import com.github.braully.graph.CombinationsFacade;
import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import com.github.braully.graph.operation.GraphConvertToNKIndex;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;


public class GraphG6Generator extends AbstractGraphGenerator {

    static final Logger log = Logger.getLogger(GraphG6Generator.class);

    static final String N_VERTICES = "G6-Code";
    static final String[] parameters = {N_VERTICES};
    static final String description = "G6 Code";

    @Override
    public String[] getParameters() {
        return parameters;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public UndirectedSparseGraphTO<Integer, Integer> generateGraph(Map parameters) {
        String g6code = getStringParameter(parameters, N_VERTICES);


        UndirectedSparseGraphTO<Integer, Integer> graph = null;
        try {
            graph = UtilGraph.loadGraphG6(g6code);
            String name = "G6" + graph.getVertexCount() + ",M" + graph.getEdgeCount();
            graph.setName(name);
            log.info("Graph: " + name);
            String s = GraphConvertToNKIndex.graphToNMIndexedCode(graph);
            System.out.println("n,m,indexed: " + s);
            log.info("n,m,indexed: " + s);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return graph;
    }
}
