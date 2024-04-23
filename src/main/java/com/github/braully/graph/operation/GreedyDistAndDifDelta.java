package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.generator.GraphGeneratorRandomGilbert;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.BFSUtil;
import util.MapCountOpt;

public class GreedyDistAndDifDelta
        extends AbstractHeuristic implements IGraphOperation {

    static final Logger log = Logger.getLogger(GreedyDistAndDifDelta.class.getSimpleName());
    static final String description = "GreedyDistDifTotal";

    public String getDescription() {
        return description;
    }

    public String getName() {
        return description;
    }

    public GreedyDistAndDifDelta() {
        this.refine = true;
        this.refine2 = true;
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

    int[] skip = null;
    //
    protected BFSUtil bdls;

    protected Set<Integer> touched = new HashSet<>();
    protected MapCountOpt auxCount;
    protected int bestVertice = -1;

    protected double maxDifTotal = 0;
    protected int maxDist = 0;
    protected int maxDelta = 0;
    protected double maxBonusPartial = 0;
    //has uncontaminated vertices on the current component
    protected boolean hasVerticesOnCC = false;

    public Set<Integer> buildTargeSet(UndirectedSparseGraphTO<Integer, Integer> graph) {
        if (graph == null) {
            return null;
        }
        List<Integer> vertices = new ArrayList<>((List<Integer>) graph.getVertices());
        //Sort vertice on reverse order of degree
        vertices.sort(Comparator
                .comparingInt((Integer v) -> -graph.degree(v))
        );
        //
        Set<Integer> targetSet = new LinkedHashSet<>();
        Set<Integer> saux = new LinkedHashSet<>();

        Integer maxVertex = (Integer) graph.maxVertex() + 1;

        int[] aux = new int[maxVertex];
        degree = new int[maxVertex];
        skip = new int[maxVertex];
        auxb = new int[maxVertex];
        N = new Set[maxVertex];

        for (Integer i : vertices) {
            aux[i] = 0;
            skip[i] = -1;
            auxb[i] = -1;
            N[i] = new LinkedHashSet<>(graph.getNeighborsUnprotected(i));
        }
        initKr(graph);

        int countContaminatedVertices = 0;
        //mandatory vertices
        for (Integer v : vertices) {
            degree[v] = graph.degree(v);

            if (degree[v] <= kr[v] - 1) {
                countContaminatedVertices = countContaminatedVertices + addVertToS(v, saux, graph, aux);
            }
            if (kr[v] == 0) {
                countContaminatedVertices = countContaminatedVertices + addVertToAux(v, graph, aux);
            }
        }

        int vertexCount = graph.getVertexCount();
        int offset = 0;

        //BFS for find vertices in current component
        bdls = BFSUtil.newBfsUtilSimple(maxVertex);
        bdls.labelDistances(graph, saux);

        bestVertice = -1;
        auxCount = new MapCountOpt(maxVertex);
        boolean lastDelta = true;
        while (countContaminatedVertices < vertexCount) {
            if (bestVertice != -1 && bdls.isEmpty(graph, bestVertice)) {
                bdls.incBfs(graph, bestVertice);
            }
            bestVertice = -1;
            maxDifTotal = 0;
            maxDelta = 0;
            maxBonusPartial = 0;

            for (Integer w : vertices) {
                //Ignore w if is already contamined OR skip review to next step
                if (aux[w] >= kr[w] || skip[w] >= countContaminatedVertices) {
                    continue;
                }
                // Ignore w if not acessible in current component of G
                int distanceForSaux = bdls.getDistanceSafe(graph, w);
                if (distanceForSaux == -1 && (countContaminatedVertices > 0 && !hasVerticesOnCC)) {
                    continue;
                }

                int wDelta = 0;
                int wPartialBonus = 0;
                int wDifDelta = 0;
                int wDifPartial = 0;
                int wPartial = 0;
                int wDistDetla = 0;

                //Cache
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
                        } else {
//                                if (inc == 1) {
//                                    wPartial++;
//                                    wDifPartial += (kr[vertn] - aux[vertn]);
//                                }
                        }
                    }
                    wDifDelta += (kr[verti] - aux[verti]);
                    wDistDetla += (degree[verti] - kr[verti]) - aux[verti];
                    wDelta++;
                }

                if (bestVertice == -1
                        || wDifDelta > maxDifTotal //                        || (wDelta == maxDifTotal && wPartialBonus > maxBonusPartial)
                        || (wDifDelta == maxDifTotal && wDistDetla > maxDist)) {
                    bestVertice = w;
                    maxDelta = wDelta;
                    maxBonusPartial = wPartialBonus;
                    maxDifTotal = wDifDelta;
                    maxDist = wDistDetla;
                }
            }
            //Ended the current component of G
            if (bestVertice == -1) {
                hasVerticesOnCC = true;
                saux = refineResult(graph, saux, countContaminatedVertices - offset);

                offset = countContaminatedVertices;
                targetSet.addAll(saux);
                saux.clear();
                bdls.clearBfs();
                continue;
            }
            hasVerticesOnCC = false;
            //Add vert to S
            int added = addVertToS(bestVertice, saux, graph, aux);
            if (added > 1) {
                lastDelta = true;
            } else {
                lastDelta = false;
            }
            countContaminatedVertices = countContaminatedVertices + added;
//            bdls.incBfs(graph, bestVertice);
        }
        saux = refineResult(graph, saux, countContaminatedVertices - offset);

        targetSet.addAll(saux);
        saux.clear();
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
        touched.clear();
        while (!mustBeIncluded.isEmpty()) {
            verti = mustBeIncluded.remove();
            Collection<Integer> neighbors = N[verti];
            for (Integer vertn : neighbors) {
                if ((++aux[vertn]) == kr[vertn]) {
                    mustBeIncluded.add(vertn);
//                    touched.add(vertn);
                } else if (aux[vertn] < kr[vertn]) {
                    touched.add(vertn);
                }
            }
            countIncluded++;
        }

//        if (countIncluded == 1) {
        //Recalc v from touched
        for (Integer vx : touched) {
            auxb[vx] = -1;
            for (Integer vnn : N[vx]) {
                auxb[vnn] = -1;
            }
        }
//        }
        return countIncluded;
    }

    protected int[] scount = null;

    public static void main(String... args) throws IOException {
//        System.out.println("Execution Sample: Livemocha database R=2");
        UndirectedSparseGraphTO<Integer, Integer> graph = null;
        GreedyDistAndDifDelta op = new GreedyDistAndDifDelta();
//
////        URI urinode = URI.create("jar:file:data/big/all-big.zip!/Livemocha/nodes.csv");
////        URI uriedges = URI.create("jar:file:data/big/all-big.zip!/Livemocha/edges.csv");
////        URI urinode = URI.create("jar:file:data/big/all-big.zip!/BlogCatalog/nodes.csv");
////        URI uriedges = URI.create("jar:file:data/big/all-big.zip!/BlogCatalog/edges.csv");
//        URI urinode = URI.create("jar:file:data/big/all-big.zip!/Last.fm/nodes.csv");
//        URI uriedges = URI.create("jar:file:data/big/all-big.zip!/Last.fm/edges.csv");
////        URI urinode = URI.create("jar:file:data/big/all-big.zip!/BlogCatalog3/nodes.csv");
////        URI uriedges = URI.create("jar:file:data/big/all-big.zip!/BlogCatalog3/edges.csv");
//        InputStream streamnode = urinode.toURL().openStream();
//        InputStream streamedges = uriedges.toURL().openStream();
//
//        graph = UtilGraph.loadBigDataset(streamnode, streamedges);
//
//        op.setVerbose(true);
//
//        op.setPercent(0.1);
//        UtilProccess.printStartTime();
//        Set<Integer> buildOptimizedHullSet = op.buildTargeSet(graph);
//        UtilProccess.printEndTime();
//
//        System.out.println(
//                "S[" + buildOptimizedHullSet.size() + "]: "
//                + buildOptimizedHullSet
//        );
//
//        boolean checkIfHullSet = op.checkIfHullSet(graph, buildOptimizedHullSet);
//        if (!checkIfHullSet) {
//            System.out.println("ALERT: ----- THE RESULT IS NOT A HULL SET");
////                            throw new IllegalStateException("IS NOT HULL SET");
//        }

        GraphGeneratorRandomGilbert generator = new GraphGeneratorRandomGilbert();
        GraphDensity dens = new GraphDensity();

        op.setK(2);
        graph = new UndirectedSparseGraphTO<>("0-1,1-2,2-3,3-4,4-5,5-0");
//        double dr = dens.density(graph);
//        Set<Integer> findMinHullSetGraph = op.buildTargeSet(graph);
//        System.out.printf("%d %2f", findMinHullSetGraph.size(), dr);
//        if (true) {
//            return;
//        }
//        op.setVerbose(true);
        double ini = 0.1;
        System.out.print("Dens:\t");
        for (double density = ini; density <= 0.6; density += 0.1) {
            System.out.printf("%.1f \t", density);

        }
        System.out.println();

        for (int nv = 1000; nv <= 2000; nv++) {

            System.out.printf("%3d: \t", nv);

            for (double density = ini; density <= 0.9; density += 0.1) {
                graph = generator.generate(nv, density);
                op.setK(2);
                double dr = dens.density(graph);
                Set<Integer> findMinHullSetGraph = op.buildTargeSet(graph);
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
