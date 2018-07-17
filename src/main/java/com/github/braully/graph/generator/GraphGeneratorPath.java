package com.github.braully.graph.generator;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GraphGeneratorPath extends AbstractGraphGenerator {
    
    static final String N_VERTICES = "Nº Vertices";
    static final String[] parameters = {N_VERTICES};
    static final String description = "Path";
    static final Integer DEFAULT_NVERTICES = 5;
    
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
        Integer nvertices = getIntegerParameter(parameters, N_VERTICES);
        
        if (nvertices == null) {
            nvertices = DEFAULT_NVERTICES;
        }
        
        UndirectedSparseGraphTO<Integer, Integer> graph = new UndirectedSparseGraphTO<>();
        graph.setName("P" + N_VERTICES);
        List<Integer> vertexElegibles = new ArrayList<>(nvertices);
        Integer[] vertexs = new Integer[nvertices];
        for (int i = 0; i < nvertices; i++) {
            vertexElegibles.add(i);
            vertexs[i] = i;
            graph.addVertex(vertexs[i]);
        }
        int countEdge = 0;
        for (int i = 0; i < nvertices - 1; i++) {
            Integer source = vertexs[i];
            Integer target = vertexs[i] + 1;
            graph.addEdge(countEdge++, source, target);
        }
        return graph;
    }
}
