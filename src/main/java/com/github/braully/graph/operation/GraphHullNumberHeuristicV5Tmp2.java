package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.generator.GraphGeneratorRandomGilbert;
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
import static tmp.DensityHeuristicCompare.INI_V;
import static tmp.DensityHeuristicCompare.MAX_V;
import util.UtilProccess;

public class GraphHullNumberHeuristicV5Tmp2
        extends GraphHullNumberHeuristicV1 implements IGraphOperation {

    public int K = 2;

    private static final Logger log = Logger.getLogger(GraphHullNumberHeuristicV5Tmp2.class);

    static final String description = "Hull Number Heuristic V5-tmp2";

    @Override
    public String getName() {
        return description;
    }

    public GraphHullNumberHeuristicV5Tmp2() {
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

    public Boolean isGreater(int... compare) {
        Boolean ret = false;
        int i = 0;
        while (i < compare.length - 1
                && compare[i] == compare[i + 1]) {
            i += 2;
        }
        if (i <= compare.length - 2) {
            if (compare[i] > compare[i + 1]) {
                ret = true;
            } else if (compare[i] == compare[i + 1]) {
                ret = null;
            }
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
    public Set<Integer> buildOptimizedHullSet(UndirectedSparseGraphTO<Integer, Integer> graph) {
        List<Integer> vertices = new ArrayList<>((List<Integer>) graph.getVertices());
        Set<Integer> hullSet = null;
        Integer vl = null;
        Set<Integer> s = new LinkedHashSet<>();

        int vertexCount = graph.getVertexCount();
        int[] auxini = new int[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            auxini[i] = 0;
        }
        int sizeHs = 0;
        for (Integer v : vertices) {
            if (graph.degree(v) <= K - 1) {
                sizeHs = sizeHs + addVertToS(v, s, graph, auxini);
            }
        }

        int[] aux = auxini.clone();
        BFSDistanceLabeler<Integer, Integer> bdl = new BFSDistanceLabeler<>();
        bdl.labelDistances(graph, s);

        int bestVertice;
        boolean esgotado = false;
        int ranking[] = new int[vertexCount];

        do {
            bestVertice = -1;
            int maiorGrau = 0;
            int maiorDeltaHs = 0;
            int maiorContaminado = 0;
            int maiorProfundidade = 0;
            int menorRest = 0;
            List<Integer> melhores = new ArrayList<Integer>();
            for (int i = 0; i < vertexCount; i++) {
                if (aux[i] >= K) {
                    ranking[i] = vertexCount;
                    continue;
                }
                int rest = K - aux[i];
                ranking[i] = rest;
                if (menorRest == 0) {
                    menorRest = rest;
                    melhores.add(i);
                    bestVertice = i;

                } else if (menorRest == rest) {
                    melhores.add(i);
                } else if (rest < menorRest) {
                    melhores.clear();
                    menorRest = rest;
                    melhores.add(i);
                    bestVertice = i;
                }
            }

            while (menorRest > 0) {
                int bestNeighbor = -1;

                for (Integer i : graph.getNeighborsUnprotected(bestVertice)) {
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

                    if (bestNeighbor == -1) {
                        maiorDeltaHs = deltaHsi;
                        maiorGrau = neighborCount;
                        maiorContaminado = contaminado;
                        bestNeighbor = i;
                        maiorProfundidade = bdl.getDistance(graph, i);
                    } else if (isGreater(deltaHsi, maiorDeltaHs,
                            neighborCount, maiorGrau,
                            bdl.getDistance(graph, i), maiorProfundidade,
                            contaminado, maiorContaminado)) {
                        maiorDeltaHs = deltaHsi;
                        maiorGrau = neighborCount;
                        maiorContaminado = contaminado;
                        bestNeighbor = i;
                        maiorProfundidade = bdl.getDistance(graph, i);
                    }

                }
                sizeHs = sizeHs + addVertToS(bestNeighbor, s, graph, aux);

                menorRest--;
            }

//            List<Integer> melhores2 = new ArrayList<Integer>();
//
//            for (Integer i : melhores) {
//                //Se vertice já foi adicionado, ignorar
//                if (aux[i] >= K) {
//                    continue;
//                }
//
//                int[] auxb = aux.clone();
//                int deltaHsi = addVertToS(i, null, graph, auxb);
//
//                int neighborCount = 0;
//                int contaminado = 0;
//                //Contabilizar quantos vertices foram adicionados
//                for (int j = 0; j < vertexCount; j++) {
//                    if (auxb[j] >= K) {
//                        neighborCount++;
//                    }
//                    if (auxb[j] > 0 && auxb[j] < K) {
//                        contaminado++;
//                    }
//                }
////                if ((i == 1 || i == 4) && v.equals(18) && s.size() == 4) {
////                    System.out.println("Peso v:" + i);
////                    printPesoAux(auxb);
////                }
//
//                if (bestVertice == -1) {
//                    maiorDeltaHs = deltaHsi;
//                    maiorGrau = neighborCount;
//                    maiorContaminado = contaminado;
//                    bestVertice = i;
//                    maiorProfundidade = bdl.getDistance(graph, i);
//                    melhores2.add(i);
//                } else {
//                    Boolean greater = isGreater(deltaHsi, maiorDeltaHs,
//                            neighborCount, maiorGrau,
//                            bdl.getDistance(graph, i), maiorProfundidade,
//                            contaminado, maiorContaminado);
//                    if (null == greater) {
//                        melhores2.add(i);
//                    } else if (greater) {
//                        maiorDeltaHs = deltaHsi;
//                        maiorGrau = neighborCount;
//                        maiorContaminado = contaminado;
//                        bestVertice = i;
//                        maiorProfundidade = bdl.getDistance(graph, i);
//                        melhores2.clear();
//                        melhores2.add(i);
//                    }
//                }
//            }
//
//            if (bestVertice == -1) {
//                if (esgotado) {
//                    break;
//                } else {
//                    esgotado = true;
//                    continue;
//                }
//            }
//            if (maiorDeltaHs == 1 && verbose) {
//                System.out.println("ALERTA: Adicionando vertice sem aumento de entropia " + bestVertice);
//                System.out.println("O vértice v: " + bestVertice + " precisa de " + (K - aux[bestVertice]) + " vizinhos para ser contaminado ");
//            }
//            if (verbose && melhores2.size() >= 2) {
//                System.out.println("O vértice v: " + bestVertice + " empatou com " + melhores2.size() + " vertices");
//
//            }
//            sizeHs = sizeHs + addVertToS(bestVertice, s, graph, aux);
            bdl.labelDistances(graph, s);
        } while (sizeHs < vertexCount);
        s = tryMinimal(graph, s);

        if (hullSet == null) {
            hullSet = s;

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

    public static void main(String... args) {

        GraphHullNumberHeuristicV5Tmp2 op = new GraphHullNumberHeuristicV5Tmp2();
        UndirectedSparseGraphTO<Integer, Integer> graph = null;
        graph = new UndirectedSparseGraphTO("0-1,0-3,1-2,3-4,3-5,4-5,");
        Set<Integer> buildOptimizedHullSet = op.buildOptimizedHullSet(graph);
        System.out.println("S[" + buildOptimizedHullSet.size() + "]: " + buildOptimizedHullSet);
        if (true) {
            return;
        }
        GraphGeneratorRandomGilbert generator = new GraphGeneratorRandomGilbert();

        op.setVerbose(true);

        System.out.print("Dens:\t");
        for (double density = 0.1; density <= 0.9; density += 0.1) {
            System.out.printf("%.1f \t", density);

        }

        for (int nv = INI_V; nv <= MAX_V; nv++) {

            System.out.printf("%3d: \t", nv);

            for (double density = 0.1; density <= 0.9; density += 0.1) {
                graph = generator.generate(nv, density);
                Set<Integer> findMinHullSetGraph = op.findMinHullSetGraph(graph);
                System.out.printf("%d", findMinHullSetGraph.size());

                System.out.printf("\t");

            }
            System.out.println();
        }
    }
}
