package com.github.braully.graph.generator;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GraphGeneratorRandom extends AbstractGraphGenerator {
    
    static final String N_VERTICES = "Nº Vertices";
    static final String MIN_DEGREE = "Min Degree";
    static final String MAX_DEGREE = "Max Degree";
    
    static final Integer DEFAULT_NVERTICES = 5;
    static final Integer DEFAULT_MIN_DEGREE = 1;
    static final Double DEFAULT_MAX_DEGREE = 1.2;
    
    static final String[] parameters = {N_VERTICES, MIN_DEGREE, MAX_DEGREE};
    static final String description = "Random";
    
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
        Integer minDegree = getIntegerParameter(parameters, MIN_DEGREE);
        Double maxDegree = getDoubleParameter(parameters, MAX_DEGREE);
        
        if (nvertices == null) {
            nvertices = DEFAULT_NVERTICES;
        }
        if (minDegree == null) {
            minDegree = DEFAULT_MIN_DEGREE;
        }
        if (maxDegree == null) {
            maxDegree = DEFAULT_MAX_DEGREE;
        }
        
        UndirectedSparseGraphTO<Integer, Integer> graph = new UndirectedSparseGraphTO<>();
        graph.setName("Random-n" + nvertices + "-min" + minDegree + "-max" + maxDegree);
        
        List<Integer> vertexElegibles = new ArrayList<>(nvertices);
        Integer[] vertexs = new Integer[nvertices];
        int[] degree = new int[nvertices];
        for (int i = 0; i < nvertices; i++) {
            vertexElegibles.add(i);
            vertexs[i] = i;
            degree[i] = 0;
            graph.addVertex(vertexs[i]);
        }
        int countEdge = 0;
        double offset = maxDegree - minDegree;
        for (int i = nvertices - 1; i > 0; i--) {
            long limite = minDegree + Math.round(Math.random() * (offset));
            int size = vertexElegibles.size();
            Integer source = vertexs[i];
            for (int j = 0; j <= limite; j++) {
                //Exclude last element from choose (no loop)
                Integer target = null;
                if (vertexElegibles.size() > 1) {
                    int vrandom = (int) Math.round(Math.random() * (size - 2));
                    target = vertexElegibles.get(vrandom);
                    if (graph.addEdge(countEdge++, source, target)) {
                        if (degree[target]++ >= maxDegree) {
                            vertexElegibles.remove(target);
                        }
                        if (degree[source]++ >= maxDegree) {
                            vertexElegibles.remove(source);
                        }
                    }
                    size = vertexElegibles.size();
                } else {
                    int vrandom = (int) Math.round(Math.random() * (nvertices - 1));
                    target = vertexs[vrandom];
                    graph.addEdge(countEdge++, source, target);
                }
            }
        }
        return graph;
    }
    
}
