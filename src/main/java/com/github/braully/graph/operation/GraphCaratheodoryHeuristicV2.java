package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import static com.github.braully.graph.operation.GraphAllCaratheodoryExistsSetOfSize.log;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GraphCaratheodoryHeuristicV2
        extends GraphCaratheodoryHeuristic {

    static final String description = "Nº Caratheodory (Heuristic v2)";
    static final boolean expand = false;

    @Override
    public String getName() {
        return description;
    }

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        Map<String, Object> result = super.doOperation(graphRead);
        if (expand) {
            GraphCaratheodoryExpandSet expand = new GraphCaratheodoryExpandSet();
            Collection initialSet = (Collection) result.get(OperationConvexityGraphResult.PARAM_NAME_CARATHEODORY_SET);
            graphRead.setSet(initialSet);
            Map<String, Object> doOperation = expand.doOperation(graphRead);
            if (GraphCaratheodoryHeuristic.verbose) {
                log.info("Initial Caratheodory Set: " + initialSet);
            }
            Set<Integer> maxCarat = (Set<Integer>) doOperation.get("Max Caratheodory Superset");
            if (maxCarat != null && !maxCarat.isEmpty()) {
                OperationConvexityGraphResult hsp3 = hsp3(graphRead, maxCarat);
                result.putAll(hsp3.toMap());
            }
        }
        return result;
    }

    @Override
    public Integer selectBestPromotableVertice(Set<Integer> s,
            Integer partial, Set<Integer> promotable,
            UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux) {
        Integer bestVertex = null;
        Integer bestRanking = null;
        int vertexCount = graph.getVertexCount();
        if (verbose) {
            System.out.print("Aux        ");
            printArrayAux(aux);

            System.out.print(String.format("bfs(%2d)    ", partial));
            System.out.print(" = {");
            for (int i = 0; i < vertexCount; i++) {
                int distance = bdl.getDistance(graph, i);
                System.out.printf("%2d | ", distance);
            }
            System.out.println("}");
        }

        for (Integer vtmp : promotable) {
            Collection neighbors = new HashSet(graph.getNeighbors(vtmp));
            neighbors.removeAll(s);
            neighbors.remove(partial);

            for (int i = 0; i < aux.length; i++) {
                if (aux[i] >= INCLUDED) {
                    neighbors.remove(i);
                }
            }
            Integer vtmpRanking = neighbors.size();
            if (bestVertex == null || (vtmpRanking >= 2 && vtmpRanking < bestRanking)) {
                bestRanking = vtmpRanking;
                bestVertex = vtmp;
            }
        }
        return bestVertex;
    }

    @Override
    public Integer selectBestNeighbor(Integer v, UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux, Integer partial, int[] auxBackup) {
        Integer ret = null;
        int vertexCount = graph.getVertexCount();

        if (verbose) {
            System.out.print("Aux        ");
            printArrayAux(aux);

            System.out.print(String.format("bfs(%2d)    ", v));
            System.out.print(" = {");
            for (int i = 0; i < vertexCount; i++) {
                int distance = bdl.getDistance(graph, i);
                System.out.printf("%2d | ", distance);
            }
            System.out.println("}");

            if (auxBackup != null) {
                System.out.print("AuxVp      ");
                printArrayAux(auxBackup);
            }
        }

        Set<Integer> neighbors = new HashSet<>(graph.getNeighbors(v));

        if (verbose) {
            System.out.println("\t* Vertices Neibhbors from " + v + " " + neighbors);
        }

        neighbors.remove(partial);
        neighbors.remove(v);
        Integer ranking = null;
        for (int i = 0; i < aux.length; i++) {
            if (aux[i] >= 2 || (auxBackup != null && auxBackup[i] >= 2)) {
                neighbors.remove(i);
            }
        }

        if (verbose) {
            System.out.println("\t* Vertices Elegibles Neibhbors from " + v + " " + neighbors);
        }

        for (Integer nei : neighbors) {
            int bfsP = bdl.getDistance(graph, nei);
            int neighborCount = graph.getNeighborCount(nei);
            int auxv = aux[nei];
            int deltaHs = deltaHs(nei, v, partial, graph, aux);
            if (verbose) {
                System.out.print("\t\t- Vertice " + nei);
                System.out.print(" bfsFrom(" + partial + ")=" + bfsP);
                System.out.print(" d(" + nei + ")=" + neighborCount);
                System.out.print(" aux[" + nei + "]=" + auxv);
                System.out.println(" deltaHs=" + deltaHs);
            }

//            int neiRanking = aux[nei] * 100 + graph.degree(nei);
            int neiRanking = calcRanking(deltaHs, neighborCount, bfsP, auxv);
            if (ret == null || neiRanking < ranking) {
                ret = nei;
                ranking = neiRanking;
            }
        }
        return ret;
    }

    @Override
    void beforeVerticePromotion(UndirectedSparseGraphTO<Integer, Integer> graph, Integer vp, Integer v, int[] aux) {
        bdl.labelDistances(graph, vp);
    }

    int deltaHs(Integer nei, Integer v, Integer parcial,
            UndirectedSparseGraphTO<Integer, Integer> graph, int[] aux) {
        int ret = 0;
        int[] auxbackup = new int[aux.length];
        copyArray(auxbackup, aux);
        addVertToS(nei, null, graph, auxbackup);
        for (int i = 0; i < auxbackup.length; i++) {
            if (!v.equals(i) && !parcial.equals(i)) {
                ret += auxbackup[i] - aux[i];
            }
        }
        return ret;
    }

    int calcRanking(int deltaHs, int neighborCount, int bfsP, int auxv) {
        int ranking = 0;
        ranking = deltaHs * 1 + neighborCount * 0 + bfsP * 0 + auxv * 0;
        return ranking;
    }

//    @Override
//    int calcRanking(int deltaHs, int neighborCount, int bfsP, int auxv) {
//        int ranking = 0;
//        ranking = (int) (deltaHs * 1 + neighborCount * 0.5 + bfsP * 0.3 + auxv * 0.2);
//        return ranking;
//    }
}
