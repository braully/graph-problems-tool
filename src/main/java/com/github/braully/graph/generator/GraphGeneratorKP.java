package com.github.braully.graph.generator;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GraphGeneratorKP extends AbstractGraphGenerator {
    
    static final String K_VERTICES = "N";
    static final String P_VERTICES = "M";
    static final String[] parameters = {K_VERTICES, P_VERTICES};
    static final String description = "Kn*Pm";
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
        Integer nvertices = getIntegerParameter(parameters, K_VERTICES);
        Integer pvertices = getIntegerParameter(parameters, P_VERTICES);
        
        if (nvertices == null) {
            nvertices = DEFAULT_NVERTICES;
        }
        
        if (pvertices == null) {
            pvertices = DEFAULT_NVERTICES;
        }
        
        UndirectedSparseGraphTO<Integer, Integer> graph = new UndirectedSparseGraphTO<>();
        graph.setName("K" + nvertices + "P" + pvertices);
        
        Integer totalVertices = nvertices * pvertices;
        List<Integer> vertexElegibles = new ArrayList<>(totalVertices);
        Integer[] vertexs = new Integer[totalVertices];
        for (int i = 0; i < totalVertices; i++) {
            vertexElegibles.add(i);
            vertexs[i] = i;
            graph.addVertex(vertexs[i]);
        }
        int countEdge = 0;
        
        for (int p = 0; p < pvertices; p++) {
            for (int i = nvertices * p; i < nvertices * (1 + p); i++) {
                for (int j = i; j < nvertices * (1 + p) - 1; j++) {
                    Integer source = vertexs[i];
                    Integer target = vertexs[j] + 1;
                    graph.addEdge(countEdge++, source, target);
                }
                if (p > 0) {
                    for (int j = i; j < nvertices * (1 + p); j++) {
                        Integer source = vertexs[i];
                        Integer target2 = source - nvertices;
                        graph.addEdge(countEdge++, source, target2);
                    }
                }
            }
        }
        return graph;
    }
}
