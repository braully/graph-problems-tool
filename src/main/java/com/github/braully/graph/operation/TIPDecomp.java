package com.github.braully.graph.operation;


import heuristic.AbstractHeuristic;
import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import util.UtilProccess;

/**
 *
 * Reference:
 */
public class TIPDecomp extends AbstractHeuristic implements IGraphOperation {

    static final String type = "P3-Convexity";
    static final String description = "TIPDecomp";

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {

        /* Processar a buscar pelo hullset e hullnumber */
        Map<String, Object> response = new HashMap<>();

        try {
            Set<Integer> tipDecomp = tipDecomp(graph);
            response.put("TIPDecomp", tipDecomp);
            response.put(IGraphOperation.DEFAULT_PARAM_NAME_SET, tipDecomp);
            response.put("|TSS|", tipDecomp.size());
            response.put(IGraphOperation.DEFAULT_PARAM_NAME_RESULT, tipDecomp.size());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;
    }

    public Set<Integer> tipDecomp(UndirectedSparseGraphTO graph) {

        Set<Integer> S = new LinkedHashSet<>(graph.getVertices());
        initKr(graph);

        int n = (Integer) graph.maxVertex() + 1;
        int[] delta = new int[n];
        int[] k = new int[n];
        float[] dist = new float[n];

        Set<Integer>[] N = new Set[n];

        for (Integer v : S) {
            k[v] = kr[v];
            dist[v] = graph.degree(v) - k[v];
            N[v] = new LinkedHashSet<>(graph.getNeighborsUnprotected(v));
        }

        boolean flag = true;

        while (flag) {
            Integer v = null;
            float minDist = Float.POSITIVE_INFINITY;
            for (var vi : S) {
                if (dist[vi] >= 0 && dist[vi] < minDist) {
                    v = vi;
                    minDist = dist[vi];
                }
            }
            if (minDist == Float.POSITIVE_INFINITY) {
                flag = false;
            } else {
                for (Integer u : N[v]) {
                    if (dist[u] > 0) {
                        dist[u]--;
                    } else {
                        dist[u] = Float.POSITIVE_INFINITY;
                    }
                }
                S.remove(v);
            }
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
        TIPDecomp optss = new TIPDecomp();

        UndirectedSparseGraphTO<Integer, Integer> graph = null;
//        graph = UtilGraph.loadGraphES("0-2,1-4,2-3,2-4,");
        graph = UtilGraph.loadBigDataset(
                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/Foursquare/nodes.csv"),
                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/Foursquare/edges.csv"));
        System.out.println(graph.toResumedString());
//        optss.setPercent(0.5);
        UtilProccess.printStartTime();
        Set<Integer> buildOptimizedHullSet = optss.tipDecomp(graph);
        System.out.println("buildOptimizedHullSet: " + buildOptimizedHullSet.size());
        UtilProccess.printStartTime();

        if (!optss.checkIfHullSet(graph, buildOptimizedHullSet)) {
            throw new IllegalStateException("NOT HULL SET");
        }
    }
}
