package com.github.braully.graph.operation;

import com.github.braully.graph.GraphWS;
import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import org.apache.log4j.Logger;
import util.UtilParse;
import util.UtilProccess;

/**
 *
 * Baseado na implementação de rodrigomafort
 * https://github.com/rodrigomafort/TSSGenetico/blob/master/TSSCordasco.cpp
 * https://github.com/rodrigomafort/TSSGenetico
 */
public class GraphTSSCordasco implements IGraphOperation {

    static final String type = "P3-Convexity";
    static final String description = "TSS-Cordasco";

    private static final Logger log = Logger.getLogger(GraphWS.class);
    public int K = 2;
    public Integer marjority;

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
//        Set<Integer> setN = new HashSet<>();
//        setN.addAll(graph.getSet());
        String inputData = graph.getInputData();
        List<Integer> reqList = UtilParse.parseAsIntList(inputData, ",");

        /* Processar a buscar pelo hullset e hullnumber */
        Map<String, Object> response = new HashMap<>();
        Set s = tssCordasco(graph, reqList);

        try {
            response.put("TSS", "" + s);
            response.put(IGraphOperation.DEFAULT_PARAM_NAME_SET, s);
            response.put("|TSS|", s.size());
            response.put(IGraphOperation.DEFAULT_PARAM_NAME_RESULT, s.size());

        } catch (Exception ex) {
            log.error(null, ex);
        }
        return response;
    }

    public Set<Integer> tssCordasco(UndirectedSparseGraphTO graph) {
        return tssCordasco(graph, null);
    }

    public Set<Integer> tssCordasco(UndirectedSparseGraphTO graph, List<Integer> reqList) {
        Set<Integer> S = new LinkedHashSet<>();
        //(G -> Vertices.begin(), G -> Vertices.end())
        Set<Integer> U = new TreeSet<>(graph.getVertices());
        int n = graph.getVertexCount();

        //Variáveis do Algoritmo
        int[] delta = new int[n];
        int[] k = new int[n];

        Set<Integer>[] N = new Set[n];

        //Variáveis auxiliares para desempenho
        Integer[] mapa = new Integer[n];
        LinkedList ll = null;
        LinkedList<Integer> k0 = new LinkedList<>();
        LinkedHashSet<Integer> delta_k = new LinkedHashSet<>();
        Boolean inDelta_k[] = new Boolean[n];

        for (Integer v : U) {
            delta[v] = graph.degree(v);
//            k[v] = R[v];
//Se existir a lista de requisitos
            int req = Math.min(K, delta[v]);
            if (reqList != null) {
                req = reqList.get(v);
            } else if (marjority != null) {
                req = graph.degree(v) / marjority;
            }

            k[v] = req;
            N[v] = new LinkedHashSet<>(graph.getNeighborsUnprotected(v));

            mapa[v] = v;
            if (k[v] == 0) {
                k0.add(v);
            }

            if (delta[v] < k[v]) {
                delta_k.add(v);
                inDelta_k[v] = true;
            } else {
                inDelta_k[v] = false;
            }
        }

        while (U.size() > 0) {
            Integer v = null;
            //Caso 1: Existe v em U tal que k(v) = 0 => v foi dominado e sua dominação deve ser propagada
            if (k0.size() > 0) {
                v = k0.pollLast();
                //cout << "Caso 1: " << v.Id() << endl;
                if (inDelta_k[v] == true) {
                    delta_k.remove(v);
                }

                for (Integer u : N[v]) {
                    if (!u.equals(v)) {
                        N[u].remove(v);
                    }

                    delta[u] = delta[u] - 1;
                    if (k[u] > 0) {
                        k[u] = k[u] - 1;
                        if (k[u] == 0) {
                            k0.add(u);
                        }
                    }
                }
            } else {
                //Caso 2: Existe v em U tal que delta(v) < k(v) => v não possui vizinhos o suficiente para ser dominado, logo v é adicionado
                if (delta_k.size() > 0) {
//				auto it = prev(delta_k.end());
//				v = *it;
                    Integer last = null;
                    for (Integer e : delta_k) {
                        last = e;
                    }
                    v = last;
                    //cout << "Caso 2: " << v.Id() << endl;

                    //cout << "Caso 2: " << v.Id() << endl;
                    delta_k.remove(last);
                    inDelta_k[v] = false;

                    S.add(v);

                    for (Integer u : N[v]) {
                        if (!u.equals(v)) {
                            N[u].remove(v);
                        }

                        k[u] = k[u] - 1;
                        delta[u] = delta[u] - 1;
                        if (k[u] == 0) {
                            k0.add(u);
                        }
                    }
                } else //Caso 3: Escolher um vértice v que será dominado por seus vizinhos
                {
                    //v é o vértice que maxima a expressão
                    double max_x = -1;
                    for (Integer u : U) {
                        double x = calcularAvaliacao(k[u], delta[u]);
                        if (x > max_x) {
                            max_x = x;
                            v = u;
                        }
                    }

                    //cout << "Caso 3: " << v.Id() << endl;
                    //v será dominado por seus vizinhos
                    for (Integer u : N[v]) {
                        if (!u.equals(v)) {
                            N[u].remove(v);
                        }
                        delta[u] = delta[u] - 1;
                        if (delta[u] < k[u]) {
                            delta_k.add(u);
                            inDelta_k[v] = true;
                        }
                    }
                }
            }
            //A cada iteração: O vértice escolhido é removido do grafo
            U.remove(v);
        }
        S = tryMinimal(graph, S);
        return S;
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
                    aux[vertn] = aux[vertn] + 1;
                    if (aux[vertn] == K) {
                        mustBeIncluded.add(vertn);
                    }
                }
            }
            aux[verti] += K;
        }
        return fecho.size() == graph.getVertexCount();
    }

    public Set<Integer> tryMinimal(UndirectedSparseGraphTO<Integer, Integer> graphRead, Set<Integer> tmp) {
        Set<Integer> s = tmp;
        int cont = 0;
        for (Integer v : tmp) {

            cont++;
            if (graphRead.degree(v) < K) {
                continue;
            }
            Set<Integer> t = new LinkedHashSet<>(s);
            t.remove(v);
            if (checkIfHullSet(graphRead, t.toArray(new Integer[0]))) {
                System.out.println("Reduzido removido: " + v);
                System.out.println("Na posição " + cont + "/" + (tmp.size() - 1));
                s = t;
            }
        }
        return s;
    }

    double calcularAvaliacao(double k, double delta) {
        return k / (delta * (delta + 1));
    }

    public String getTypeProblem() {
        return type;
    }

    public String getName() {
        return description;
    }

    public static void main(String... args) throws FileNotFoundException, IOException {
        GraphTSSCordasco optss = new GraphTSSCordasco();

        System.out.println("Teste greater: ");

        UndirectedSparseGraphTO<Integer, Integer> graph = null;
//        graph = new UndirectedSparseGraphTO("681-753,681-1381,681-4658,753-1381,753-4658,1381-2630,1381-2819,1381-4220,1381-4658,2630-2819,2630-3088,2630-4220,2819-3088,2819-4220,");

//        graph = UtilGraph.loadGraphG6("S?????????????????w@oK?B??GW@OE?g");
//        graph = UtilGraph.loadGraphG6("S??A?___?O_aOOCGCO?OG@AAB_??Fvw??");
//        graph = UtilGraph.loadGraphG6("Ss_?G?@???coH`CEABGR?AWDe?A_oAR??");
        graph = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/ca-AstroPh/ca-AstroPh.txt"));
//        graph = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/ca-HepPh/ca-HepPh.txt"));
//        graph = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/ca-HepTh/ca-HepTh.txt"));
//        graph = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/ca-CondMat/ca-CondMat.txt"));
//        graph = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/ca-GrQc/ca-GrQc.txt"));
//        graph = UtilGraph.loadBigDataset(
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/BlogCatalog3/nodes.csv"),
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/BlogCatalog3/edges.csv"));
//        graph = UtilGraph.loadBigDataset(
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/BlogCatalog/nodes.csv"),
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/BlogCatalog/edges.csv"));
//        graph = UtilGraph.loadBigDataset(
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/Last.fm/nodes.csv"),
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/Last.fm/edges.csv"));
//        graph = UtilGraph.loadBigDataset(
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/Delicious/nodes.csv"),
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/Delicious/edges.csv"));
//        graph = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/ca-GrQc/ca-GrQc.txt"));
//        GraphStatistics statistics = new GraphStatistics();
//        System.out.println(graph.getName() + ": " + statistics.doOperation(graph));
//        System.out.println(graph.getName());
//        graph = UtilGraph.loadGraphG6("U?GoA?ACCA?_E???O?@???@c@`_?Q_C`DGs?o_Q?");
//        graph = UtilGraph.loadGraph(new File("tmp.es"));
        System.out.println(graph.toResumedString());
//        op.K = 3;
//        optss.verbose = false;
////        System.out.println("Subgraph: ");
//        UndirectedSparseGraphTO subGraph = opsubgraph.subGraphInduced(graph, Set.of(388, 1129, 1185, 1654, 3584, 3997));
//        System.out.println(subGraph.getEdgeString());
//        System.out.println("Subgraph: ");
//        UndirectedSparseGraphTO subGraph = opsubgraphn.subGraphInduced(graph, Set.of(1381, 3088, 2630));
//        System.out.println(subGraph.getEdgeString());
        optss.K = 2;
        UtilProccess.printStartTime();
        Set<Integer> buildOptimizedHullSet = optss.tssCordasco(graph);

        UtilProccess.printStartTime();

        System.out.println(
                "S[" + buildOptimizedHullSet.size() + "]: " + buildOptimizedHullSet);
    }
}
