package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.apache.log4j.Logger;
import util.BFSUtil;
import util.UtilProccess;

public class GraphHullNumberHeuristicV6
        extends GraphHullNumber implements IGraphOperation {

    public static int K = 2;

    private static final Logger log = Logger.getLogger(GraphHullNumberHeuristicV6.class);

    static final String description = "Hull Number Heuristic V6";

    static boolean verbose = true;

    @Override
    public String getName() {
        return description;
    }

    public GraphHullNumberHeuristicV6() {
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
        int bestVertice;
        int bestVertice1;
        boolean esgotado = false;

        do {
            bestVertice = -1;
            bestVertice1 = -1;
            int maiorGrau = 0;
            int maiorDeltaHs = 0;
            int maiorContaminado = 0;

            for (int i = 0; i < vertexCount; i++) {
                if (aux[i] >= K) {
                    continue;
                }
                if (!esgotado && aux[i] > 0) {
                    continue;
                }
                int[] auxb = aux.clone();
                int addVertToS = addVertToS(i, null, graph, auxb);
                int[] auxc = aux.clone();
                for (int x = i + 1; x < vertexCount; x++) {
                    if (aux[x] >= K) {
                        continue;
                    }
                    if (!esgotado && aux[x] > 0) {
                        continue;
                    }

                    int neighborCount = 0;
                    int contaminado = 0;
                    int deltaHsi = addVertToS(x, null, graph, auxc) + addVertToS;
                    for (int j = 0; j < vertexCount; j++) {
                        if (auxc[j] >= K) {
                            neighborCount++;
                        }
                        if (auxc[j] > 0 && auxc[j] < K) {
                            contaminado++;
                        }
                    }
                    if (bestVertice == -1) {
                        maiorDeltaHs = deltaHsi;
                        maiorGrau = neighborCount;
                        maiorContaminado = contaminado;
                        bestVertice = i;
                        bestVertice1 = x;
                    } else if (deltaHsi > maiorDeltaHs) {
                        maiorDeltaHs = deltaHsi;
                        maiorGrau = neighborCount;
                        maiorContaminado = contaminado;
                        bestVertice = i;
                        bestVertice1 = x;
                    } else if (deltaHsi == maiorDeltaHs) {
                        if (neighborCount > maiorGrau) {
                            maiorDeltaHs = deltaHsi;
                            maiorGrau = neighborCount;
                            maiorContaminado = contaminado;
                            bestVertice = i;
                            bestVertice1 = x;
                        } else if (neighborCount == maiorGrau) {
                            if (contaminado > maiorContaminado) {
                                maiorDeltaHs = deltaHsi;
                                maiorGrau = neighborCount;
                                maiorContaminado = contaminado;
                                bestVertice = i;
                                bestVertice1 = x;
                            }
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
            sizeHs = sizeHs + addVertToS(bestVertice1, s, graph, aux);
        } while (sizeHs < vertexCount);

        s = tryMinimal(graph, s);
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

        int total = graphRead.getVertexCount();
        int cont = 0;
        boolean esgotado = false;
        Integer v = vertices.get(0);
        BFSUtil bfs = BFSUtil.newBfsUtilSimple(vertexCount);
        bfs.labelDistances(graphRead, v);

//        if(v != null)
        Set<Integer> s = new LinkedHashSet<>(sini);
        int[] aux = auxini;

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
                if (!esgotado && aux[i] > 0) {
                    continue;
                }
                int[] auxb = aux.clone();
                int deltaHsi = addVertToS(i, null, graphRead, auxb);

                int neighborCount = 0;
                int contaminado = 0;
                //Contabilizar quantos vertices foram adicionados
                for (int j = 0; j < vertexCount; j++) {
                    if (auxb[j] == K) {
                        neighborCount++;
                    }
                    if (auxb[j] <= K) {
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
                if (esgotado) {
                    break;
                } else {
                    esgotado = true;
                    continue;
                }
            }
            sizeHs = sizeHs + addVertToS(bestVertice, s, graphRead, aux);
        } while (sizeHs < vertexCount);

        s = tryMinimal(graphRead, s);
        if (hullSet == null) {
            hullSet = new HashSet<>(s);
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
