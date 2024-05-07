package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.MapCountOpt;
import util.UtilProccess;

public class CCMPanizi
        extends AbstractHeuristic implements IGraphOperation {

    static final Logger log = Logger.getLogger(CCMPanizi.class.getSimpleName());
    static final String description = "CCM-Panizi";

    public String getDescription() {
        return description;
    }

    public String getName() {
        return description;
    }

    public CCMPanizi() {
    }

    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Integer hullNumber = 0;
        Set<Integer> minHullSet = null;

        try {
            String inputData = graph.getInputData();
            if (inputData != null) {
                int parseInt = Integer.parseInt(inputData.trim());
                setR(parseInt);
            }
        } catch (Exception e) {

        }

        try {
            minHullSet = buildTargeSet(graph);
            if (minHullSet != null && !minHullSet.isEmpty()) {
                hullNumber = minHullSet.size();
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, null, ex);
        }

        /* Processar a buscar pelo hullset e hullnumber */
        Map<String, Object> response = new HashMap<>();
        response.put("R", this.rTreshold);
        response.put(PARAM_NAME_HULL_NUMBER, hullNumber);
        response.put(PARAM_NAME_HULL_SET, minHullSet);
        response.put(IGraphOperation.DEFAULT_PARAM_NAME_RESULT, hullNumber);
        return response;
    }

    //
    protected Queue<Integer> mustBeIncluded = new ArrayDeque<>();
    protected MapCountOpt auxCount;
    protected int bestVertice = -1;

    protected int maxDelta = 0;
    protected double maxPartial = 0;

    public Set<Integer> buildTargeSet(UndirectedSparseGraphTO<Integer, Integer> graph) {
        if (graph == null) {
            return null;
        }
        List<Integer> vertices = new ArrayList<>((List<Integer>) graph.getVertices());

        Set<Integer> targetSet = new LinkedHashSet<>();

        Integer maxVertex = (Integer) graph.maxVertex() + 1;

        int[] aux = new int[maxVertex];
        degree = new int[maxVertex];
        auxb = new int[maxVertex];
        N = new Set[maxVertex];
        int[] skip = new int[maxVertex];
        for (Integer i : vertices) {
            aux[i] = 0;
            skip[i] = -1;
            N[i] = new LinkedHashSet<>(graph.getNeighborsUnprotected(i));
        }
        initKr(graph);

        int countContaminatedVertices = 0;
        //mandatory vertices
        for (Integer v : vertices) {
            degree[v] = graph.degree(v);

            if (degree[v] <= kr[v] - 1) {
                countContaminatedVertices = countContaminatedVertices + addVertToS(v, targetSet, graph, aux);
            }
            if (kr[v] == 0) {
                countContaminatedVertices = countContaminatedVertices + addVertToAux(v, graph, aux);
            }
        }

        int vertexCount = graph.getVertexCount();

        bestVertice = -1;
        auxCount = new MapCountOpt(maxVertex);

        while (countContaminatedVertices < vertexCount) {
            bestVertice = -1;
            maxDelta = 0;
            maxPartial = 0;

            for (Integer w : vertices) {
                //Ignore w if is already contamined OR skip review to next step
                if (aux[w] >= kr[w] || skip[w] >= countContaminatedVertices) {
                    continue;
                }
                int wDelta = 0;
                int wPartial = 0;

                //Clear and init w contamined count aux variavles
                auxCount.clear();
                auxCount.setVal(w, kr[w]);
                mustBeIncluded.clear();
                mustBeIncluded.add(w);
                //Propagate w contamination
                while (!mustBeIncluded.isEmpty()) {
                    Integer verti = mustBeIncluded.remove();
                    Collection<Integer> neighbors = N[verti];
                    for (Integer vertn : neighbors) {
                        if ((aux[vertn] + auxCount.getCount(vertn)) >= kr[vertn]) {
                            continue;
                        }
                        Integer inc = auxCount.inc(vertn);
                        if ((inc + aux[vertn]) == kr[vertn]) {
                            mustBeIncluded.add(vertn);
                            skip[vertn] = countContaminatedVertices;
                        }
                    }
                    wDelta++;
                }
                //Partial contamination
                for (Integer x : auxCount.keySet()) {
                    if (auxCount.getCount(x) + aux[x] < kr[x]) {
                        wPartial++;
                    }
                }

                if (bestVertice == -1 || (wDelta > maxDelta
                        || (wDelta == maxDelta && wPartial > maxPartial))) {
                    bestVertice = w;
                    maxDelta = wDelta;
                    maxPartial = wPartial;
                }

            }
            countContaminatedVertices = countContaminatedVertices + addVertToS(bestVertice, targetSet, graph, aux);
        }
        if (this.refine) {
            targetSet = refineResultStep1(graph, targetSet, countContaminatedVertices);
        }
        if (this.refine2) {
            targetSet = refineResultStep1(graph, targetSet, countContaminatedVertices);
        }
        return targetSet;
    }

    public int addVertToAux(Integer verti,
            UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux) {
        int countIncluded = 0;
        if (verti == null) {
            return countIncluded;
        }
        if (kr[verti] > 0 && aux[verti] >= kr[verti]) {
            return countIncluded;
        }

        aux[verti] = aux[verti] + kr[verti];
        mustBeIncluded.clear();
        mustBeIncluded.add(verti);
        while (!mustBeIncluded.isEmpty()) {
            verti = mustBeIncluded.remove();
            Collection<Integer> neighbors = N[verti];
            for (Integer vertn : neighbors) {
                if ((++aux[vertn]) == kr[vertn]) {
                    mustBeIncluded.add(vertn);
                }
            }
            countIncluded++;
        }

        return countIncluded;
    }

    public int addVertToS(Integer verti, Set<Integer> s,
            UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux) {
        int countIncluded = 0;
        if (verti == null) {
            return countIncluded;
        }
        if (kr[verti] > 0 && aux[verti] >= kr[verti]) {
            return countIncluded;
        }

        aux[verti] = aux[verti] + kr[verti];
        if (s != null) {
            s.add(verti);
        }
        mustBeIncluded.clear();
        mustBeIncluded.add(verti);
        while (!mustBeIncluded.isEmpty()) {
            verti = mustBeIncluded.remove();
            Collection<Integer> neighbors = N[verti];
            for (Integer vertn : neighbors) {
                if ((++aux[vertn]) == kr[vertn]) {
                    mustBeIncluded.add(vertn);
                }
            }
            countIncluded++;
        }

        return countIncluded;
    }

    protected int[] scount = null;

    public Set<Integer> refineResultStep1(UndirectedSparseGraphTO<Integer, Integer> graphRead,
            Set<Integer> tmp, int tamanhoAlvo) {
        Set<Integer> s = new LinkedHashSet<>(tmp);

        for (Integer v : tmp) {
            Collection<Integer> nvs = N[v];
            int scont = 0;
            for (Integer nv : nvs) {
                if (s.contains(nv)) {
                    scont++;
                }
            }
            if (scont >= kr[v]) {
                s.remove(v);
            }
        }
        return s;
    }

    public Set<Integer> refineResultStep2(UndirectedSparseGraphTO<Integer, Integer> graphRead,
            Set<Integer> tmp, int tamanhoAlvo) {
        Set<Integer> s = tmp;

        if (s.size() <= 1) {
            return s;
        }

        if (verbose) {
            System.out.println("tentando reduzir: " + s.size());
//            System.out.println("s: " + s);
        }
        int cont = 0;
        for (Integer v : tmp) {
            cont++;
            if (graphRead.degree(v) < kr[v]) {
                continue;
            }
            Set<Integer> t = new LinkedHashSet<>(s);
            t.remove(v);

            int contadd = 0;
            int[] aux = auxb;

            for (int i = 0; i < aux.length; i++) {
                aux[i] = 0;
            }

            mustBeIncluded.clear();
            for (Integer iv : t) {
                mustBeIncluded.add(iv);
                aux[iv] = kr[iv];
            }
            while (!mustBeIncluded.isEmpty()) {
                Integer verti = mustBeIncluded.remove();
                contadd++;
                Collection<Integer> neighbors = N[verti];
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
                if (verbose) {
                    System.out.println(" - removido: " + v + " na pos " + cont + "/" + s.size() + " det " + v + ": " + degree[v]
                            + "/" + kr[v] + " " + ((float) kr[v] * 100 / (float) degree[v]));

                }
                s = t;
            }
        }
        if (verbose) {
            int delt = tmp.size() - s.size();
            if (delt > 0) {
                System.out.println(tmp.size() + "/" + s.size() + " removido " + delt + " vertices");
            }
        }
        return s;
    }

    @Override
    public Set<Integer> refineResult(UndirectedSparseGraphTO<Integer, Integer> graph, Set<Integer> s, int targetSize) {
        s = refineResultStep1(graph, s, targetSize);
//        s = refineResultStep2(graph, s, targetSize);
        return s;
    }

    public static void main(String... args) throws IOException {
        System.out.println("Execution Sample: Livemocha database R=2");
        UndirectedSparseGraphTO<Integer, Integer> graph = null;
        CCMPanizi op = new CCMPanizi();

//        URI urinode = URI.create("jar:file:data/big/all-big.zip!/Livemocha/nodes.csv");
//        URI uriedges = URI.create("jar:file:data/big/all-big.zip!/Livemocha/edges.csv");
        URI urinode = URI.create("jar:file:data/big/all-big.zip!/BlogCatalog/nodes.csv");
        URI uriedges = URI.create("jar:file:data/big/all-big.zip!/BlogCatalog/edges.csv");
        InputStream streamnode = urinode.toURL().openStream();
        InputStream streamedges = uriedges.toURL().openStream();

        graph = UtilGraph.loadBigDataset(streamnode, streamedges);

        op.setVerbose(true);

        op.setPercent(0.7);
        UtilProccess.printStartTime();
        Set<Integer> buildOptimizedHullSet = op.buildTargeSet(graph);
        UtilProccess.printEndTime();

        System.out.println(
                "S[" + buildOptimizedHullSet.size() + "]: "
                + buildOptimizedHullSet
        );
    }

}
