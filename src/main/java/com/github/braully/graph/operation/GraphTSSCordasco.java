package com.github.braully.graph.operation;

import heuristic.AbstractHeuristic;
import com.github.braully.graph.GraphWS;
import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.apache.log4j.Logger;
import util.BFSUtil;
import util.MapCountOpt;
import util.UtilParse;
import util.UtilProccess;

/**
 *
 * Baseado na implementação de rodrigomafort
 * https://github.com/rodrigomafort/TSSGenetico/blob/master/TSSCordasco.cpp
 * https://github.com/rodrigomafort/TSSGenetico
 */
public class GraphTSSCordasco extends AbstractHeuristic implements IGraphOperation {

    static final String description = "TSS-Cordasco";

    {
        parameters.put(MINIMAL, null);
    }

    public String getName() {
        StringBuilder sb = new StringBuilder(description);
        for (String par : parameters.keySet()) {
            Boolean get = parameters.get(par);
            if (get != null) {
                if (get) {
                    sb.append("+");
                } else {
                    sb.append("-");
                }
                sb.append(par);
            }
        }
        return sb.toString();
    }

    private static final Logger log = Logger.getLogger(GraphWS.class);
    public int K = 2;
    public Integer marjority;

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
//        Set<Integer> setN = new HashSet<>();
//        setN.addAll(graph.getSet());
        try {

            String inputData = graph.getInputData();
            if (inputData != null) {
                int parseInt = Integer.parseInt(inputData.trim());
                setR(parseInt);

            }

        } catch (Exception e) {

        }

        /* Processar a buscar pelo hullset e hullnumber */
        Map<String, Object> response = new HashMap<>();
        Set s = tssCordasco(graph);

        try {
            response.put("R", this.R);
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
        initKr(graph);
        //(G -> Vertices.begin(), G -> Vertices.end())
        Set<Integer> U = new LinkedHashSet<>(graph.getVertices());
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
            k[v] = kr[v];
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
                    N[u].remove(v);
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
                        N[u].remove(v);
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
                    if (v == null) {
                        System.out.print(" " + v);
                    }
                    for (Integer u : N[v]) {
                        N[u].remove(v);
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
        if (tryMiminal()) {
            S = tryMinimal(graph, S);
        }
//        S = tryMinimalAll(graph, S);

        return S;
    }
    protected BFSUtil bdls;
    protected MapCountOpt mapCount;
    Map<Integer, Integer> tamanhoT = new HashMap<>();
    int menorT = Integer.MAX_VALUE;
    int tamanhoReduzido = 0;
    protected Queue<Integer> mustBeIncluded = new ArrayDeque<>();
    protected int[] pularAvaliacao = null;
    protected int[] degree = null;
    int[] auxb = null;

    public Set<Integer> tryMinimal(UndirectedSparseGraphTO<Integer, Integer> graphRead,
            Set<Integer> tmp, int tamanhoAlvo) {
        Set<Integer> s = tmp;

        tamanhoT.clear();
        tamanhoReduzido = 0;
        menorT = Integer.MAX_VALUE;

        if (s.size() <= 1) {
            return s;
        }

        if (verbose) {
            System.out.println("tentando reduzir: " + s.size());
//            System.out.println("s: " + s);
        }
        for (Integer v : tmp) {
            if (graphRead.degree(v) < kr[v]) {
                continue;
            }
            Set<Integer> t = new LinkedHashSet<>(s);
            t.remove(v);

            int contadd = 0;
            int[] aux = auxb;
            int maiorScount = 0;

            for (int i = 0; i < aux.length; i++) {
                aux[i] = 0;
                if (scount[i] > maiorScount) {
                    maiorScount = scount[i];
                }
            }

            mustBeIncluded.clear();
            for (Integer iv : t) {
                mustBeIncluded.add(iv);
                aux[iv] = kr[iv];
            }
            while (!mustBeIncluded.isEmpty()) {
                Integer verti = mustBeIncluded.remove();
                contadd++;
                Collection<Integer> neighbors = graphRead.getNeighborsUnprotected(verti);
                for (Integer vertn : neighbors) {
                    if (aux[vertn] <= kr[vertn] - 1) {
                        aux[vertn] = aux[vertn] + 1;
                        if (aux[vertn] == kr[vertn]) {
                            mustBeIncluded.add(vertn);
                        }
                    }
                }
                aux[verti] += kr[verti];
            }

            if (contadd >= tamanhoAlvo) {
                s = t;
                Collection<Integer> neighbors = graphRead.getNeighborsUnprotected(v);
                for (Integer vertn : neighbors) {
                    scount[vertn]--;
                }
            } else {
                //            int tamt = contadd - t.size();
                int tamt = contadd;
                tamanhoT.put(v, tamt);
                if (tamt < menorT) {
                    menorT = tamt;
                }
            }
        }
//        if (verbose) {
//            System.out.println("reduzido para: " + s.size());
////            System.out.println("s: " + s);
//        }
        tamanhoReduzido = tmp.size() - s.size();
        return s;
    }
//    int tamanhoReduzido = 0;

    public Set<Integer> tryMinimal2Lite(UndirectedSparseGraphTO<Integer, Integer> graphRead,
            Set<Integer> tmp, int tamanhoAlvo) {
        Set<Integer> s = tmp;
        if (s.size() <= 2) {
            return s;
        }
        if (verbose) {
            System.out.println("tentando reduzir-2-lite: " + s.size() + " tamanho alvo: " + tamanhoAlvo);
//            System.out.println("s: " + s);
        }
        List<Integer> ltmp = new ArrayList<>(tmp);
        Collection<Integer> vertices = graphRead.getVertices();
        List<Integer> verticesElegiveis = new ArrayList<>();
        for (Integer v : vertices) {
            Integer distance = bdls.getDistance(graphRead, v);
            if (!s.contains(v) && distance != null
                    && distance <= 1 //                    && scount[v] < kr[v]
                    ) {
                verticesElegiveis.add(v);
            }
        }

        if (verbose) {
            System.out.println("vertices elegiveis " + verticesElegiveis.size());
//            System.out.println("s: " + s);
        }
        int menortRef = menorT + tamanhoReduzido + 1;

        for_p:
//        for (int h = 0; h < ltmp.size() / 2; h++) {

        for (int h = 0; h < ltmp.size(); h++) {
            Integer x = ltmp.get(h);
            if (degree[x] < kr[x] || !s.contains(x)) {
                continue;
            }
            Integer get = tamanhoT.get(x);
            if (get == null || get > menortRef) {
                if (scount[x] < kr[x] - 1) {
                    continue;
                }
            }
            if (verbose) {
//                System.out.println("  - tentando v " + h + "/" + (ltmp.size() - 1));
            }
//            Collection<Integer> nssY = graphRead.getNeighborsUnprotected(x);
            Collection<Integer> nsY = new LinkedHashSet<>();
            for (Integer ny : graphRead.getNeighborsUnprotected(x)) {
                if (!s.contains(ny)
                        && scount[ny] <= kr[ny] + 1) {
                    nsY.add(ny);
                }
            }
            for (int j = h + 1; j < ltmp.size(); j++) {
                Integer y = ltmp.get(j);
                Collection<Integer> nsX = graphRead.getNeighborsUnprotected(y);
                boolean xydisjoint = Collections.disjoint(nsX, nsY);
                if (degree[y] < kr[y]
                        || !s.contains(y)
                        || xydisjoint) {
                    continue;
                }

                if (verbose) {
//                    System.out.println("     -- tentando x " + j + "/" + (ltmp.size() - 1));
                }
                Set<Integer> t = new LinkedHashSet<>(s);
                t.remove(x);
                t.remove(y);

                int contadd = 0;

                int[] aux = auxb;

                for (int i = 0; i < aux.length; i++) {
                    aux[i] = 0;
                    pularAvaliacao[i] = -1;
                }

                mustBeIncluded.clear();
                for (Integer iv : t) {
                    Integer v = iv;
                    mustBeIncluded.add(v);
                    aux[v] = kr[v];
                }
                while (!mustBeIncluded.isEmpty()) {
                    Integer verti = mustBeIncluded.remove();
                    contadd++;
                    Collection<Integer> neighbors = graphRead.getNeighborsUnprotected(verti);
                    for (Integer vertn : neighbors) {
                        if (aux[vertn] <= kr[vertn] - 1) {
                            aux[vertn] = aux[vertn] + 1;
                            if (aux[vertn] == kr[vertn]) {
                                mustBeIncluded.add(vertn);
                            }
                        }
                    }
                    aux[verti] += kr[verti];
                }
                int c = 0;
                for (Integer z : verticesElegiveis) {
                    c++;
                    if (aux[z] >= kr[z] || z.equals(x) || z.equals(y)) {
                        continue;
                    }
                    if (pularAvaliacao[z] >= contadd) {
                        continue;
                    }

                    int contz = contadd;

                    mapCount.clear();
                    mapCount.setVal(z, kr[z]);
                    mustBeIncluded.add(z);

                    while (!mustBeIncluded.isEmpty()) {
                        Integer verti = mustBeIncluded.remove();
                        contz++;
                        Collection<Integer> neighbors = graphRead.getNeighborsUnprotected(verti);
                        for (Integer vertn : neighbors) {
                            if ((aux[vertn] + mapCount.getCount(vertn)) >= kr[vertn]) {
                                continue;
                            }
                            Integer inc = mapCount.inc(vertn);
                            if ((inc + aux[vertn]) == kr[vertn]) {
                                mustBeIncluded.add(vertn);
                                pularAvaliacao[vertn] = contadd;
                            }
                        }
                    }

                    if (verbose) {
//                        System.out.println("        --- tentando z " + c + "/" + (verticesElegiveis.size() - 1) + " contz: " + contz + "/" + tamanhoAlvo);
                    }

                    if (contz == tamanhoAlvo) {
//                        if (verbose) {
//                            System.out.println("Reduzido removido: " + x + " " + y + " adicionado " + z);
//                            System.out.println("Na posição x " + "/" + (tmp.size() - 1));
//                            System.out.println(" - Detalhes de v: "
//                                    + x + " tamt: " + tamanhoT.get(x) + " [" + menorT + "," 
////                                    + maiorT 
//                                    + "]");
//                        }
                        for (Integer vertn : nsX) {
                            scount[vertn]--;
                        }
                        for (Integer vertn : graphRead.getNeighborsUnprotected(x)) {
                            scount[vertn]--;
                        }
                        for (Integer vertn : graphRead.getNeighborsUnprotected(z)) {
                            if ((++scount[vertn]) == kr[vertn] && t.contains(vertn)) {
                                t.remove(vertn);
                                Collection<Integer> nn = graphRead.getNeighborsUnprotected(vertn);
                                for (Integer vnn : nn) {
                                    scount[vnn]--;
                                }
                            }
                        }

                        t.add(z);
                        s = t;
                        ltmp = new ArrayList<>(s);
                        h--;
//                        h = 0;
//                        h = h / 2;
                        continue for_p;
                    }
                }

            }
        }
        return s;
    }

    double calcularAvaliacao(double k, double delta) {
        return k / (delta * (delta + 1));
//        return k / (delta * (delta + 1));
//        return (k + 1) - delta;
//        return k / delta;
//        return (k + 1) - delta;
    }

    public static void main(String... args) throws FileNotFoundException, IOException {
        GraphTSSCordasco optss = new GraphTSSCordasco();

        System.out.println("Teste greater: ");

        UndirectedSparseGraphTO<Integer, Integer> graph = null;
//        graph = new UndirectedSparseGraphTO("681-753,681-1381,681-4658,753-1381,753-4658,1381-2630,1381-2819,1381-4220,1381-4658,2630-2819,2630-3088,2630-4220,2819-3088,2819-4220,");

//        graph = UtilGraph.loadGraphG6("S?????????????????w@oK?B??GW@OE?g");
//        graph = UtilGraph.loadGraphG6("S??A?___?O_aOOCGCO?OG@AAB_??Fvw??");
//        graph = UtilGraph.loadGraphG6("Ss_?G?@???coH`CEABGR?AWDe?A_oAR??");
//        graph = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/ca-AstroPh/ca-AstroPh.txt"));
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
        graph = UtilGraph.loadBigDataset(
                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/BlogCatalog/nodes.csv"),
                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/BlogCatalog/edges.csv"));

        System.out.println(graph.toResumedString());
//        op.K = 3;
//        optss.verbose = false;
////        System.out.println("Subgraph: ");
//        UndirectedSparseGraphTO subGraph = opsubgraph.subGraphInduced(graph, Set.of(388, 1129, 1185, 1654, 3584, 3997));
//        System.out.println(subGraph.getEdgeString());
//        System.out.println("Subgraph: ");
//        UndirectedSparseGraphTO subGraph = opsubgraphn.subGraphInduced(graph, Set.of(1381, 3088, 2630));
//        System.out.println(subGraph.getEdgeString());
//        optss.setR(10);
//        optss.setTryMinimal();
        optss.setMarjority(2);
        UtilProccess.printStartTime();
        Set<Integer> buildOptimizedHullSet = optss.tssCordasco(graph);

        UtilProccess.printStartTime();

        System.out.println(
                "S[" + buildOptimizedHullSet.size() + "]: " + buildOptimizedHullSet);

        if (!optss.checkIfHullSet(graph, buildOptimizedHullSet)) {
            throw new IllegalStateException("NOT HULL SET");
        }
    }

    private Set<Integer> tryMinimalAll(UndirectedSparseGraphTO graph, Set<Integer> S) {
        Integer maxVertex = (Integer) graph.maxVertex() + 1;

//        int[] aux = new int[maxVertex];
        scount = new int[maxVertex];
        degree = new int[maxVertex];
        pularAvaliacao = new int[maxVertex];
        auxb = new int[maxVertex];
        bdls = BFSUtil.newBfsUtilSimple(maxVertex);
        bdls.labelDistances(graph, S);
        for (int i = 0; i < maxVertex; i++) {
//            aux[i] = 0;
            pularAvaliacao[i] = -1;
            scount[i] = 0;
            auxb[i] = 0;

        }
        for (Integer v : S) {
            Collection<Integer> ns = graph.getNeighborsUnprotected(v);
            for (Integer nv : ns) {
                scount[nv]++;
            }
        }
        S = tryMinimal(graph, S, graph.getVertexCount());

        S = tryMinimal2Lite(graph, S, graph.getVertexCount());
        return S;
    }

}
