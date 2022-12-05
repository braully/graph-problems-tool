package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

public class GraphHullNumberHeuristicV2
        extends GraphHullNumber implements IGraphOperation {

    private static final Logger log = Logger.getLogger(GraphHullNumberHeuristicV2.class);

    static final String description = "Hull Number Heuristic V2";

    static boolean verbose = true;

    @Override
    public String getName() {
        return description;
    }

    public GraphHullNumberHeuristicV2() {
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
        Set<Integer> s = new LinkedHashSet<>();

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
        vertices.sort(Comparator
                .comparingInt((Integer v) -> -graphRead.degree(v))
                .thenComparing(v -> v));
        int total = graphRead.getVertexCount();
        int cont = 0;
//        Integer v = vertices.get(0);
//        if(v != null)
        for (Integer v : vertices) {
            if (s.contains(v)) {
                continue;
            }
            if (verbose) {
                if (cont % 10 == 0) {
                    System.out.println("Trying ini vert: " + v);
                }
//                UtilProccess.printCurrentItme();

            }
            Set<Integer> tmp = buildOptimizedHullSetFromStartVertice(graphRead, v, s, aux, sizeHs);
            tmp = tentarMinimizar(graphRead, tmp);
            if (hullSet == null) {
                hullSet = tmp;
                vl = v;
                System.out.println("Primeiro encontrado " + (hullSet.size()));

            } else if (tmp.size() < hullSet.size()) {
                System.out.println("Melhorado em: " + (hullSet.size() - tmp.size()));
                System.out.println(" em i " + v + " vindo de " + vl);
                System.out.println("d(" + v + ")=" + graphRead.degree(v) + " d(" + vl + ")=" + graphRead.degree(vl));
                System.out.println(hullSet);
                System.out.println(tmp);
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

    private Set<Integer> buildOptimizedHullSetFromStartVertice(UndirectedSparseGraphTO<Integer, Integer> graph,
            Integer v, Set<Integer> sini, int[] auxini, int sizeHsini) {
        Set<Integer> s = new LinkedHashSet<>(sini);
        int vertexCount = graph.getVertexCount();
        int[] aux = auxini.clone();
//        int sizeHs = addVertToS(v, s, graph, aux) + sizeHsini;
        int sizeHs = sizeHsini;
        int bestVertice;
        do {
            bestVertice = -1;
            int maiorGrau = 0;
            int maiorDeltaHs = 0;
            int maiorContaminado = 0;

            for (int i = 0; i < vertexCount; i++) {
                //Se vertice já foi adicionado, ignorar
                if (aux[i] >= INCLUDED) {
                    continue;
                }
                int[] auxb = aux.clone();
                int deltaHsi = addVertToS(i, null, graph, auxb);

                int neighborCount = 0;
                int contaminado = 0;
                //Contabilizar quantos vertices foram adicionados
                for (int j = 0; j < vertexCount; j++) {
                    if (auxb[j] == INCLUDED) {
                        neighborCount++;
                    }
                    if (auxb[j] == NEIGHBOOR_COUNT_INCLUDED) {
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

    public Set<Integer> tentarMinimizar(UndirectedSparseGraphTO<Integer, Integer> graphRead, Set<Integer> tmp) {
        Set<Integer> s = tmp;
        for (Integer v : tmp) {
            if (graphRead.degree(v) < 2) {
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
}
