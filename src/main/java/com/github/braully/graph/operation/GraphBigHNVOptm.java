package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import static com.github.braully.graph.operation.GraphHNVOptm.allParameters;
import static com.github.braully.graph.operation.GraphHNVOptm.pbonusParcial;
import static com.github.braully.graph.operation.GraphHNVOptm.pbonusParcialNormalizado;
import static com.github.braully.graph.operation.GraphHNVOptm.pbonusTotal;
import static com.github.braully.graph.operation.GraphHNVOptm.pbonusTotalNormalizado;
import static com.github.braully.graph.operation.GraphHNVOptm.pdeltaHsi;
import static com.github.braully.graph.operation.GraphHNVOptm.pdificuldadeParcial;
import static com.github.braully.graph.operation.GraphHNVOptm.pdificuldadeTotal;
import static com.github.braully.graph.operation.GraphHNVOptm.pgrau;
import static com.github.braully.graph.operation.GraphHNVOptm.pprofundidadeS;
import static com.github.braully.graph.operation.GraphHullNumber.PARAM_NAME_HULL_NUMBER;
import static com.github.braully.graph.operation.GraphHullNumber.PARAM_NAME_HULL_SET;
import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.log4j.Logger;
import util.BFSUtil;
import util.MapCountOpt;
import util.UtilProccess;

public class GraphBigHNVOptm
        extends AbstractHeuristic implements IGraphOperation {

    public boolean startVertice = true;
    public boolean marjorado = false;

    public boolean checkbfs = false;
    public boolean checkstartv = false;
    public boolean checkDeltaHsi = false;

    private static final Logger log = Logger.getLogger(GraphBigHNVOptm.class);

    static final String description = "HHnV2Big";
    int etapaVerbose = -1;
    boolean checkaddirmao = true;
    boolean rollbackEnable = false;
    //

    {
//        parameters.put(type, verbose)
    }

    @Override
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
        if (pularAvaliacaoOffset) {
            sb.append(":pularAva");
        }
        return sb.toString();
    }

    public GraphBigHNVOptm() {
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
    boolean esgotado = false;
    int bestVertice = -1;
    int maiorGrau = 0;
    int maiorGrauContaminacao = 0;
    int maiorDeltaHs = 0;
    int maiorContaminadoParcialmente = 0;
    double maiorBonusParcialNormalizado = 0.0;
    double maiorDificuldadeTotal = 0;
    double maiorBonusTotal = 0;
    double maiorBonusTotalNormalizado = 0;
    double maiorBonusParcial = 0;
    double maiorDificuldadeParcial = 0;
    int maiorProfundidadeS = 0;
    int maiorProfundidadeHS = 0;
    int maiorAux = 0;
    double maiorDouble = 0;
    Queue<Integer> mustBeIncluded = new ArrayDeque<>();
    MapCountOpt mapCount;
    List<Integer> melhores = new ArrayList<Integer>();

    protected Set<Integer> buildOptimizedHullSetFromStartVertice(UndirectedSparseGraphTO<Integer, Integer> graph,
            Integer v, Set<Integer> sini, int[] auxini, int sizeHsini, List<Integer> verticeStart) {
//        Set<Integer> s = new HashSet<>(sini);
//        List<Integer> verticesInteresse = new ArrayList<>();
        Set<Integer> s = new LinkedHashSet<>(sini);
        Collection<Integer> vertices = graph.getVertices();
        int vertexCount = graph.getVertexCount();
        Integer maxVertex = graph.maxVertex();
        int[] aux = auxini.clone();
        int sizeHs = sizeHsini;
        if (verbose) {
            System.out.println("Sini-Size: " + sini.size());
            System.out.println("Sini: " + sini);
        }
        if (v != null) {
            if (verbose) {
                System.out.println("Start vertice: " + v);
            }
            sizeHs += addVertToS(v, s, graph, aux);
        }
        if (verticeStart != null) {
            for (Integer vi : verticeStart) {
                sizeHs += addVertToS(vi, s, graph, aux);
            }
        }
//        BFSDistanceLabeler<Integer, Integer> bdls = new BFSDistanceLabeler<>();
        BFSDistanceLabeler<Integer, Integer> bdlhs = new BFSDistanceLabeler<>();
        BFSUtil bdls = BFSUtil.newBfsUtilSimple(maxVertex + 1);
        bdls.labelDistances(graph, s);
        int commit = sini.size();

        bestVertice = -1;

        mapCount = new MapCountOpt(maxVertex + 1);
//        MapCountOpt mapCountS = new MapCountOpt(maxVertex + 1);

//        for (Integer vt : sini) {
//            Collection<Integer> ns = graph.getNeighborsUnprotected(vt);
//            for (Integer vnn : ns) {
//                mapCountS.inc(vnn);
//            }
//        }
        while (sizeHs < vertexCount) {
            if (bestVertice != -1) {
                bdls.incBfs(graph, bestVertice);
            }

            bestVertice = -1;
            maiorGrau = 0;
            maiorGrauContaminacao = 0;
            maiorDeltaHs = 0;
            maiorContaminadoParcialmente = 0;
            maiorBonusParcialNormalizado = 0;
            maiorDificuldadeTotal = 0;
            maiorBonusTotal = 0;
            maiorProfundidadeS = 0;
            maiorProfundidadeHS = 0;
            maiorAux = 0;
            maiorDouble = 0;
            melhores.clear();
            if (etapaVerbose == s.size()) {
                System.out.println("- Verbose etapa: " + etapaVerbose);
                System.out.println("Size:");
                System.out.println(" * s: " + s.size());
                System.out.println(" * hs: " + sizeHs);
                System.out.println(" * n: " + vertexCount);
                System.out.printf("- vert: del conta pconta prof aux grau\n");
            }
//            void escolherMelhorVertice(UndirectedSparseGraphTO<Integer, Integer> graph,
//            int[] aux, Collection<Integer> vertices, BFSUtil bdls, int sizeHs) 
            escolherMelhorVertice(graph, aux, vertices, bdls, sizeHs);
            if (etapaVerbose == s.size()) {
                System.out.println(" - " + bestVertice);
                System.out.println(" - " + melhores);
                for (Integer ml : melhores) {
                    System.out.println("  -- " + ml + " [" + graph.getNeighborsUnprotected(ml).size() + "]: " + graph.getNeighborsUnprotected(ml));
                }

            }
            if (bestVertice == -1) {
                esgotado = true;
                continue;
            }
            if (maiorDeltaHs == 1 && esgotado) {

            }
            esgotado = false;
            sizeHs = sizeHs + addVertToS(bestVertice, s, graph, aux);
//            if (sizeHs < vertexCount) {
//                bdl.labelDistances(graph, s);
//            }
        }

//        System.out.println("Vertices de interesse[" + verticesInteresse.size() + "]: ");
        if (tryMiminal()) {
            s = tryMinimal(graph, s);
        }
//        s = tryMinimal2(graph, s);
        return s;
    }

    public double trans(double x) {
        if (x == 0) {
            return x;
        } else {
            return -x;
        }
    }

    public int trans(int x) {
        if (x == 0) {
            return x;
        } else {
            return -x;
        }
    }

    protected boolean isGreaterSimple(int... compare) {
        boolean ret = false;
        int i = 0;
        while (i < compare.length - 2
                && compare[i] == compare[i + 1]) {
            i += 2;
        }
        if (i <= compare.length - 2 && compare[i] > compare[i + 1]) {
            ret = true;
        }
        return ret;
    }

    public Boolean isGreater(double... compare) {
        Boolean ret = false;
        int i = 0;
        while (i < compare.length - 2 && compare[i] == compare[i + 1]) {
            i += 2;
        }
        if (i <= compare.length - 2) {
            if (compare[i] > compare[i + 1]) {
                ret = true;
            } else if (compare[i] == compare[i + 1]) {
                ret = null;
            }
        }
        return ret;
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
            Collection<Integer> neighbors = graph.getNeighborsUnprotected(verti);
            for (Integer vertn : neighbors) {
                if ((++scount[vertn]) == kr[vertn] && s.contains(vertn)) {
                    if (verbose) {
                        System.out.println("Scount > kr: " + vertn + " removendo de S ");
                    }
                    s.remove(vertn);
                }
            }
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
    public boolean pularAvaliacaoOffset = true;
    int[] ofssetPularAvaliacao = null;
    int[] scount = null;
    int[] degree = null;
//    @Override

    public Set<Integer> buildOptimizedHullSet(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        List<Integer> vertices = new ArrayList<>((List<Integer>) graphRead.getVertices());
        Set<Integer> hullSet = null;
        Integer vl = null;
        Set<Integer> sini = new LinkedHashSet<>();

        int vertexCount = (Integer) graphRead.maxVertex() + 1;
        int[] auxini = new int[vertexCount];
        scount = new int[vertexCount];
        degree = new int[vertexCount];
        ofssetPularAvaliacao = new int[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            auxini[i] = 0;
            ofssetPularAvaliacao[i] = -1;
            scount[i] = 0;

        }
        initKr(graphRead);
        int sizeHs = 0;
        for (Integer v : vertices) {
            degree[v] = graphRead.degree(v);
            if (degree[v] <= kr[v] - 1) {
                sizeHs = sizeHs + addVertToS(v, sini, graphRead, auxini);
            }
            if (kr[v] == 0) {
                sizeHs = sizeHs + addVertToAux(v, graphRead, auxini);
            }
        }
//        vertices.sort(Comparator
//                .comparingInt((Integer v) -> -graphRead.degree(v))
//                .thenComparing(v -> -v));

//        int total = graphRead.getVertexCount();
//        int cont = 0;
//        int ret = total & cont;
//        Integer vi = vertices.get(0);
        List<Integer> verticeStart = new ArrayList<>();
//        Integer bestN = null;

//        BFSDistanceLabeler<Integer, Integer> bdl = new BFSDistanceLabeler<>();
//        TreeSet<Integer> verts = new TreeSet<>(vertices);
//        while (!verts.isEmpty()) {
//            Integer first = verts.pollFirst();
//            bestN = first;
//
//            bdl.labelDistances(graphRead, first);
//            for (Integer v : vertices) {
//                if (bdl.getDistance(graphRead, v) >= 0) {
//                    verts.remove(v);
//                    if (graphRead.degree(v) > graphRead.degree(bestN)) {
//                        bestN = v;
//                    }
//                }
//            }
////            verticeStart.add(bestN);
////            sini.add(bestN);
//        }
//        for (Integer v : graphRead.getNeighborsUnprotected(vi)) {
//            if (bestN == null) {
//                bestN = v;
//            } else if (graphRead.degree(v) > graphRead.degree(bestN)
//                    || (graphRead.degree(bestN) == graphRead.degree(v)
//                    && bestN > v)) {
//                bestN = v;
//            }
//        }
//        verticeStart.add(vi);
//        verticeStart.add(bestN);
//        for (Integer v : verticeStart) {
//            if (sini.contains(v)) {
//                continue;
//            }
//            if (verbose) {
////                System.out.println("Trying ini vert: " + v);
////                UtilProccess.printCurrentItme();
//
//            }
        Integer v = null;
        int degreev = -1;
        if (startVertice) {

            for (Integer vi : vertices) {
                if (v == null) {
                    v = vi;
                    degreev = degree[vi];

                } else {
                    int degreeVi = degree[vi];
                    if (degreeVi > degreev) {
                        v = vi;
                        degreev = degreeVi;
                    } else if (degreeVi == degreev && vi > v) {
                        v = vi;
                        degreev = degreeVi;
                    }
                }
            }
            if (checkstartv) {
                vertices.sort(Comparator
                        .comparingInt((Integer vi) -> -graphRead.degree(vi))
                        .thenComparing(vi -> -vi));
                Integer vtmp = vertices.get(0);
                if (!vtmp.equals(v)) {
                    System.err.println("Start vertices diferetnes: " + v + " " + vtmp);
                }
            }
        }
        Set<Integer> tmp = buildOptimizedHullSetFromStartVertice(graphRead, v, sini, auxini, sizeHs,
                verticeStart);
//        tmp = tryMinimal(graphRead, tmp);
        if (hullSet == null) {
            hullSet = tmp;
//                vl = v;
        }
//            else if (tmp.size() < hullSet.size()) {
//                if (verbose) {
//                    System.out.println("Melhorado em: " + (hullSet.size() - tmp.size()));
//                    System.out.println(" em i " + v + " vindo de " + vl);
//                    System.out.println("d(" + v + ")=" + graphRead.degree(v) + " d(" + vl + ")=" + graphRead.degree(vl));
//                    System.out.println(hullSet);
//                    System.out.println(tmp);
//                }
//                hullSet = tmp;
//            }
//            cont++;
//            if (verbose) {
////                UtilProccess.printCurrentItmeAndEstimated(total - cont);
////                System.out.println(" s size: " + tmp.size());
//            }
//        }
        if (hullSet == null) {
            hullSet = sini;

        }
//        if (!checkIfHullSet(graphRead, hullSet)) {
//            throw new IllegalStateException("NOT HULL SET");
//        }
        return hullSet;
    }

    public void printPesoAux(int[] auxb) {
        int peso = 0;
        for (int i = 0; i < auxb.length; i++) {
            peso = peso + auxb[i];
        }
        System.out.print("{" + peso + "}");
        UtilProccess.printArray(auxb);
    }

    void escolherMelhorVertice(UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux, Collection<Integer> vertices, BFSUtil bdls, int sizeHs) {
        for (Integer i : vertices) {
            //Se vertice já foi adicionado, ignorar
            if (aux[i] >= kr[i]) {
                continue;
            }
            int profundidadeS = bdls.getDistanceSafe(graph, i);
            if (profundidadeS == -1 && (sizeHs > 0 && !esgotado)) {
                continue;
            }
            if (pularAvaliacaoOffset
                    && ofssetPularAvaliacao[i] >= sizeHs) {
                continue;
            }

            int grauContaminacao = 0;
            int contaminadoParcialmente = 0;
            double bonusParcialNormalizado = 0;
            double bonusTotalNormalizado = 0;
            double bonusParcial = 0;
            double dificuldadeParcial = 0;
            double ddouble = 0;
            double bonusTotal = 0;
            double dificuldadeTotal = 0;
            int grauI = degree[i];

            double bonusHs = 0;
            double dificuldadeHs = 0;
            mustBeIncluded.clear();
            mapCount.clear();
            mustBeIncluded.add(i);
            mapCount.setVal(i, kr[i]);
//                System.out.println(s.size() + "-avaliando: " + i);
            int di = 0;
            while (!mustBeIncluded.isEmpty()) {
                Integer verti = mustBeIncluded.remove();
                Collection<Integer> neighbors = graph.getNeighborsUnprotected(verti);
                for (Integer vertn : neighbors) {
                    if (vertn.equals(verti)
                            || vertn.equals(i)
                            || (aux[vertn] + mapCount.getCount(vertn)) >= kr[vertn]) {
                        continue;
                    }
                    Integer inc = mapCount.inc(vertn);
                    if ((inc + aux[vertn]) == kr[vertn]) {
                        mustBeIncluded.add(vertn);
//                        bonusHs += degree[vertn] - kr[vertn];
//                        dificuldadeHs += (kr[vertn] - aux[vertn]);
                        ofssetPularAvaliacao[vertn] = sizeHs;
                    }
                }
                double bonus = degree[verti] - kr[verti];
                double dificuldade = (kr[verti] - aux[verti]);

                bonusHs += bonus;
                dificuldadeHs += (kr[verti] - aux[verti]);
//                pularAvaliacao[verti] = sizeHs;
                profundidadeS += bdls.getDistanceSafe(graph, verti) + 1;
                grauContaminacao++;
                bonusTotalNormalizado += (bonus / grauContaminacao);
                di += degree[verti];
            }

            for (Integer x : mapCount.keySet()) {
                if (mapCount.getCount(x) + aux[x] < kr[x]) {
                    int dx = degree[x];
//                        double bonus = Math.max(1, dx - kr[x]);
//                        double bonus = kr[x] - dx;
                    double bonus = dx - kr[x];
                    bonusParcial += bonus;
                    double dificuldade = (kr[x] - aux[x]);
                    dificuldadeParcial += dificuldade;
                    contaminadoParcialmente++;
                }
            }

//                dificuldadeTotal = kr[i] - aux[i];
//                bonusTotal = grauI - kr[i];
            grauI = di;
            bonusTotal = bonusHs;
            dificuldadeTotal = dificuldadeHs;
            bonusTotalNormalizado = bonusTotal / grauContaminacao;
            bonusParcialNormalizado = bonusParcial / contaminadoParcialmente;
            int deltaHsi = grauContaminacao;

            if (checkDeltaHsi) {
                int[] auxb = aux.clone();
                int deltaHsib = addVertToAux(i, graph, auxb);
                if (deltaHsi != deltaHsib) {
                    System.err.println("fail on deltahsi: " + deltaHsi + "/" + deltaHsib);
                }
            }
            //Contabilizar quantos vertices foram adicionados
//                for (int j = 0; j < vertexCount; j++) {
//                    if (auxb[j] >= K) {
//                        grauContaminacao++;
//                    }
//                    if (auxb[j] > 0 && auxb[j] < K) {
//                        contaminadoParcialmente++;
//                    }
//                }

            ddouble = contaminadoParcialmente / degree[i];
//                int profundidadeHS = bdlhs.getDistance(graph, i);

//                if (etapaVerbose == s.size()) {
//                    System.out.printf(" * %3d: %3d %3d %3d %3d %3d %3d \n",
//                            i, deltaHsi, grauContaminacao,
//                            contaminadoParcialmente, profundidadeS, aux[i], di);
//                }
//                System.out.printf("- vert: del conta pconta prof aux grau");
//                System.out.printf(" %d: ");
            if (bestVertice == -1) {
                melhores.clear();
                melhores.add(i);
                maiorDeltaHs = deltaHsi;
                maiorGrauContaminacao = grauContaminacao;
                maiorContaminadoParcialmente = contaminadoParcialmente;
                maiorBonusParcialNormalizado = bonusParcialNormalizado;
                maiorBonusTotal = bonusTotal;
                maiorDificuldadeTotal = dificuldadeTotal;
                bestVertice = i;
                maiorProfundidadeS = profundidadeS;
//                    maiorProfundidade = bdl.getDistance(graph, i);
                maiorDouble = ddouble;
                maiorAux = aux[i];
                maiorGrau = di;
                maiorBonusTotalNormalizado = bonusTotalNormalizado;
                maiorBonusParcial = bonusParcial;
                maiorDificuldadeParcial = dificuldadeParcial;
            } else {

                double[] list = new double[parameters.size() * 2];
                int cont = 0;
                for (String p : parameters.keySet()) {
                    Boolean get = parameters.get(p);
                    double p1 = 0, p2 = 0;
                    if (get != null) {
                        switch (p) {
                            case pdeltaHsi:
                                p1 = deltaHsi;
                                p2 = maiorDeltaHs;
                                break;
                            case pbonusTotal:
                                p1 = bonusTotal;
                                p2 = maiorBonusTotal;
                                break;
                            case pbonusParcial:
                                p1 = bonusParcial;
                                p2 = maiorBonusParcial;
                                break;
                            case pbonusParcialNormalizado:
                                p1 = bonusParcialNormalizado;
                                p2 = maiorBonusParcialNormalizado;
                                break;
                            case pbonusTotalNormalizado:
                                p1 = bonusParcialNormalizado;
                                p2 = maiorBonusTotalNormalizado;
                                break;
                            case pdificuldadeTotal:
                                p1 = dificuldadeTotal;
                                p2 = maiorDificuldadeTotal;
                                break;
                            case pdificuldadeParcial:
                                p1 = dificuldadeParcial;
                                p2 = maiorDificuldadeParcial;
                                break;
                            case pprofundidadeS:
                                p1 = profundidadeS;
                                p2 = maiorProfundidadeS;
                                break;
                            case pgrau:
                                p1 = di;
                                p2 = maiorGrau;
                                break;
                            default:
                                break;
                        }
                        if (get) {
                            list[cont++] = p1;
                            list[cont++] = p2;
                        } else {
                            list[cont++] = -p1;
                            list[cont++] = -p2;
                        }
                    }
                }
                Boolean greater = isGreater(list);
//                Boolean greater = isGreater(deltaHsi, maiorDeltaHs,
//                        // bonusTotal, maiorBonusTotal,
//                        // trans(dificuldadeTotal), trans(maiorDificuldadeTotal),
//                        // trans(aux[i]), trans(maiorAux),
//                        // profundidadeHS, maiorProfundidadeHS,
//                        bonusTotal, maiorBonusTotal,
//                        bonusParcial, maiorBonusParcial
//                //                        bonusTotalNormalizado, maiorBonusTotalNormalizado,
//                //                        bonusParcialNormalizado, maiorBonusParcialNormalizado
//                //                        trans(dificuldadeTotal), trans(maiorDificuldadeTotal)
//                //                        profundidadeS, maiorProfundidadeS
//
//                // contaminadoParcialmente, maiorContaminadoParcialmente
//                // (int) Math.round(ddouble * 10), (int) Math.round(maiorDouble * 10),
//                );
                if (greater == null) {
                    melhores.add(i);
                } else if (greater) {
                    melhores.clear();
                    melhores.add(i);
                    maiorDeltaHs = deltaHsi;
                    maiorGrauContaminacao = grauContaminacao;
                    maiorContaminadoParcialmente = contaminadoParcialmente;
                    maiorBonusParcialNormalizado = bonusParcialNormalizado;
                    bestVertice = i;
                    maiorProfundidadeS = profundidadeS;
                    maiorBonusTotal = bonusTotal;
                    maiorDificuldadeTotal = dificuldadeTotal;
                    maiorGrau = di;
                    maiorDouble = ddouble;
                    maiorAux = aux[i];
                    maiorBonusTotalNormalizado = bonusTotalNormalizado;
                    maiorBonusParcial = bonusParcial;
                    maiorDificuldadeParcial = dificuldadeParcial;
                }
            }
        }
    }

    public static void main(String... args) throws IOException {
        GraphBigHNVOptm op = new GraphBigHNVOptm();
        int totalGlobal = 0;
        int melhorGlobal = 0;
        int piorGlobal = 0;

        String[] dataSets = new String[]{
            "ca-GrQc", "ca-HepTh", //            "ca-CondMat", "ca-HepPh",
        //            "ca-AstroPh",
        //            "Douban",
        //            "Delicious",
        //            "BlogCatalog3",
        //            //            "BlogCatalog2",
        //            //            "Livemocha",
        //            "BlogCatalog",
        //            //            "BuzzNet",
        //            "Last.fm", //             "YouTube2"
        };
//        GraphHullNumberHeuristicV5Tmp heur = new GraphHullNumberHeuristicV5Tmp();
        List<String> parametros = new ArrayList<>();
        parametros.addAll(List.of(
                pdeltaHsi, pdificuldadeTotal,
                pbonusTotal, pbonusParcial, pdificuldadeParcial,
                pbonusTotalNormalizado, pbonusParcialNormalizado,
                //                pprofundidadeS, 
                pgrau
        //        , pgrau, paux
        ));
        UndirectedSparseGraphTO<Integer, Integer> graph = null;
        //

        for (int t = 1; t <= 3; t++) {
            for (int r = 4; r <= 7; r++) {
                int cont = 0;
                MapCountOpt contMelhor = new MapCountOpt(allParameters.size() * 100);

                for (String s : dataSets) {
                    System.out.println("\n-DATASET: " + s);

                    try {
                        graph = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/" + s + "/nodes.csv"),
                                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/" + s + "/edges.csv"));
                    } catch (FileNotFoundException e) {
                        graph = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/" + s + "/" + s + ".txt"));
                    }
                    if (graph == null) {
                        System.out.println("Fail to Load GRAPH: " + s);
                    }
                    op.setR(r);
                    Integer melhor = null;
                    List<int[]> melhores = new ArrayList<>();
//                for (int ip = 0; ip < allParameters.size(); ip++) {
                    op.pularAvaliacaoOffset = true;
                    Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(parametros.size(), t);
                    while (combinationsIterator.hasNext()) {

                        int[] currentSet = combinationsIterator.next();

                        op.resetParameters();
                        for (int ip : currentSet) {
                            String p = parametros.get(ip);
                            op.setParameter(p, true);
                        }
                        Set<Integer> optmHullSet = op.buildOptimizedHullSet(graph);
                        String name = op.getName();
                        int res = optmHullSet.size();
                        String out = "R\t g" + cont++ + "\t r"
                                + r + "\t" + name
                                + "\t" + res + "\n";
                        if (melhor == null) {
                            melhor = res;
                            melhores.add(currentSet);
                        } else if (melhor == res) {
                            melhores.add(currentSet);
                        } else if (melhor > res) {
                            melhor = res;
                            melhores.clear();
                            melhores.add(currentSet);
                        }
                        System.out.println(out);
                        if (t > 1) {
                            int[] currentRerverse = currentSet.clone();
                            for (int i = 0; i < currentSet.length; i++) {
                                currentRerverse[i] = currentSet[(currentSet.length - 1) - i];
                            }

                            op.resetParameters();
                            for (int ip : currentRerverse) {
                                String p = parametros.get(ip);
                                op.setParameter(p, true);
                            }
                            optmHullSet = op.buildOptimizedHullSet(graph);
                            name = op.getName();
                            res = optmHullSet.size();
                            out = "R\t g" + cont++ + "\t r"
                                    + r + "\t" + name
                                    + "\t" + res + "\n";
                            if (melhor == null) {
                                melhor = res;
                                melhores.add(currentRerverse);
                            } else if (melhor == res) {
                                melhores.add(currentRerverse);
                            } else if (melhor > res) {
                                melhor = res;
                                melhores.clear();
                                melhores.add(currentRerverse);
                            }
                            System.out.println(out);
                        }
                    }
//                for (Integer i : melhores) {
                    for (int[] ip : melhores) {
                        int i = array2idx(ip);
                        contMelhor.inc(i);
                    }
                    cont++;
                    System.out.println("\n---------------");
                    System.out.println("Melhor parcial r: " + r + " dataset: " + s + " melhor: " + melhor);

                    Map<String, Integer> map = new HashMap<>();
//            for (int ip = 0; ip < allParameters.size(); ip++) {
//                String p = allParameters.get(ip);
////                System.out.println(p + ": " + contMelhor.getCount(ip));
//                map.put(p, contMelhor.getCount(ip));
//            }
                    for (int[] i : allarrays()) {
                        StringBuilder sb = new StringBuilder();
                        for (int ip : i) {
                            sb.append(parametros.get(ip));
//                    String p = allParameters.get(ip);
                            sb.append("-");
//                System.out.println(p + ": " + contMelhor.getCount(ip));
                        }
                        map.put(sb.toString(), contMelhor.getCount(array2idx(i)));
                    }
                    List<Entry<String, Integer>> entrySet = new ArrayList<>(map.entrySet());
                    entrySet.sort(
                            Comparator.comparingInt(
                                    (Entry<String, Integer> v) -> -v.getValue()
                            )
                                    .thenComparing(v -> v.getKey())
                    );
                    for (Entry<String, Integer> e : entrySet) {
                        String p = e.getKey();
                        System.out.println(p + ": " + e.getValue());
                    }
                }

//            for (int ip = 0; ip < allParameters.size(); ip++) {
//                String p = allParameters.get(ip);
//                System.out.println(p + ": " + contMelhor.getCount(ip));
//            }
            }
        }
    }

    private static int apply(GraphBigHNVOptm op, int[] currentSet, UndirectedSparseGraphTO<Integer, Integer> graph, int cont, int r, Integer melhor, List<int[]> melhores1) {
        op.resetParameters();
        for (int ip : currentSet) {
            String p = allParameters.get(ip);
            op.setParameter(p, true);
        }
        Set<Integer> optmHullSet = op.buildOptimizedHullSet(graph);
        String name = op.getName();
        int res = optmHullSet.size();
        String out = "R\t g" + cont++ + "\t r"
                + r + "\t" + name
                + "\t" + res + "\n";
        if (melhor == null) {
            melhor = res;
            melhores1.add(currentSet);
        } else if (melhor == res) {
            melhores1.add(currentSet);
        } else if (melhor > res) {
            melhor = res;
            melhores1.clear();
            melhores1.add(currentSet);
        }
//                    System.out.print("xls: " + out);
        return cont;
    }

    static Map<Integer, int[]> map = new HashMap<>();

    static int[] offset = new int[]{1, 100};

    public static int array2idx(int[] ip) {
        int cont = 0;
        for (int i = 0; i < ip.length; i++) {
            int ipp = ip[i];
            cont = cont + offset[i] * ipp;
            map.put(cont, ip);
        }
        return cont;
    }

    public static Collection<int[]> allarrays() {
        return map.values();
    }

    public void resetParameters() {
        this.parameters.clear();
    }

    public void setParameter(String p, boolean b) {
        this.parameters.put(p, b);
    }
}
