package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.apache.log4j.Logger;
import util.UtilProccess;

public class GraphHullNumberHeuristicV5
        extends GraphHullNumberHeuristicV1 implements IGraphOperation {

    public static int K = 2;

    private static final Logger log = Logger.getLogger(GraphHullNumberHeuristicV5.class);

    static final String description = "Hull Number Heuristic V5";

    @Override
    public String getName() {
        return description;
    }

    public GraphHullNumberHeuristicV5() {
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

    protected Set<Integer> buildOptimizedHullSetFromStartVertice(UndirectedSparseGraphTO<Integer, Integer> graph,
            Integer v, Set<Integer> sini, int[] auxini, int sizeHsini) {
//        Set<Integer> s = new HashSet<>(sini);
        Set<Integer> s = new LinkedHashSet<>(sini);

        int vertexCount = graph.getVertexCount();
        int[] aux = auxini.clone();
        int sizeHs = addVertToS(v, s, graph, aux) + sizeHsini;
        BFSDistanceLabeler<Integer, Integer> bdl = new BFSDistanceLabeler<>();
        bdl.labelDistances(graph, s);

        int bestVertice;
        boolean esgotado = false;

        do {
            bestVertice = -1;
            int maiorGrau = 0;
            int maiorDeltaHs = 0;
            int maiorContaminado = 0;
            int maiorProfundidade = 0;

            for (int i = 0; i < vertexCount; i++) {
                //Se vertice já foi adicionado, ignorar
                if (aux[i] >= K) {
                    continue;
                }
//                if (!esgotado && aux[i] > 0) {
//                    continue;
//                }
//                if (!esgotado && aux[i] > 0) {
//                    continue;
//                }
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
//                if ((i == 1 || i == 4) && v.equals(18) && s.size() == 4) {
//                    System.out.println("Peso v:" + i);
//                    printPesoAux(auxb);
//                }

                if (verbose) {
                    if (s.size() == 2 && i == 0
                            && v.equals(18)) {
                        System.out.println("Pexso aux: ");
                        printPesoAux(aux);

                    }
                    if ((i == 17 || i == 4
                            || i == 5 || i == 1 || i == 0
                            || i == 8 || i == 9
                            || i == 10 || i == 11
                            || i == 15 || i == 16
                            || i == 2 || i == 3)
                            && v.equals(18) && s.size() == 2) {
                        System.out.println("\n* PESO v:" + i);
                        System.out.println(" - Degree: " + graph.degree(i));
                        System.out.println(" - bfs: " + bdl.getDistance(graph, i));
                        System.out.println(" - Delta: " + deltaHsi);
                        System.out.print(" + ");
                        for (int x = 0; x < vertexCount; x++) {
                            if (aux[x] < K && auxb[x] >= K && x != i) {
                                System.out.print(x + " [" + aux[x] + "][" + bdl.getDistance(graph, x) + "]");
                            }

                        }
                        System.out.println();
                        System.out.println(" - NeighborCount: " + neighborCount);
                        System.out.println(" - maiorContaminado: " + contaminado);
                        System.out.print(" ");
                        printPesoAux(auxb);
                    }
                }

                if (bestVertice == -1) {
                    maiorDeltaHs = deltaHsi;
                    maiorGrau = neighborCount;
                    maiorContaminado = contaminado;
                    bestVertice = i;
                    maiorProfundidade = bdl.getDistance(graph, i);
                } else if (deltaHsi > maiorDeltaHs) {
                    maiorDeltaHs = deltaHsi;
                    maiorGrau = neighborCount;
                    maiorContaminado = contaminado;
                    bestVertice = i;
                    maiorProfundidade = bdl.getDistance(graph, i);
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
                if (esgotado) {
                    break;
                } else {
                    esgotado = true;
                    continue;
                }
            }
            sizeHs = sizeHs + addVertToS(bestVertice, s, graph, aux);
            bdl.labelDistances(graph, s);
        } while (sizeHs < vertexCount);

        s = tryMinimal(graph, s);
        return s;
    }

    private boolean isGreater(int... compare) {
        boolean ret = false;
        int i = 0;
        while (i < compare.length - 1
                && compare[i] == compare[i + 1]) {
            i++;
        }
        if (compare[i] > compare[i + 1]) {
            ret = true;
        }
        return ret;
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
                if (!vertn.equals(verti) && ++aux[vertn] == K) {
                    mustBeIncluded.add(vertn);
                }
            }
            countIncluded++;
        }
        return countIncluded;
    }

//    @Override
    public Set<Integer> buildOptimizedHullSet(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        List<Integer> vertices = new ArrayList<>((List<Integer>) graphRead.getVertices());
        Set<Integer> hullSet = null;
        Integer vl = null;
        Set<Integer> sini = new LinkedHashSet<>();

        int vertexCount = graphRead.getVertexCount();
        int[] auxini = new int[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            auxini[i] = 0;
        }
        int sizeHs = 0;
        for (Integer v : vertices) {
            if (graphRead.degree(v) <= K - 1) {
                sizeHs = sizeHs + addVertToS(v, sini, graphRead, auxini);
            }
        }
        vertices.sort(Comparator
                .comparingInt((Integer v) -> -graphRead.degree(v))
                .thenComparing(v -> -v));

        int total = graphRead.getVertexCount();
        int cont = 0;
//        int ret = total & cont;
        Integer vi = vertices.get(0);
        List<Integer> verticeStart = new ArrayList<>();
        Integer bestN = null;
        for (Integer v : graphRead.getNeighborsUnprotected(vi)) {
            if (bestN == null) {
                bestN = v;
            } else if (graphRead.degree(v) > graphRead.degree(bestN)
                    || (graphRead.degree(bestN) == graphRead.degree(v)
                    && bestN > v)) {
                bestN = v;
            }
        }
        verticeStart.add(vi);
        verticeStart.add(bestN);

        for (Integer v : verticeStart) {
            if (sini.contains(v)) {
                continue;
            }
            if (verbose) {
//                System.out.println("Trying ini vert: " + v);
//                UtilProccess.printCurrentItme();

            }
            Set<Integer> tmp = buildOptimizedHullSetFromStartVertice(graphRead, v, sini, auxini, sizeHs);
            tmp = tryMinimal(graphRead, tmp);
            if (hullSet == null) {
                hullSet = tmp;
                vl = v;
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
        return hullSet;
    }

    public Set<Integer> tryMinimal(UndirectedSparseGraphTO<Integer, Integer> graphRead, Set<Integer> tmp) {
        Set<Integer> s = tmp;
//        System.out.println("tentando reduzir");

        for (Integer v : tmp) {
            if (graphRead.degree(v) < K) {
                continue;
            }
            Set<Integer> t = new LinkedHashSet<>(tmp);
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

    public void printPesoAux(int[] auxb) {
        int peso = 0;
        for (int i = 0; i < auxb.length; i++) {
            peso = peso + auxb[i];
        }
        System.out.print("{" + peso + "}");
        UtilProccess.printArray(auxb);
    }
}
