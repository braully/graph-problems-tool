package com.github.braully.graph.operation;

import heuristic.AbstractHeuristic;
import com.github.braully.graph.GraphWS;
import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import util.UtilParse;
import util.UtilProccess;

/**
 *
 * Reference: Discovering Small Target Sets in Social Networks: A Fast and
 * Effective Algorithm
 */
public class GraphTIPDecomp extends AbstractHeuristic implements IGraphOperation {

    static final String type = "P3-Convexity";
    static final String description = "TIPDecomp";

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
        Set s = tipDecomp(graph, reqList);

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

    public Set<Integer> tipDecomp(UndirectedSparseGraphTO graph) {
        return tipDecomp(graph, null);
    }

    public Set<Integer> tipDecomp(UndirectedSparseGraphTO graph, List<Integer> reqList) {
        Set<Integer> S = new LinkedHashSet<>(graph.getVertices());
        initKr(graph);
        int n = graph.getVertexCount();

        int[] delta = new int[n];
        int[] k = new int[n];
        int[] dist = new int[n];

        Set<Integer>[] N = new Set[n];

        for (Integer v : S) {
            delta[v] = graph.degree(v);
            k[v] = kr[v];
            N[v] = new LinkedHashSet<>(graph.getNeighborsUnprotected(v));
            dist[v] = delta[v] - kr[v];
        }

        boolean flag = true;

        while (flag) {
            Integer v = null;
            for (var vi : S) {
                if (v == null) {
                    v = vi;
                } else if (dist[v] > dist[vi]) {
                    v = vi;
                }
            }
            if (dist[v] == Integer.MAX_VALUE) {
                flag = false;
            } else {
                S.remove(v);
                for (Integer u : N[v]) {
                    if (dist[u] > 0) {
                        dist[u]--;
                    } else {
                        dist[u] = Integer.MAX_VALUE;
                    }
                    N[u].remove(v);
                }
            }
        }
//        S = tryMinimal(graph, S);
        return S;
    }

    public String getTypeProblem() {
        return type;
    }

    public String getName() {
        return description;
    }

    public static void main(String... args) throws FileNotFoundException, IOException {
        GraphTIPDecomp optss = new GraphTIPDecomp();

        System.out.println("Teste greater: ");

        UndirectedSparseGraphTO<Integer, Integer> graph = null;
//        graph = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/ca-GrQc/ca-GrQc.txt"));
        graph = UtilGraph.loadBigDataset(
                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/BlogCatalog3/nodes.csv"),
                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/BlogCatalog3/edges.csv")); 
        System.out.println(graph.toResumedString());
        optss.setR(3);
        UtilProccess.printStartTime();
        Set<Integer> buildOptimizedHullSet = optss.tipDecomp(graph);

        UtilProccess.printStartTime();

        System.out.println(
                "S[" + buildOptimizedHullSet.size() + "]: " + buildOptimizedHullSet);

        if (!optss.checkIfHullSet(graph, buildOptimizedHullSet)) {
            throw new IllegalStateException("NOT HULL SET");
        }
    }
}
