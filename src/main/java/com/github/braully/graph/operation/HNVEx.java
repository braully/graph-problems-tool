package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import com.github.braully.graph.generator.GraphGeneratorRandomGilbert;
import static com.github.braully.graph.operation.GraphDensity.PARAM_DENSITY;
import static com.github.braully.graph.operation.GraphHullNumber.PARAM_NAME_HULL_NUMBER;
import static com.github.braully.graph.operation.GraphHullNumber.PARAM_NAME_HULL_SET;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import static tmp.DensityHeuristicCompare.INI_V;
import static tmp.DensityHeuristicCompare.MAX_V;
import util.BFSUtil;
import util.MapCountOpt;
import util.UtilProccess;

public class HNVEx
        extends AbstractHeuristic implements IGraphOperation {

    static final Logger log = Logger.getLogger(HNVEx.class.getSimpleName());
    static final String description = "HNVEX";

    GraphDensity graphDensity = new GraphDensity();

    public String getDescription() {
        return description;
    }

    public String getName() {
        return description;
    }

    public HNVEx() {
    }

    int[] skip = null;
    int[] auxb = null;
    //
    protected BFSUtil bdls;

    protected Queue<Integer> mustBeIncluded = new ArrayDeque<>();
    protected MapCountOpt mapCount;
    protected int bestVertice = -1;

    protected double maxDifTotal = 0;
    protected int maxDeltaH = 0;
    protected double maxBonusPartial = 0;
    protected boolean esgotado = false;

    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Integer hullNumber = 0;
        Set<Integer> minHullSet = null;

        try {
            String inputData = graph.getInputData();
            if (inputData != null) {
                int parseInt = Integer.parseInt(inputData.trim());
                setK(parseInt);
            }
        } catch (Exception e) {

        }

        try {
            minHullSet = findHullSet(graph);
            if (minHullSet != null && !minHullSet.isEmpty()) {
                hullNumber = minHullSet.size();
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, null, ex);
        }

        Double density = null;

        try {
            density = graphDensity.density(graph);
        } catch (Exception e) {

        }

        /* Processar a buscar pelo hullset e hullnumber */
        Map<String, Object> response = new HashMap<>();
        response.put("K", 2);
        response.put(PARAM_NAME_HULL_NUMBER, hullNumber);
        response.put(PARAM_DENSITY, density);
        response.put(PARAM_NAME_HULL_SET, minHullSet);
        response.put(IGraphOperation.DEFAULT_PARAM_NAME_RESULT, hullNumber);
        return response;
    }

    public Set<Integer> findHullSet(UndirectedSparseGraphTO<Integer, Integer> graph) {
        return buildHullSet(graph);

    }

    public List<Integer> getVertices(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        List<Integer> vertices = new ArrayList<>((List<Integer>) graphRead.getVertices());
        vertices.sort(Comparator
                .comparingInt((Integer v) -> -graphRead.degree(v))
        //                .thenComparing(v -> -v)
        );
        return vertices;
    }

    public Set<Integer> buildHullSet(UndirectedSparseGraphTO<Integer, Integer> graph) {
        List<Integer> vertices = getVertices(graph);
        Set<Integer> hullSet = new LinkedHashSet<>();
        Set<Integer> s = new LinkedHashSet<>();

        Integer maxVertex = (Integer) graph.maxVertex() + 1;

        int[] aux = new int[maxVertex];
        degree = new int[maxVertex];
        skip = new int[maxVertex];
        auxb = new int[maxVertex];
        scount = new int[maxVertex];
        for (int i = 0; i < maxVertex; i++) {
            aux[i] = 0;
            skip[i] = -1;
            scount[i] = 0;
        }
        initKr(graph);

        int sizeHs = 0;
        for (Integer v : vertices) {
            degree[v] = graph.degree(v);
            if (degree[v] <= kr[v] - 1) {
                sizeHs = sizeHs + addVertToS(v, s, graph, aux);
            }
            if (kr[v] == 0) {
                sizeHs = sizeHs + addVertToAux(v, graph, aux);
            }
        }

        int vertexCount = graph.getVertexCount();
        int offset = 0;

        bdls = BFSUtil.newBfsUtilSimple(maxVertex);
        bdls.labelDistances(graph, s);

        bestVertice = -1;

        mapCount = new MapCountOpt(maxVertex);

        while (sizeHs < vertexCount) {
            if (bestVertice != -1) {
                bdls.incBfs(graph, bestVertice);
            }
            bestVertice = -1;
            maxDifTotal = 0;
            maxDeltaH = 0;
            maxBonusPartial = 0;

            for (Integer i : vertices) {
                //Ignore i if is already contamined OR contamined by another checked vertice
                if (aux[i] >= kr[i] || skip[i] >= sizeHs) {
                    continue;
                }
                // Ignore i if not acessible in current component of G
                int profundidadeS = bdls.getDistanceSafe(graph, i);
                if (profundidadeS == -1 && (sizeHs > 0 && !esgotado)) {
                    continue;
                }

                int grauContaminacao = 0;
                double bonusParcial = 0;
                double dificuldadeTotal = 0;
                double dificuldadeHs = 0;

                mapCount.clear();
                mapCount.setVal(i, kr[i]);

                mustBeIncluded.clear();
                mustBeIncluded.add(i);

                while (!mustBeIncluded.isEmpty()) {
                    Integer verti = mustBeIncluded.remove();
                    Collection<Integer> neighbors = graph.getNeighborsUnprotected(verti);
                    for (Integer vertn : neighbors) {
                        if ((aux[vertn] + mapCount.getCount(vertn)) >= kr[vertn]) {
                            continue;
                        }
                        Integer inc = mapCount.inc(vertn);
                        if ((inc + aux[vertn]) == kr[vertn]) {
                            mustBeIncluded.add(vertn);
                            skip[vertn] = sizeHs;
                        }
                    }
//                    double bonus = degree[verti] - kr[verti];
                    double dificuldade = (kr[verti] - aux[verti]);

                    dificuldadeHs += dificuldade;
                    profundidadeS += bdls.getDistanceSafe(graph, verti) + 1;
                    grauContaminacao++;
                }

                for (Integer x : mapCount.keySet()) {
                    if (mapCount.getCount(x) + aux[x] < kr[x]) {
                        int dx = degree[x];
                        double bonus = dx - kr[x];
                        bonusParcial += bonus;
                    }
                }

                dificuldadeTotal = dificuldadeHs;
                int deltaHsi = grauContaminacao;

                if (bestVertice == -1) {
                    bestVertice = i;
                    maxDeltaH = deltaHsi;
                    maxDifTotal = dificuldadeTotal;
                    maxBonusPartial = bonusParcial;
                } else {
                    double rank = dificuldadeTotal * deltaHsi;
                    double rankMaior = maxDifTotal * maxDeltaH;
                    if (rank > rankMaior
                            || (rank == rankMaior && bonusParcial > maxBonusPartial)) {
                        bestVertice = i;
                        maxDeltaH = deltaHsi;
                        maxDifTotal = dificuldadeTotal;
                        maxBonusPartial = bonusParcial;
                    }
                }
            }
            //Ended the current component of G
            if (bestVertice == -1) {
                esgotado = true;
                s = refineResult(graph, s, sizeHs - offset);

                offset = sizeHs;
                hullSet.addAll(s);
                s.clear();
                bdls.clearBfs();
                continue;
            }
            esgotado = false;
            //Add vert to S
            sizeHs = sizeHs + addVertToS(bestVertice, s, graph, aux);
            bdls.incBfs(graph, bestVertice);
        }
        s = refineResultStep2(graph, s, sizeHs - offset);
        s = refineResultStep3(graph, s, sizeHs - offset);

        hullSet.addAll(s);
        s.clear();
        return hullSet;
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
            Collection<Integer> neighbors = graph.getNeighborsUnprotected(verti);
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
            Collection<Integer> neighbors = graph.getNeighborsUnprotected(verti);
            for (Integer vertn : neighbors) {
                if ((++aux[vertn]) == kr[vertn]) {
                    mustBeIncluded.add(vertn);
                }
            }
            countIncluded++;
        }

        return countIncluded;
    }

    Map<Integer, Integer> sizesPartial = new HashMap<>();
    int minimumSize = Integer.MAX_VALUE;
    int reducedCount = 0;

    protected int[] scount = null;

    public Set<Integer> refineResultStep1(UndirectedSparseGraphTO<Integer, Integer> graphRead,
            Set<Integer> tmp, int tamanhoAlvo) {
        Set<Integer> s = new LinkedHashSet<>(tmp);

        for (Integer v : tmp) {
            Collection<Integer> nvs = graphRead.getNeighborsUnprotected(v);
            int scont = 0;
            for (Integer nv : nvs) {
                scount[nv]++;
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

        sizesPartial.clear();
        reducedCount = 0;
        minimumSize = Integer.MAX_VALUE;

        if (s.size() <= 1) {
            return s;
        }

        if (verbose) {
            System.out.println("tentando reduzir: " + s.size());
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
            } else {
                //            int tamt = contadd - t.size();
                int tamt = contadd;
                sizesPartial.put(v, tamt);
                if (tamt < minimumSize) {
                    minimumSize = tamt;
                }
            }
        }
        reducedCount = tmp.size() - s.size();
        return s;
    }

    public Set<Integer> refineResultStep3(UndirectedSparseGraphTO<Integer, Integer> graphRead,
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
        List<Integer> verticesElegiveis = new ArrayList<>(vertices);

        int menortRef = minimumSize + 1 + reducedCount;

        for_p:
        for (int h = 0; h < ltmp.size(); h++) {
            Integer x = ltmp.get(h);
            //
            if (degree[x] < kr[x] || !s.contains(x)) {
                continue;
            }
            Integer get = sizesPartial.get(x);
            if (get == null
                    || get > menortRef) {
                if (scount[x] < kr[x] - 1) {
                    continue;
                }
            }
            for (int j = h + 1; j < ltmp.size(); j++) {
                Integer y = ltmp.get(j);
                if (degree[y] < kr[y]) {
                    continue;
                }

                Set<Integer> t = new LinkedHashSet<>(s);
                t.remove(x);
                t.remove(y);

                int contadd = 0;

                int[] aux = auxb;

                for (int i = 0; i < aux.length; i++) {
                    aux[i] = 0;
                    skip[i] = -1;
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
                    if (skip[z] >= contadd) {
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
                                skip[vertn] = contadd;
                            }
                        }
                    }
                    if (contz == tamanhoAlvo) {
                        t.add(z);
                        s = t;
                        ltmp = new ArrayList<>(s);
                        h = 0;
                        continue for_p;
                    }
                }
            }
        }
        return s;
    }

    public Set<Integer> refineResult(UndirectedSparseGraphTO<Integer, Integer> graph, Set<Integer> s, int targetSize) {
        s = refineResultStep2(graph, s, targetSize);
        s = refineResultStep3(graph, s, targetSize);
        return s;
    }

    public static void main(String... args) throws IOException {
//        System.out.println("Execution Sample: Livemocha database R=2");
        UndirectedSparseGraphTO<Integer, Integer> graph = null;
        HNVEx op = new HNVEx();
//
//        URI urinode = URI.create("jar:file:data/big/all-big.zip!/Livemocha/nodes.csv");
//        URI uriedges = URI.create("jar:file:data/big/all-big.zip!/Livemocha/edges.csv");
//
//        InputStream streamnode = urinode.toURL().openStream();
//        InputStream streamedges = uriedges.toURL().openStream();
//
//        graph = UtilGraph.loadBigDataset(streamnode, streamedges);
//
//        op.setVerbose(true);
//
//        op.setR(2);
//        UtilProccess.printStartTime();
//        Set<Integer> buildOptimizedHullSet = op.buildHullSet(graph);
//        UtilProccess.printEndTime();
//
//        System.out.println(
//                "S[" + buildOptimizedHullSet.size() + "]: "
//                + buildOptimizedHullSet
//        );

//        int cont = 0;
//        String readLine;
//        BufferedReader r = new BufferedReader(new FileReader("/home/strike/Documentos/doutorado/resultado-processamento/mincayley/mincayley-1-95.g6"));
//        while ((readLine = r.readLine()) != null && !readLine.isEmpty()) {
//            try {
//                UndirectedSparseGraphTO ret = UtilGraph.loadGraphG6(readLine);
//
//                op.setK(2);
//                Set<Integer> buildOptimizedHullSet = op.buildHullSet(ret);
//                double density = op.graphDensity.density(ret);
//                int vertexCount = ret.getVertexCount();
//                System.out.println("mincayley " + cont + " n:" + vertexCount + " m:" + ret.getEdgeCount() + "  h:" + buildOptimizedHullSet.size() + " d:" + (density * 10.0));
//                cont++;
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        GraphGeneratorRandomGilbert generator = new GraphGeneratorRandomGilbert();
        GraphDensity dens = new GraphDensity();

//        op.setVerbose(true);
        double ini = 0.1;
        System.out.print("Dens:\t");
        for (double density = ini; density <= 0.6; density += 0.1) {
            System.out.printf("%.1f \t", density);

        }
        System.out.println();

        for (int nv = 1000; nv <= 2000; nv++) {

            System.out.printf("%3d: \t", nv);

            for (double density = ini; density <= 0.6; density += 0.1) {
                graph = generator.generate(nv, density);
                op.setK(2);
                double dr = dens.density(graph);
                Set<Integer> findMinHullSetGraph = op.buildHullSet(graph);
                System.out.printf("%d %2f", findMinHullSetGraph.size(), dr);

                System.out.printf("\t");

                boolean checkIfHullSet = op.checkIfHullSet(graph, findMinHullSetGraph);
                if (!checkIfHullSet) {
                    throw new IllegalStateException("NOT HULL SET");
                }

            }
            System.out.println();
        }
    }
}
