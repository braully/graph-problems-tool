package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.log4j.Logger;

public class GraphHullNumberHeuristicV1
        extends GraphHullNumber implements IGraphOperation {

    public int K = 2;

    private static final Logger log = Logger.getLogger(GraphHullNumberHeuristicV1.class);

    static final String description = "Hull Number Heuristic";
    public Integer fatorLimite = null;

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
            minHullSet = findMinHullSetGraph(graph);
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

    public Set<Integer> findMinHullSetGraph(UndirectedSparseGraphTO<Integer, Integer> graph) {
        return buildOptimizedHullSet(graph);

    }

//    @Override
    public Set<Integer> buildOptimizedHullSet(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        List<Integer> vertices = new ArrayList<>((List<Integer>) graphRead.getVertices());
        Set<Integer> hullSet = null;
        Integer vl = null;
        Set<Integer> s = new HashSet<>();

        int vertexCount = graphRead.getVertexCount();
        int[] aux = new int[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            aux[i] = 0;
        }
        int sizeHs = 0;
        for (Integer v : vertices) {
            if (graphRead.degree(v) <= 1) {
                sizeHs = sizeHs + addVertToS(v, s, graphRead, aux);
            }
        }

        int total = graphRead.getVertexCount();
        int cont = 0;

        vertices.sort(Comparator
                .comparingInt((Integer v) -> -graphRead.degree(v))
                .thenComparing(v -> -v));
        int limite = vertexCount;
        if (fatorLimite != null) {
            limite = vertexCount / fatorLimite;
        }

        for (int idx = 0; idx < limite; idx++) {
            Integer v = vertices.get(idx);
            if (s.contains(v)) {
                continue;
            }
            if (verbose) {
                System.out.println("Trying ini vert: " + v);
//                UtilProccess.printCurrentItme();

            }
            Set<Integer> tmp = buildOptimizedHullSetFromStartVertice(graphRead, v, s, aux, sizeHs);
            tmp = tryMinimal(graphRead, tmp);
            if (hullSet == null) {
                hullSet = tmp;
                vl = v;
                if (verbose) {
                    System.out.println("Primeiro resultado v:" + v + " em: " + hullSet.size());
                }
            } else if (tmp.size() < hullSet.size()) {
                if (verbose) {
                    System.out.println("Melhorado em: " + (hullSet.size() - tmp.size()));
                    System.out.println(" em i " + v + " vindo de " + vl);
                    System.out.println("d(" + v + ")=" + graphRead.degree(v) + " d(" + vl + ")=" + graphRead.degree(vl));
                    System.out.println(hullSet);
                    System.out.println(tmp);
                }
                hullSet = tmp;
            }
            cont++;
            if (verbose) {
//                UtilProccess.printCurrentItmeAndEstimated(total - cont);
//                System.out.println(" s size: " + tmp.size());
            }
        }
        if (hullSet == null) {
            hullSet = new HashSet<>(s);
        }
        return hullSet;
    }

    protected Set<Integer> buildOptimizedHullSetFromStartVertice(UndirectedSparseGraphTO<Integer, Integer> graph,
            Integer v, Set<Integer> sini, int[] auxini, int sizeHsini) {
        Set<Integer> s = new HashSet<>(sini);
        int vertexCount = graph.getVertexCount();
        int[] aux = auxini.clone();
        int sizeHs = addVertToS(v, s, graph, aux) + sizeHsini;
        int bestVertice;
        do {
            bestVertice = -1;
            int maiorGrau = 0;
            int maiorDeltaHs = 0;
            int maiorContaminado = 0;

            for (int i = 0; i < vertexCount; i++) {
                //Se vertice já foi adicionado, ignorar
                if (aux[i] >= K) {
                    continue;
                }
                int[] auxb = aux.clone();
                int deltaHsi = addVertToS(i, null, graph, auxb);

                int neighborCount = 0;
                int contaminado = 0;
                //Contabilizar quantos vertices foram adicionados
                for (int j = 0; j < vertexCount; j++) {
                    if (auxb[j] >= K) {
                        neighborCount++;
                    }
                    if (auxb[j] > 0 && auxb[j] < K) {
                        contaminado++;
                    }
                }

                if (bestVertice == -1) {
                    maiorDeltaHs = deltaHsi;
                    maiorGrau = neighborCount;
                    maiorContaminado = contaminado;
                    bestVertice = i;
                } else if (deltaHsi > maiorDeltaHs) {
                    maiorDeltaHs = deltaHsi;
                    maiorGrau = neighborCount;
                    maiorContaminado = contaminado;
                    bestVertice = i;
                } else if (deltaHsi == maiorDeltaHs) {
                    if (neighborCount > maiorGrau) {
                        maiorDeltaHs = deltaHsi;
                        maiorGrau = neighborCount;
                        maiorContaminado = contaminado;
                        bestVertice = i;
                    } else if (neighborCount == maiorGrau) {
                        if (contaminado > maiorContaminado) {
                            maiorDeltaHs = deltaHsi;
                            maiorGrau = neighborCount;
                            maiorContaminado = contaminado;
                            bestVertice = i;
                        }
                    }
                }
            }
            if (bestVertice == -1) {
                break;
            }
            sizeHs = sizeHs + addVertToS(bestVertice, s, graph, aux);
        } while (sizeHs < vertexCount);
        return s;
    }

    public int addVertToS(Integer verti, Set<Integer> s,
            UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux) {
        int countIncluded = 0;
        if (verti == null || aux[verti] >= K) {
            return countIncluded;
        }

        aux[verti] = aux[verti] + K;
        if (s != null) {
            s.add(verti);
        }
        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        mustBeIncluded.add(verti);
        while (!mustBeIncluded.isEmpty()) {
            verti = mustBeIncluded.remove();
            Collection<Integer> neighbors = graph.getNeighborsUnprotected(verti);
            for (Integer vertn : neighbors) {
                if (vertn.equals(verti)) {
                    continue;
                }
                if (!vertn.equals(verti) && (++aux[vertn]) == K) {
                    mustBeIncluded.add(vertn);
                }
            }
            countIncluded++;
        }

        return countIncluded;
    }

    public Set<Integer> findHullSubSetBruteForce(
            UndirectedSparseGraphTO<Integer, Integer> graph,
            int currentSetSize, Integer... subset) {
        Set<Integer> hullSet = null;
        if (graph == null || graph.getVertexCount() <= 0) {
            return hullSet;
        }
        Set<Integer> verticeset = new HashSet<>(graph.getVertices());
        if (subset != null) {
            for (Integer v : subset) {
                verticeset.remove(v);
            }
        }
        List<Integer> vertices = new ArrayList<>(verticeset);
        Collections.sort(vertices);

        Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(vertices.size(), currentSetSize - subset.length);
        while (combinationsIterator.hasNext()) {
            int[] currentSet = combinationsIterator.next();
            if (checkIfHullSubSet(graph, vertices, currentSet, subset)) {
                hullSet = new LinkedHashSet<>();
                if (subset != null) {
                    for (Integer v : subset) {
                        hullSet.add(v);
                    }
                }
                for (int i : currentSet) {
                    hullSet.add(vertices.get(i));
                }
                break;
            }
        }
        return hullSet;
    }

    public Set<Integer> tryMinimal(UndirectedSparseGraphTO<Integer, Integer> graphRead, Set<Integer> tmp) {
        Set<Integer> s = tmp;
        if (verbose) {
            System.out.println("tentando reduzir: " + s.size());
        }

        for (Integer v : tmp) {
            if (graphRead.degree(v) < K) {
                continue;
            }
            Set<Integer> t = new LinkedHashSet<>(s);
            t.remove(v);
            if (checkIfHullSet(graphRead, t.toArray(new Integer[0]))) {
                s = t;
                if (verbose) {
                    System.out.println("Reduzido removido: " + v);
                }
            }
        }
        return s;
    }

    public boolean checkIfHullSet(UndirectedSparseGraphTO<Integer, Integer> graph,
            Integer... currentSet) {
        if (currentSet == null || currentSet.length == 0) {
            return false;
        }
        Set<Integer> fecho = new HashSet<>();
        int[] aux = new int[(Integer) graph.maxVertex() + 1];
        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
        }

        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        for (Integer iv : currentSet) {
            Integer v = iv;
            mustBeIncluded.add(v);
            aux[v] = K;
        }
        while (!mustBeIncluded.isEmpty()) {
            Integer verti = mustBeIncluded.remove();
            fecho.add(verti);
            Collection<Integer> neighbors = graph.getNeighborsUnprotected(verti);
            for (Integer vertn : neighbors) {
                if (vertn.equals(verti)) {
                    continue;
                }
                if (!vertn.equals(verti) && aux[vertn] <= K - 1) {
                    aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                    if (aux[vertn] == K) {
                        mustBeIncluded.add(vertn);
                    }
                }
            }
            aux[verti] += K;
        }
        return fecho.size() == graph.getVertexCount();
    }

    public Set<Integer> buildOptimizedHullSetTryMinimal(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        Set<Integer> optimizedHullSet = this.buildOptimizedHullSet(graphRead);
        Set<Integer> optimizedMinimalHullSet = tryMinimal(graphRead, optimizedHullSet);
        return optimizedMinimalHullSet;
    }

    public boolean checkIfHullSubSet(UndirectedSparseGraphTO<Integer, Integer> graph, List<Integer> vertices, int[] currentSet, Integer[] subset) {
        if (currentSet == null || currentSet.length == 0) {
            return false;
        }
        Set<Integer> fecho = new HashSet<>();
        int[] aux = new int[(Integer) graph.maxVertex() + 1];
        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
        }

        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        for (Integer iv : currentSet) {
            Integer v = vertices.get(iv);
            mustBeIncluded.add(v);
            aux[v] = K;
        }
        for (Integer iv : subset) {
            Integer v = iv;
            mustBeIncluded.add(v);
            aux[v] = K;
        }
        while (!mustBeIncluded.isEmpty()) {
            Integer verti = mustBeIncluded.remove();
            fecho.add(verti);
            Collection<Integer> neighbors = graph.getNeighborsUnprotected(verti);
            for (Integer vertn : neighbors) {
                if (vertn.equals(verti)) {
                    continue;
                }
                if (!vertn.equals(verti) && aux[vertn] <= K - 1) {
                    aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                    if (aux[vertn] == K) {
                        mustBeIncluded.add(vertn);
                    }
                }
            }
            aux[verti] += K;
        }
        return fecho.size() == graph.getVertexCount();
    }

    public static void main(String... args) throws IOException {
        GraphHullNumberHeuristicV1 op = new GraphHullNumberHeuristicV1();
        GraphHullNumberOptm op2 = new GraphHullNumberOptm();
        UndirectedSparseGraphTO<Integer, Integer> graph = null;
//        graph = new UndirectedSparseGraphTO("0-1,0-3,1-2,3-4,3-5,4-5,");
//        graph = UtilGraph.loadGraphG6("S??OOc_OAP?G@_?KQ?C????[?EPWgF??W");
//        graph = UtilGraph.loadGraphG6("Ss_?G?@???coH`CEABGR?AWDe?A_oAR??");
//        graph = UtilGraph.loadBigDatasetRaw(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/ca-HepPh/ca-HepPh.txt"));
//        graph = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/ca-HepPh/ca-HepPh.txt"));

//        graph = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/ca-CondMat/ca-CondMat.txt"));
//        graph = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/ca-GrQc/ca-GrQc.txt"));
//        graph = UtilGraph.loadBigDataset(
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/BlogCatalog3/nodes.csv"),
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/BlogCatalog3/edges.csv"));
//     
//        graph = UtilGraph.loadBigDataset(
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/Delicious/nodes.csv"),
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/Delicious/edges.csv"));
        graph = UtilGraph.loadBigDataset(
                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/Douban/nodes.csv"),
                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/Douban/edges.csv"));

        System.out.println(graph.getName());
        System.out.println(graph.toResumedString());
        System.out.println(op.numConnectedComponents(graph));
//        op.fatorLimite = 5;
//        op2.K = 3;
//        op2.verbose = true;
//        Set<Integer> findMinHullSetGraph = op2.findMinHullSetGraph(graph);
//        op.K = 3;
        op.K = 2;
        Set<Integer> findMinHullSetGraph = op.findMinHullSetGraph(graph);
        System.out.println("REF-S[" + findMinHullSetGraph.size() + "]: " + findMinHullSetGraph);
    }
}
