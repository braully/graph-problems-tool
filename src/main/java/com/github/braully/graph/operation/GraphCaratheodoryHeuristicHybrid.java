package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import static com.github.braully.graph.operation.GraphCaratheodoryAllSetOfSize.log;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.apache.log4j.Logger;

public class GraphCaratheodoryHeuristicHybrid extends GraphCaratheodoryCheckSet
        implements IGraphOperation {

    private static final Logger log = Logger.getLogger(GraphCaratheodoryHeuristicHybrid.class);
    public static boolean verbose = false;
    static final boolean expand = false;

    static final String description = "Caratheodory No. Heuristic Mix";

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        Map<String, Object> result = super.doOperation(graphRead);
        if (expand) {
            GraphCaratheodoryExpandSet expand = new GraphCaratheodoryExpandSet();
            Collection initialSet = (Collection) result.get(OperationConvexityGraphResult.PARAM_NAME_CARATHEODORY_SET);
            graphRead.setSet(initialSet);
            Map<String, Object> doOperation = expand.doOperation(graphRead);
            if (verbose) {
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

    void beforeReturnSFind(UndirectedSparseGraphTO<Integer, Integer> graph, Set<Integer> s, int[] aux) {
        expandCaratheodorySet(graph, s, aux);
    }

    public Set<Integer> buildMaxCaratheodorySet(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        Set<Integer> caratheodorySet = new HashSet<>();
        Collection<Integer> vertices = graphRead.getVertices();
        for (Integer v : vertices) {
            if (verbose) {
                log.info("Trying Start Vertice: " + v);
            }
            Set<Integer> tmp = buildCaratheodorySetFromStartVertice(graphRead, v);
            if (tmp != null && tmp.size() > caratheodorySet.size()) {
                caratheodorySet = tmp;
            }
        }
        if (GraphCaratheodoryHeuristicHybrid.verbose) {
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
            if (verbose) {
                log.info("\tAnalizing vertice: ");
            }
            for (int i = 0; i < vertexCount; i++) {
                if (s.contains(i)) {
                    continue;
                }
                if (verbose) {
                    log.info("\t\t" + i);
                }
                s.add(i);
                boolean isCarat = isCaratheodorySet(graph, s);
                if (isCarat) {
                    int[] auxb = aux.clone();
                    addVertToS(i, null, graph, auxb);
                    int sizeHs = countSizeHs(s, auxb);
                    int neighborCount = graph.getNeighborCount(i);
                    if (verbose) {
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
                if (verbose) {
                    log.info("\tBest vert choice: " + bv);
                }
            } else if (verbose) {
                if (verbose) {
                    log.info("End Avaiable: S=" + s);
                }
            }
        } while (bv != -1);
    }

    public void addVertToS(Integer verti, Set<Integer> s,
            UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux) {

        if (verti == null || aux[verti] >= INCLUDED) {
            return;
        }

        aux[verti] = aux[verti] + INCLUDED;
        if (s != null) {
            s.add(verti);
        }

        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        mustBeIncluded.add(verti);
        while (!mustBeIncluded.isEmpty()) {
            verti = mustBeIncluded.remove();
            Collection<Integer> neighbors = graph.getNeighbors(verti);
            for (int vertn : neighbors) {
                if (vertn == verti) {
                    continue;
                }
                if (vertn != verti && ++aux[vertn] == INCLUDED) {
                    mustBeIncluded.add(vertn);
                }
            }
        }
    }

    public Integer selectBestNeighbor(Integer v, UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux, Integer partial, int[] auxBackup) {
        Integer ret = null;
        Set<Integer> neighbors = new HashSet<>(graph.getNeighbors(v));
        if (partial != null) {
            neighbors.remove(partial);
        }
        neighbors.remove(v);
        Integer ranking = null;
        for (int i = 0; i < aux.length; i++) {
            if (aux[i] >= 2 || (auxBackup != null && auxBackup[i] >= 2)) {
                neighbors.remove(i);
            }
        }

        for (Integer nei : neighbors) {
            int neiRanking = aux[nei] * 100 + graph.degree(nei);
            if (ret == null || neiRanking < ranking) {
                ret = nei;
                ranking = neiRanking;
            }
        }
        return ret;
    }

    public void removeVertFromS(Integer verti, Set<Integer> s,
            UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux) {

        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
        }
        s.remove(verti);
        for (Integer v : s) {
            addVertToS(v, s, graph, aux);
        }
    }

    public int countSizeHs(Set<Integer> s, int[] aux) {
        int cont = 0;
        if (aux == null) {
            return 0;
        }
        for (int i = 0; i < aux.length; i++) {
            if (aux[i] >= INCLUDED) {
                cont++;
            }
        }
        return cont;
    }

    @Override
    public String getName() {
        return description;
    }

    @Override
    public String getTypeProblem() {
        return type;
    }
}
