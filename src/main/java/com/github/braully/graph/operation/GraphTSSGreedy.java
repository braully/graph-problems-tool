package com.github.braully.graph.operation;

import com.github.braully.graph.GraphWS;
import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
public class GraphTSSGreedy extends AbstractHeuristic implements IGraphOperation {

    static final String description = "Greedy-Cordasco";

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
        Set s = tssGreedy(graph, reqList);

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

    public Set<Integer> tssGreedy(UndirectedSparseGraphTO graph) {
        return tssGreedy(graph, null);
    }

    public Set<Integer> tssGreedy(UndirectedSparseGraphTO graph, List<Integer> reqList) {
        Set<Integer> S = new LinkedHashSet<>();
        Set<Integer> U = new LinkedHashSet<>(graph.getVertices());
        initKr(graph);
        int n = graph.getVertexCount();

        int[] delta = new int[n];
        int[] k = new int[n];

        Set<Integer>[] N = new Set[n];

        for (Integer v : U) {
            delta[v] = graph.degree(v);
            k[v] = kr[v];
            N[v] = new LinkedHashSet<>(graph.getNeighborsUnprotected(v));
        }

        while (U.size() > 0) {
            Integer v = null;
            int min_d = Integer.MAX_VALUE;
            for (Integer u : U) {
                if (k[u] < min_d) {
                    min_d = k[u];
                    v = u;
                }
            }
            if (k[v] > 0) {
                double max_x = -1;
                for (Integer u : U) {
                    double x = N[u].size();
                    if (x > max_x) {
                        max_x = x;
                        v = u;
                    }
                }
                S.add(v);
            }

            //v será dominado por seus vizinhos
            for (Integer u : N[v]) {
                k[u] = Math.max(0, k[u] - 1);
                N[u].remove(v);
                delta[u] = delta[u] - 1;
            }
            U.remove(v);
        }
        return S;
    }

    public String getTypeProblem() {
        return type;
    }

    public String getName() {
        return description;
    }

    public static void main(String... args) throws FileNotFoundException, IOException {
        GraphTSSGreedy optss = new GraphTSSGreedy();

        System.out.println("Teste greater: ");

        UndirectedSparseGraphTO<Integer, Integer> graph = null;
        graph = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/ca-GrQc/ca-GrQc.txt"));
        System.out.println(graph.toResumedString());
        optss.setR(5);
        optss.setTryMinimal();
        UtilProccess.printStartTime();
        Set<Integer> buildOptimizedHullSet = optss.tssGreedy(graph);

        UtilProccess.printStartTime();

        System.out.println(
                "S[" + buildOptimizedHullSet.size() + "]: " + buildOptimizedHullSet);

        if (!optss.checkIfHullSet(graph, buildOptimizedHullSet)) {
            throw new IllegalStateException("NOT HULL SET");
        }
    }

}
