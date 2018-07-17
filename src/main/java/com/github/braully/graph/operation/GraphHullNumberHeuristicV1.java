package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import static com.github.braully.graph.operation.GraphHullNumber.PARAM_NAME_HULL_NUMBER;
import static com.github.braully.graph.operation.GraphHullNumber.PARAM_NAME_HULL_SET;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

public class GraphHullNumberHeuristicV1
        extends GraphHullNumber implements IGraphOperation {

    private static final Logger log = Logger.getLogger(GraphHullNumberHeuristicV1.class);

    static final String description = "Hull Number (Heuristic v1)";

    @Override
    public String getName() {
        return description;
    }

    public GraphHullNumberHeuristicV1() {
    }

    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Integer hullNumber = -1;
        Set<Integer> minHullSet = null;

        try {
            minHullSet = buildOptimizedHullSet(graph);
            if (minHullSet != null && !minHullSet.isEmpty()) {
                hullNumber = minHullSet.size();
            }
        } catch (Exception ex) {
            log.error(null, ex);
        }

        /* Processar a buscar pelo hullset e hullnumber */
        Map<String, Object> response = new HashMap<>();
        response.put(PARAM_NAME_HULL_NUMBER, hullNumber);
        response.put(PARAM_NAME_HULL_SET, minHullSet);
        response.put(IGraphOperation.DEFAULT_PARAM_NAME_RESULT, hullNumber);
        return response;
    }

//    @Override
    public Set<Integer> buildOptimizedHullSet(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        Set<Integer> hullSet = null;
        Collection<Integer> vertices = graphRead.getVertices();
        for (Integer v : vertices) {
            if (GraphCaratheodoryHeuristic.verbose) {
                log.info("Trying Start Vertice: " + v);
            }
            Set<Integer> tmp = buildOptimizedHullSetFromStartVertice(graphRead, v);
            if (hullSet == null || tmp.size() < hullSet.size()) {
                hullSet = tmp;
            }
        }
        if (GraphCaratheodoryHeuristic.verbose) {
            log.info("Best S=" + hullSet);
        }
        return hullSet;
    }

    private Set<Integer> buildOptimizedHullSetFromStartVertice(UndirectedSparseGraphTO<Integer, Integer> graph,
            Integer v) {
        Set<Integer> s = new HashSet<>();
        int vertexCount = graph.getVertexCount();
        int[] aux = new int[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            aux[i] = 0;
        }
        int sizeHs = addVertToS(v, s, graph, aux);
        int bv;
        do {
            bv = -1;
            int maiorGrau = 0;
            int maiorDeltaHs = 0;
            if (GraphCaratheodoryHeuristic.verbose) {
                log.info("\tAnalizing vertice: ");
            }
            for (int i = 0; i < vertexCount; i++) {
                if (aux[i] >= INCLUDED) {
                    continue;
                }
                int[] auxb = aux.clone();
                int deltaHsi = addVertToS(i, null, graph, auxb);

                int neighborCount = 0;
                for (int j = 0; j < vertexCount; j++) {
                    if (auxb[j] == INCLUDED) {
                        neighborCount++;
                    }
                }

                if (GraphCaratheodoryHeuristic.verbose) {
                    log.info("\t" + s + " = Charatheodory |H(S)|=" + sizeHs + " d=" + neighborCount);
                }
                if (bv == -1 || (deltaHsi >= maiorDeltaHs && neighborCount > maiorGrau)) {
                    maiorDeltaHs = deltaHsi;
                    maiorGrau = neighborCount;
                    bv = i;
                }
            }
            if (GraphCaratheodoryHeuristic.verbose) {
                log.info("\tBest vert choice: " + bv);
            }
            sizeHs = sizeHs + addVertToS(bv, s, graph, aux);
        } while (sizeHs < vertexCount);
        return s;
    }

}
