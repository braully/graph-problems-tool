package com.github.braully.graph.generator;

import com.github.braully.graph.CombinationsFacade;
import com.github.braully.graph.UndirectedSparseGraphTO;

import static com.github.braully.graph.generator.GraphGeneratorCompleteBipartite.P_VERTICES;
import static com.github.braully.graph.generator.GraphGeneratorKP.K_VERTICES;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class GraphNKIndexedGenerator extends AbstractGraphGenerator {

    static final Logger log = Logger.getLogger(GraphNKIndexedGenerator.class);

    static final String N_VERTICES = "N,M,Index";
    static final String[] parameters = {K_VERTICES};
    static final String description = "N,M-Indexed";
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
        String nmindexcode = getStringParameter(parameters, N_VERTICES);

        UndirectedSparseGraphTO<Integer, Integer> graph = null;
        try {
            String[] split = nmindexcode.split(",");

            Integer nvertices = Integer.parseInt(split[0]);
            Integer pvertices = Integer.parseInt(split[1]);
            Long nkindex = Long.parseLong(split[2]);

            graph = new UndirectedSparseGraphTO<>();
            String name = "N" + nvertices + ",M" + pvertices + "-Indexed" + nkindex;
            graph.setName(name);
            List<Integer> vertexElegibles = new ArrayList<>(nvertices);
            Integer[] vertexs = new Integer[nvertices];

            for (int i = 0; i < nvertices; i++) {
                vertexElegibles.add(i);
                vertexs[i] = i;
                graph.addVertex(vertexs[i]);
            }

            int maxEdges = (nvertices * (nvertices - 1)) / 2;
            int[] combination = CombinationsFacade.getCombinationNKByLexicographIndex(maxEdges, pvertices, nkindex);

            System.out.println(name);

            System.out.printf("Comb = {");
            for (int i = 0; i < combination.length; i++) {
                System.out.printf("%d", combination[i]);
                if (i < combination.length - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println("}");

//        log.info("Max Edges: " + maxEdges);
            if (combination != null && combination.length > 0) {
                Set<Integer> edges = new HashSet<Integer>();
                for (int e : combination) {
                    edges.add(e);
                }
                log.info("Edges Combinations: " + edges);

                int countEdge = 0;
                for (int i = 0; i < nvertices; i++) {
                    for (int j = i; j < nvertices - 1; j++) {
                        if (edges.contains(countEdge)) {
                            Integer source = vertexs[i];
                            Integer target = vertexs[j] + 1;
                            graph.addEdge(source, target);
                        }
                        countEdge++;
                    }
                }
            }

            log.info("Graph: " + name);
        } catch (Exception e) {
            log.info("Error: " + e.getLocalizedMessage());
        }
        return graph;
    }
}
