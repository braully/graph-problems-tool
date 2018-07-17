package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;

public class GraphCaratheodoryHeuristicV3
        extends GraphCaratheodoryHeuristicV2 {

    private static final Logger log = Logger.getLogger(GraphCaratheodoryHeuristicV3.class);

    static final String description = "Nº Caratheodory (Heuristic v3)";

    @Override
    public Set<Integer> buildMaxCaratheodorySet(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        Set<Integer> caratheodorySet = new HashSet<>();
        Collection<Integer> vertices = graphRead.getVertices();
        for (Integer v : vertices) {
            if (GraphCaratheodoryHeuristic.verbose) {
//                log.info("Trying Start Vertice: " + v);
                log.info("Trying Start Vertice: " + v);
            }
            Set<Integer> tmp = buildCaratheodorySetFromStartVertice(graphRead, v);
            if (tmp != null && tmp.size() > caratheodorySet.size()) {
                caratheodorySet = tmp;
            }
        }
        if (GraphCaratheodoryHeuristic.verbose) {
            log.info("Best S=" + caratheodorySet);
        }
        return caratheodorySet;
    }

    private Set<Integer> buildCaratheodorySetFromStartVertice(UndirectedSparseGraphTO<Integer, Integer> graph,
            Integer v) {
        Set<Integer> s = new HashSet<>();
        int vertexCount = graph.getVertexCount();
        int[] aux = new int[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            aux[i] = 0;
        }
        addVertToS(v, s, graph, aux);
        expandCaratheodorySet(graph, s, aux);
        return s;
    }

    public void expandCaratheodorySet(UndirectedSparseGraphTO<Integer, Integer> graph, 
            Set<Integer> s, int[] aux) {
        int vertexCount = graph.getVertexCount();
        int bv;
        do {
            bv = -1;
            int menorGrau = 0;
            int menorHs = 0;
            if (GraphCaratheodoryHeuristic.verbose) {
                log.info("\tAnalizing vertice: ");
            }
            for (int i = 0; i < vertexCount; i++) {
                if (s.contains(i)) {
                    continue;
                }
                if (GraphCaratheodoryHeuristic.verbose) {
                    log.info("\t\t" + i);
                }
                s.add(i);
                boolean isCarat = isCaratheodorySet(graph, s);
                if (isCarat) {
                    int[] auxb = aux.clone();
                    addVertToS(i, null, graph, auxb);
                    int sizeHs = countSizeHs(s, auxb);
                    int neighborCount = graph.getNeighborCount(i);
                    if (GraphCaratheodoryHeuristic.verbose) {
                        log.info("\t" + s + " = Charatheodory |H(S)|=" + sizeHs + " d=" + neighborCount);
                    }
                    if (bv == -1 || (sizeHs <= menorHs && neighborCount < menorGrau)) {
                        menorHs = sizeHs;
                        menorGrau = neighborCount;
                        bv = i;
                    }
                } 
//                else {
                    s.remove(i);
//                }
            }
            if (bv != -1) {
                addVertToS(bv, s, graph, aux);
                if (GraphCaratheodoryHeuristic.verbose) {
                    log.info("\tBest vert choice: " + bv);
                }
            } else if (GraphCaratheodoryHeuristic.verbose) {
                if (GraphCaratheodoryHeuristic.verbose) {
                    log.info("End Avaiable: S=" + s);
                }
            }
        } while (bv != -1);
    }

    @Override
    public String getName() {
        return description;
    }
}
