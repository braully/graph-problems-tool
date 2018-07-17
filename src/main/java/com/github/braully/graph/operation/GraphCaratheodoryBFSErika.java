package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.algorithms.filters.FilterUtils;
import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

public class GraphCaratheodoryBFSErika
        extends GraphCheckCaratheodorySet
        implements IGraphOperation {

    private static final Logger log = Logger.getLogger(GraphCaratheodoryBFSErika.class);

    static final String type = "P3-Convexity";
    static final String description = "Nº Caratheodory (BFS Bloco Erika)";

    public static boolean verbose = true;

//    public static boolean verbose = false;
    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {

        int maxmaxcg = 0;

        if (verbose) {
            StringBuilder sb = new StringBuilder();
            sb.append("V(G)    = {");
            for (int i = 0; i < graph.getVertexCount(); i++) {
                sb.append(String.format("%2d | ", i));
            }
            sb.append("}");
            log.info(sb.toString());
        }
        Collection<Integer> vertices = graph.getVertices();

        for (Integer v : vertices) {
            int maxcg = 0;
            maxcg = ncaratBfsBloco(graph, v);
            if (maxmaxcg < maxcg) {
                maxmaxcg = maxcg;
            }
        }
        if (verbose) {
            log.info("c(G) = " + maxmaxcg);
        }
        /* Processar a buscar pelo caratheodoryset e caratheodorynumber */
        Map<String, Object> response = new HashMap<>();
        response.put(OperationConvexityGraphResult.PARAM_NAME_CARATHEODORY_NUMBER, maxmaxcg);
        response.put(IGraphOperation.DEFAULT_PARAM_NAME_RESULT, maxmaxcg);
        return response;
    }

    public int ncaratBfsBloco(UndirectedSparseGraphTO<Integer, Integer> graph, Integer v) {
        BFSDistanceLabeler<Integer, Integer> bdl = new BFSDistanceLabeler<>();
        Collection<Integer> vertices = graph.getVertices();
        int vertexCount = graph.getVertexCount();

        int[] lv = new int[vertexCount];
        int[] lvLinha = new int[vertexCount];
        int[] l1v = new int[vertexCount];
        int[] l2v = new int[vertexCount];
        int[] l3v = new int[vertexCount];
        int maxcg = 0;
        if (verbose) {
            log.info("0 - Enraizando: v=" + v);
        }
        //BFS
        bdl.labelDistances(graph, v);

        if (verbose) {
            StringBuilder sb = new StringBuilder();

            sb.append(String.format("bfs(%2d) = {", v));
            for (int i = 0; i < vertexCount; i++) {
                int distance = bdl.getDistance(graph, i);
                sb.append(String.format("%2d | ", distance));
            }
            log.info("}");
        }
        //Computar arvore
        Map<Integer, Integer> predecessorCount = new HashMap<>();
        Map<Integer, Set<Integer>> childs = new HashMap<>();
        for (int w = 0; w < vertexCount; w++) {
            Set<Integer> predecessors = bdl.getPredecessors(w);
            if (predecessors != null) {
                for (Integer vp : predecessors) {
                    Integer cont = predecessorCount.get(vp);
                    if (cont == null) {
                        cont = 0;
                    }
                    cont = cont + 1;
                    predecessorCount.put(vp, cont);

                    Set<Integer> chvp = childs.get(vp);
                    if (chvp == null) {
                        chvp = new HashSet<>();
                        childs.put(vp, chvp);
                    }
                    chvp.add(w);
                }
            }
        }
        //Folhas
        Set<Integer> keySet = predecessorCount.keySet();
        Set<Integer> leafs = new HashSet<>(vertices);
        leafs.removeAll(keySet);
        if (verbose) {
            log.info("Leafs(" + v + "): " + leafs);

        }
        // 1 - Se w é folha
        for (Integer w : leafs) {
            if (verbose) {
                log.info("1 - É folha: " + w);
            }
            lv[w] = 1;
            lvLinha[w] = Integer.MIN_VALUE;

        }
        // 2 - Se w tem no maximo um filho u
        for (int w = 0; w < vertexCount; w++) {
            Integer pdcount = predecessorCount.get(w);
            if (pdcount != null && pdcount.equals(1)) {
                Set<Integer> ChvW = childs.get(w);
                if (verbose) {
                    log.info("2 - Apenas 1 filho: " + w + " -> " + ChvW);
                }
                for (Integer u : ChvW) {
                    lv[w] = 1;
                    lvLinha[w] = lv[u]; //duvida
                }
            }
        }
        // 3 - Se w tem pelo menos dois filho u1, u2
        for (int w = 0; w < vertexCount; w++) {
            Integer pdcount = predecessorCount.get(w);
            if (pdcount != null && pdcount.intValue() >= 2) {
                Set<Integer> su = childs.get(w);
                if (verbose) {
                    log.info("3 - Pelo menos 2 filhos: w=" + w + " -> Chv(w)=" + su);
                }
                // 3a - Se dois filhos são folha
                int contFilhosFolha = 0;
                for (Integer u : su) {
                    if (leafs.contains(u)) {
                        contFilhosFolha++;
                    }
                }
                if (contFilhosFolha >= 2) {
                    if (verbose) {
                        log.info("\t3a - 2 filhos folha: " + w);
                    }
                    l1v[w] = 2;
                }

                //3b - Conjunto de componentes conexas do subgrafo induzido G|Chv(w)
                UndirectedSparseGraphTO inducedSubgraph = FilterUtils.createInducedSubgraph(su, graph);
                if (verbose) {
                    log.info("\t3b - subgrafo induzido G|Chv(w) : " + inducedSubgraph);
                }
                List<UndirectedSparseGraphTO> componentesConexas = listaComponentesConexas(inducedSubgraph);
                if (verbose) {
                    log.info("\t Compomentes conexas (" + componentesConexas.size() + "): " + componentesConexas);
                }
                //3c -
                l2v[w] = Integer.MIN_VALUE;
                if (componentesConexas.size() >= 2) {
                    int maxlvu[] = new int[componentesConexas.size()];
                    for (int c = 0; c < componentesConexas.size(); c++) {
                        maxlvu[c] = Integer.MIN_VALUE;
                        UndirectedSparseGraphTO componente = componentesConexas.get(c);
                        for (Integer u : (Collection<Integer>) componente.getVertices()) {
                            maxlvu[c] = Math.max(maxlvu[c], lv[u]);
                        }
                    }
                    Arrays.sort(maxlvu);
                    l2v[w] = maxlvu[maxlvu.length - 1] + maxlvu[maxlvu.length - 2];
                }

                //3d -
                int maxlvu1lvu2 = Integer.MIN_VALUE;
                for (int c = 0; c < componentesConexas.size(); c++) {
                    int u1 = -1;
                    int maxlvu = Integer.MIN_VALUE;
                    int maxlvulinha = Integer.MIN_VALUE;
                    UndirectedSparseGraphTO componente = componentesConexas.get(c);
                    for (Integer u : (Collection<Integer>) componente.getVertices()) {
                        if (maxlvu < lv[u]) {
                            maxlvu = lv[u];
                            u1 = u;
                        }
                        if (maxlvulinha < lvLinha[u] && !u.equals(u1)) {
                            maxlvulinha = lvLinha[u];
                        }
                    }
                    if (maxlvu > maxlvu1lvu2 && maxlvulinha > maxlvu1lvu2 && (maxlvu + maxlvulinha) > maxlvu1lvu2) {
                        maxlvu1lvu2 = maxlvu + maxlvulinha;
                    }
                }
                l3v[w] = maxlvu1lvu2;

                //3e - lv(w) = max{l1v(w), l2v(w), l3v(w)}, lvlinha(w) = max{lv(u) |uEChv(w)}
                lv[w] = Math.max(Math.max(l1v[w], l2v[w]), l3v[w]);
                lvLinha[w] = Integer.MIN_VALUE;
                for (Integer u : su) {
                    lvLinha[w] = Math.max(lvLinha[w], lv[u]);
                }
            }
        }

        if (verbose) {
            StringBuilder sblv = new StringBuilder("lv   = { ");
            StringBuilder sblv1 = new StringBuilder("l1v  = { ");
            StringBuilder sblv2 = new StringBuilder("l2v  = { ");
            StringBuilder sblv3 = new StringBuilder("l3v  = { ");
            StringBuilder sblvlinha = new StringBuilder("lv' ={ ");
            for (int i = 0; i < graph.getVertexCount(); i++) {
//                String f = String.format(") = { ");
//                sblv.append(f);
//                sblv1.append(f);
//                sblv2.append(f);
//                sblv3.append(f);
//                sblvlinha.append(f);

                sblv.append(String.format("%2d | ", lv[i]));
                sblv1.append(String.format("%2d | ", l1v[i]));
                sblv2.append(String.format("%2d | ", l2v[i]));
                sblv3.append(String.format("%2d | ", l3v[i]));
                sblvlinha.append(String.format("%2d | ", lvLinha[i]));
            }
            sblv.append("}");
            sblv1.append("}");
            sblv2.append("}");
            sblv3.append("}");
            sblvlinha.append("}");

            log.info(sblv.toString());
            log.info(sblv1.toString());
            log.info(sblv2.toString());
            log.info(sblv3.toString());
            log.info(sblvlinha.toString());
        }

        //4 - c(G) = max{lv(v)|vEV(G)}
        for (Integer vert : vertices) {
            if (lv[vert] > maxcg) {
                maxcg = lv[vert];
            }
        }
        if (verbose) {
            log.info("4 - c(G) = max{lv(v)|vEV(G) = " + maxcg);
        }
        return maxcg;
    }

    @Override
    public String getTypeProblem() {
        return type;
    }

    @Override
    public String getName() {
        return description;
    }

    private List<UndirectedSparseGraphTO> listaComponentesConexas(UndirectedSparseGraphTO inducedSubgraph) {
        List<UndirectedSparseGraphTO> listaComponetesConexas = new ArrayList<>();
        BFSDistanceLabeler<Integer, Integer> bdl = new BFSDistanceLabeler<>();
        Collection<Integer> vertices = inducedSubgraph.getVertices();
        Set<Integer> discoverd = new HashSet<>();
        for (Integer v : vertices) {
            if (!discoverd.contains(v)) {
                bdl.labelDistances(inducedSubgraph, v);
                List<Integer> verticesDiscoverd = bdl.getVerticesInOrderVisited();
                discoverd.addAll(discoverd);
                UndirectedSparseGraphTO componente = FilterUtils.createInducedSubgraph(verticesDiscoverd, inducedSubgraph);
                listaComponetesConexas.add(componente);
            }
        }
        return listaComponetesConexas;
    }

}
