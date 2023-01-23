package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import com.github.braully.graph.generator.GraphGeneratorRandomGilbert;
import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import org.apache.log4j.Logger;
import static tmp.DensityHeuristicCompare.INI_V;
import static tmp.DensityHeuristicCompare.MAX_V;
import util.BFSUtil;
import util.MapCountOpt;
import util.UtilProccess;

public class GraphHullNumberHeuristicV5Tmp3Testes
        extends GraphHullNumberHeuristicV1 implements IGraphOperation {

    public int K = 2;
    public boolean startVertice = true;
    public boolean marjorado = false;

    public boolean checkbfs = false;
    public boolean checkstartv = false;
    public boolean checkDeltaHsi = false;

    private static final Logger log = Logger.getLogger(GraphHullNumberHeuristicV5Tmp3Testes.class);

    static final String description = "HHn-v2";
    int etapaVerbose = -1;
    boolean checkaddirmao = true;
    boolean rollbackEnable = false;

    @Override
    public String getName() {
        return description;
    }

    public GraphHullNumberHeuristicV5Tmp3Testes() {
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

    int bestVertice = -1;
    int maiorGrau = 0;
    int maiorGrauContaminacao = 0;
    int maiorDeltaHs = 0;
    int maiorContaminadoParcialmente = 0;
    int maiorProfundidadeS = 0;
    int maiorProfundidadeHS = 0;
    int maiorAux = 0;
    double maiorDouble = 0;

    protected Set<Integer> buildOptimizedHullSetFromStartVertice(UndirectedSparseGraphTO<Integer, Integer> graph,
            Integer v, Set<Integer> sini, int[] auxini, int sizeHsini, List<Integer> verticeStart) {
//        Set<Integer> s = new HashSet<>(sini);
        List<Integer> verticesInteresse = new ArrayList<>();
        Set<Integer> s = new LinkedHashSet<>(sini);
        Set<Integer> hs = new LinkedHashSet<>();
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
        boolean esgotado = false;
        List<Integer> melhores = new ArrayList<Integer>();
        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        MapCountOpt mapCount = new MapCountOpt(maxVertex + 1);
        MapCountOpt mapCountS = new MapCountOpt(maxVertex + 1);

        for (Integer vt : sini) {
            Collection<Integer> ns = graph.getNeighborsUnprotected(vt);
            for (Integer vnn : ns) {
                mapCountS.inc(vnn);
            }
        }
        while (sizeHs < vertexCount) {
            if (bestVertice != -1) {
                bdls.incBfs(graph, bestVertice);
            }
            if (checkbfs) {
                bdlhs.labelDistances(graph, s);

                for (Integer c : vertices) {
                    Integer distanceSafe = bdls.getDistanceSafe(graph, c);
                    Integer distance = bdlhs.getDistance(graph, c);
                    if (!distance.equals(distanceSafe)) {
                        System.err.println("Fail BFS: vertice " + c + " d1 " + distance + " d2 " + distanceSafe);
                    }
                }
            }

//            for (Integer i : vertices) {
//                if (aux[i] >= K) {
//                    hs.add(i);
//                }
//            }
//            bdlhs.labelDistances(graph, hs);
            bestVertice = -1;
            maiorGrau = 0;
            maiorGrauContaminacao = 0;
            maiorDeltaHs = 0;
            maiorContaminadoParcialmente = 0;
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
            for (Integer i : vertices) {
                //Se vertice já foi adicionado, ignorar
                if (aux[i] >= K) {
                    continue;
                }
                int profundidadeS = bdls.getDistanceSafe(graph, i);
                if (profundidadeS == -1 && (sizeHs > 0 && !esgotado)) {
                    continue;
                }

                int grauContaminacao = 0;
                int contaminadoParcialmente = 0;
                double ddouble = 0;

                mustBeIncluded.clear();
                mapCount.clear();
                mustBeIncluded.add(i);
                mapCount.setVal(i, K);
                while (!mustBeIncluded.isEmpty()) {
                    Integer verti = mustBeIncluded.remove();
                    Collection<Integer> neighbors = graph.getNeighborsUnprotected(verti);
                    for (Integer vertn : neighbors) {
                        if (vertn.equals(verti)
                                || vertn.equals(i)
                                || (aux[vertn] + mapCount.getCount(vertn)) >= K) {
                            continue;
                        }
                        Integer inc = mapCount.inc(vertn);
                        if ((inc + aux[vertn]) == K) {
                            mustBeIncluded.add(vertn);
                        }
                    }
                    grauContaminacao++;
                }

                for (Integer x : mapCount.keySet()) {
                    if (mapCount.getCount(x) + aux[x] < K) {
                        contaminadoParcialmente++;
                    }
                }

                int deltaHsi = grauContaminacao;

                if (checkDeltaHsi) {
                    int[] auxb = aux.clone();
                    int deltaHsib = addVertToS(i, null, graph, auxb);
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
                int di = graph.degree(i);
                int deltadei = di - aux[i];

                ddouble = contaminadoParcialmente / graph.degree(i);
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
                    bestVertice = i;
                    maiorProfundidadeS = profundidadeS;
//                    maiorProfundidade = bdl.getDistance(graph, i);
                    maiorDouble = ddouble;
                    maiorAux = aux[i];
                    maiorGrau = di;
//                    maiorProfundidadeHS = profundidadeHS;
                    if (etapaVerbose == s.size()) {
                        System.out.printf(" * %3d: %3d %3d %3d %3d %3d %3d \n",
                                i, deltaHsi, grauContaminacao,
                                contaminadoParcialmente, profundidadeS, aux[i], di);
//                        System.out.print("  * ");
                        System.out.print("  * ");
//System.out.print("  * ");
                        for (Integer x : mapCount.keySet()) {
                            if (x.equals(i)) {
                                continue;
                            }
                            if (aux[x] < K && mapCount.getCount(x) + aux[x] >= K) {
                                System.out.print(" +" + x);
                            }
                        }
                        for (Integer x : mapCount.keySet()) {
                            if (x.equals(i)) {
                                continue;
                            }
                            if (aux[x] == 0 && mapCount.getCount(x) + aux[x] < K) {
                                System.out.print(" +/-" + x);
                            }
                        }
//                            System.out.println();
                        System.out.println();
                    }
                } else {
                    //Canonico
//                    Boolean greater = isGreater(
//                            deltaHsi, maiorDeltaHs,
//                            grauContaminacao, maiorGrauContaminacao,
//                            profundidadeS, maiorProfundidadeS,
//                            //                            profundidadeHS, maiorProfundidadeHS,
//                            contaminadoParcialmente, maiorContaminadoParcialmente,
//                            //                            (int) Math.round(ddouble * 10), (int) Math.round(maiorDouble * 10),
//                            -aux[i], -maiorAux
//                    );

                    Boolean greater = isGreater(
                            deltaHsi, maiorDeltaHs,
                            trans(aux[i]), trans(maiorAux),
                            profundidadeS, maiorProfundidadeS,
                            //                            profundidadeHS, maiorProfundidadeHS,
                            contaminadoParcialmente, maiorContaminadoParcialmente
                    //                            (int) Math.round(ddouble * 10), (int) Math.round(maiorDouble * 10),
                    );
                    if (greater == null) {
                        melhores.add(i);
                        if (etapaVerbose == s.size()) {
                            System.out.printf(" * %3d: %3d %3d %3d %3d %3d %3d \n",
                                    i, deltaHsi, grauContaminacao,
                                    contaminadoParcialmente, profundidadeS, aux[i], di);

                            System.out.print("  * ");
//System.out.print("  * ");
                            for (Integer x : mapCount.keySet()) {
                                if (x.equals(i)) {
                                    continue;
                                }
                                if (aux[x] < K && mapCount.getCount(x) + aux[x] >= K) {
                                    System.out.print(" +" + x);
                                }
                            }
                            for (Integer x : mapCount.keySet()) {
                                if (x.equals(i)) {
                                    continue;
                                }
                                if (aux[x] == 0 && mapCount.getCount(x) + aux[x] < K) {
                                    System.out.print(" +/-" + x);
                                }
                            }
//                            System.out.println();
                            System.out.println();
                        }
                    } else if (greater) {
                        melhores.clear();
                        melhores.add(i);
                        maiorDeltaHs = deltaHsi;
                        maiorGrauContaminacao = grauContaminacao;
                        maiorContaminadoParcialmente = contaminadoParcialmente;
                        bestVertice = i;
                        maiorProfundidadeS = profundidadeS;
                        maiorGrau = di;
                        maiorDouble = ddouble;
                        maiorAux = aux[i];
//                        maiorProfundidadeHS = profundidadeHS;
//                        System.out.printf("- vert: del conta pconta prof aux grau");
                        if (etapaVerbose == s.size()) {
                            System.out.printf(" * %3d: %3d %3d %3d %3d %3d %3d \n",
                                    i, deltaHsi, grauContaminacao,
                                    contaminadoParcialmente, profundidadeS, aux[i], di);

                            System.out.print("  * ");
                            for (Integer x : mapCount.keySet()) {
                                if (x.equals(i)) {
                                    continue;
                                }
                                if (aux[x] < K && mapCount.getCount(x) + aux[x] >= K) {
                                    System.out.print(" +" + x);
                                }
                            }
                            for (Integer x : mapCount.keySet()) {
                                if (x.equals(i)) {
                                    continue;
                                }
                                if (aux[x] == 0 && mapCount.getCount(x) + aux[x] < K) {
                                    System.out.print(" +/-" + x);
                                }
                            }
                            System.out.println();
                        }
                    }
                }
            }
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
            if (checkaddirmao) {
                Collection<Integer> neighborsUnprotected = graph.getNeighborsUnprotected(bestVertice);
                for (Integer nv : neighborsUnprotected) {
                    if (s.contains(nv)) {
                        if (verbose) {
                            System.out.println("add v to s: " + bestVertice + " nv from: " + nv + " posicao: " + s.size());
                        }
                        Integer inc = mapCountS.inc(nv);
                        if (inc >= K) {
                            if (verbose) {
                                System.out.println("" + nv + " maior que k: será removido de s");
                            }
                            s.remove(nv);
//                            bdls.labelDistances(graph, s);
                        } else if (inc == K - 1) {
                            if (verbose) {
                                System.out.println("  - " + nv + " possivel vertice de corte, reavaliar um de seus vizinhos");
                            }
//                            s.remove(nv);
//                            aux[nv] -= K;
//                            sizeHs--;
                            boolean comecou = false;
//                            System.out.print("  - remover: ");
                            Set<Integer> remover = new LinkedHashSet<>();
                            for (Integer vs : s) {
                                if (vs.equals(nv)) {
                                    comecou = true;
                                }
                                if (comecou) {
//                                    System.out.print(vs + ", ");
                                    remover.add(vs);
                                }
                            }
                            if (verbose) {
                                System.out.println();
                            }
                            if (rollbackEnable && (s.size() - remover.size()) > commit) {
                                if (verbose) {
                                    System.out.println("  --Será removido: " + remover);
                                }
                                s.removeAll(remover);
                                aux = auxini.clone();
                                commit = s.size();
                                sizeHs = sizeHsini;
                                mapCountS.clear();

                                for (Integer vi : s) {
                                    sizeHs += addVertToS(vi, null, graph, aux);
                                    Collection<Integer> ns = graph.getNeighborsUnprotected(vi);
                                    for (Integer vnn : ns) {
                                        if (s.contains(vnn)) {
                                            mapCountS.inc(vnn);
                                        }
                                    }
                                }

                                bdls.labelDistances(graph, s);
                            } else {
                                if (rollbackEnable && verbose) {
                                    System.out.println("Não será removido, bloco comitado");
                                }
                            }
                        }
                    }
                }
            }
            esgotado = false;

            if (maiorDeltaHs == 1) {
                verticesInteresse.add(bestVertice);
                Integer distance = bdls.getDistance(graph, bestVertice);
                if (distance == null || distance < 0) {
                    continue;
                }
                System.out.println("Novo vertice de interesse: " + bestVertice + " posicao: " + (s.size() - 1) + " " + sizeHs + "/" + vertexCount);
                int contfront = 0;
                for (Integer vt : vertices) {
                    if (aux[vt] > 0 && aux[vt] < K) {
                        contfront++;
                    }
                }
                System.out.println("Tamanho fronteira: " + contfront + " degree: " + graph.degree(bestVertice));

            }
            sizeHs = sizeHs + addVertToS(bestVertice, s, graph, aux);
//            if (sizeHs < vertexCount) {
//                bdl.labelDistances(graph, s);
//            }
        }

        System.out.println("Vertices de interesse[" + verticesInteresse.size() + "]: ");

        s = tryMinimal(graph, s);
//        s = tryMinimal2(graph, s);
        return s;
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

    public Boolean isGreater(int... compare) {
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

    public int addVertToS(Integer verti, Set<Integer> s,
            UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux) {
        int countIncluded = 0;
        if (verti == null || aux[verti] >= K) {
            return countIncluded;
        }

        aux[verti] = aux[verti] + K;
        if (s != null) {
            s.add(verti);
        }
        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        mustBeIncluded.add(verti);
        while (!mustBeIncluded.isEmpty()) {
            verti = mustBeIncluded.remove();
            Collection<Integer> neighbors = graph.getNeighborsUnprotected(verti);
            for (Integer vertn : neighbors) {
                if (vertn.equals(verti)) {
                    continue;
                }
                if ((++aux[vertn]) == K) {
                    mustBeIncluded.add(vertn);
                }
            }
            countIncluded++;
        }

        return countIncluded;
    }

//    @Override
    public Set<Integer> buildOptimizedHullSet(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        List<Integer> vertices = new ArrayList<>((List<Integer>) graphRead.getVertices());
        Set<Integer> hullSet = null;
        Integer vl = null;
        Set<Integer> sini = new LinkedHashSet<>();

        int vertexCount = (Integer) graphRead.maxVertex() + 1;
        int[] auxini = new int[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            auxini[i] = 0;
        }
        int sizeHs = 0;
        for (Integer v : vertices) {
            if (graphRead.degree(v) <= K - 1) {
                sizeHs = sizeHs + addVertToS(v, sini, graphRead, auxini);
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
                    degreev = graphRead.degree(vi);

                } else {
                    int degreeVi = graphRead.degree(vi);
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
        if (!checkIfHullSet(graphRead, hullSet.toArray(new Integer[0]))) {
            throw new IllegalStateException("NOT HULL SET");
        }
        return hullSet;
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
                    aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
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
        if (verbose) {
            System.out.println("tentando reduzir: " + s.size());
            System.out.println("s: " + s);
        }
        int cont = 0;
        for (Integer v : tmp) {

//        LinkedList<Integer> tmp2 = new LinkedList<>(tmp);
//        Iterator<Integer> descendingIterator = tmp2.descendingIterator();
//        while (descendingIterator.hasNext()) {
//            Integer v = descendingIterator.next();
            cont++;
            if (graphRead.degree(v) < K) {
                continue;
            }
            Set<Integer> t = new LinkedHashSet<>(s);
            t.remove(v);
            if (checkIfHullSet(graphRead, t.toArray(new Integer[0]))) {
                s = t;
                if (verbose) {
                    System.out.println("Reduzido removido: " + v);
                    System.out.println("Na posição " + cont + "/" + (tmp.size() - 1));
                }
            }
        }
        return s;
    }

    public Set<Integer> tryMinimal2(UndirectedSparseGraphTO<Integer, Integer> graphRead,
            Set<Integer> tmp) {
        Set<Integer> s = tmp;
        List<Integer> ltmp = new ArrayList<>(tmp);
        if (verbose) {
            System.out.println("tentando reduzir: " + s.size());
            System.out.println("s: " + s);
        }
        Collection<Integer> vertices = graphRead.getVertices();
        int cont = -1;
        for_p:
        for (int h = 0; h < ltmp.size(); h++) {
            cont++;
            Integer x = ltmp.get(h);
            if (graphRead.degree(x) < K || !s.contains(x)) {
                continue;
            }
            for (int j = h + 1; j < ltmp.size(); j++) {
                Integer y = ltmp.get(j);
                if (graphRead.degree(y) < K || y.equals(x)
                        || !s.contains(y)) {
                    continue;
                }
                Set<Integer> t = new LinkedHashSet<>(s);
                t.remove(x);
                t.remove(y);

                int contadd = 0;

                int[] aux = new int[(Integer) graphRead.maxVertex() + 1];
                for (int i = 0; i < aux.length; i++) {
                    aux[i] = 0;
                }

                Queue<Integer> mustBeIncluded = new ArrayDeque<>();
                for (Integer iv : t) {
                    Integer v = iv;
                    mustBeIncluded.add(v);
                    aux[v] = K;
                }
                while (!mustBeIncluded.isEmpty()) {
                    Integer verti = mustBeIncluded.remove();
                    contadd++;
                    Collection<Integer> neighbors = graphRead.getNeighborsUnprotected(verti);
                    for (Integer vertn : neighbors) {
                        if (vertn.equals(verti)) {
                            continue;
                        }
                        if (!vertn.equals(verti) && aux[vertn] <= K - 1) {
                            aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                            if (aux[vertn] == K) {
                                mustBeIncluded.add(vertn);
                            }
                        }
                    }
                    aux[verti] += K;
                }

                for (Integer z : vertices) {
                    if (aux[z] >= K
                            || z.equals(x)
                            || z.equals(y)) {
                        continue;
                    }
                    int contz = contadd;
                    int[] auxb = (int[]) aux.clone();
                    mustBeIncluded.add(z);
                    auxb[z] = K;
                    while (!mustBeIncluded.isEmpty()) {
                        Integer verti = mustBeIncluded.remove();
                        contz++;
                        Collection<Integer> neighbors = graphRead.getNeighborsUnprotected(verti);
                        for (Integer vertn : neighbors) {
                            if (vertn.equals(verti)) {
                                continue;
                            }
                            if (!vertn.equals(verti) && auxb[vertn] <= K - 1) {
                                auxb[vertn] = auxb[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                                if (auxb[vertn] == K) {
                                    mustBeIncluded.add(vertn);
                                }
                            }
                        }
                        auxb[verti] += K;
                    }

                    if (contz == vertices.size()) {
                        if (verbose) {
                            System.out.println("Reduzido removido: " + x + " " + y + " adicionado " + z);
                            System.out.println("Na posição " + cont + "/" + (tmp.size() - 1));
                        }
                        t.add(z);
                        s = t;
                        ltmp = new ArrayList<>(s);
//                        h--;
                        h = 0;
                        continue for_p;
                    }
                }

            }

        }
        return s;
    }

    public Set<Integer> tryMinimal2KeepSize(UndirectedSparseGraphTO<Integer, Integer> graphRead,
            Set<Integer> tmp, int sizeKeep) {
        Set<Integer> s = tmp;
        List<Integer> ltmp = new ArrayList<>(tmp);
        if (verbose) {
            System.out.println("tentando reduzir-keep size: " + s.size());
//            System.out.println("s: " + s);
        }
        Collection<Integer> vertices = graphRead.getVertices();
        int cont = -1;
        for_p:
        for (int h = 0; h < ltmp.size(); h++) {
            cont++;
            Integer x = ltmp.get(h);
            if (graphRead.degree(x) < K || !s.contains(x)) {
                continue;
            }
            for (int j = h + 1; j < ltmp.size(); j++) {
                Integer y = ltmp.get(j);
                if (graphRead.degree(y) < K || y.equals(x)
                        || !s.contains(y)) {
                    continue;
                }
                Set<Integer> t = new LinkedHashSet<>(s);
                t.remove(x);
                t.remove(y);

                int contadd = 0;

                int[] aux = new int[(Integer) graphRead.maxVertex() + 1];
                for (int i = 0; i < aux.length; i++) {
                    aux[i] = 0;
                }

                Queue<Integer> mustBeIncluded = new ArrayDeque<>();
                for (Integer iv : t) {
                    Integer v = iv;
                    mustBeIncluded.add(v);
                    aux[v] = K;
                }
                while (!mustBeIncluded.isEmpty()) {
                    Integer verti = mustBeIncluded.remove();
                    contadd++;
                    Collection<Integer> neighbors = graphRead.getNeighborsUnprotected(verti);
                    for (Integer vertn : neighbors) {
                        if (vertn.equals(verti)) {
                            continue;
                        }
                        if (!vertn.equals(verti) && aux[vertn] <= K - 1) {
                            aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                            if (aux[vertn] == K) {
                                mustBeIncluded.add(vertn);
                            }
                        }
                    }
                    aux[verti] += K;
                }

                for (Integer z : vertices) {
                    if (aux[z] >= K
                            || z.equals(x)
                            || z.equals(y)) {
                        continue;
                    }
                    int contz = contadd;
                    int[] auxb = (int[]) aux.clone();
                    mustBeIncluded.add(z);
                    auxb[z] = K;
                    while (!mustBeIncluded.isEmpty()) {
                        Integer verti = mustBeIncluded.remove();
                        contz++;
                        Collection<Integer> neighbors = graphRead.getNeighborsUnprotected(verti);
                        for (Integer vertn : neighbors) {
                            if (vertn.equals(verti)) {
                                continue;
                            }
                            if (!vertn.equals(verti) && auxb[vertn] <= K - 1) {
                                auxb[vertn] = auxb[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                                if (auxb[vertn] == K) {
                                    mustBeIncluded.add(vertn);
                                }
                            }
                        }
                        auxb[verti] += K;
                    }

                    if (contz == sizeKeep) {
                        if (verbose) {
                            System.out.println("Reduzido removido: " + x + " " + y + " adicionado " + z);
                            System.out.println("Na posição " + cont + "/" + (tmp.size() - 1));
                        }
                        t.add(z);
                        s = t;
                        ltmp = new ArrayList<>(s);
//                        h--;
                        h = 0;
                        continue for_p;
                    }
                }

            }

        }
        return s;
    }

    public void printPesoAux(int[] auxb) {
        int peso = 0;
        for (int i = 0; i < auxb.length; i++) {
            peso = peso + auxb[i];
        }
        System.out.print("{" + peso + "}");
        UtilProccess.printArray(auxb);
    }

    public static void main(String... args) throws IOException {
        GraphHullNumberHeuristicV1 opref = new GraphHullNumberHeuristicV1();
        GraphTSSCordasco optss = new GraphTSSCordasco();
        GraphSubgraph opsubgraph = new GraphSubgraph();
        GraphSubgraphNeighborHood opsubgraphn = new GraphSubgraphNeighborHood();
        opref.setVerbose(false);
        GraphHullNumberHeuristicV5Tmp3Testes op = new GraphHullNumberHeuristicV5Tmp3Testes();

        System.out.println("Teste greater: ");

        UndirectedSparseGraphTO<Integer, Integer> graph = null;
//        graph = new UndirectedSparseGraphTO("681-753,681-1381,681-4658,753-1381,753-4658,1381-2630,1381-2819,1381-4220,1381-4658,2630-2819,2630-3088,2630-4220,2819-3088,2819-4220,");

//        graph = UtilGraph.loadGraphG6("S?????????????????w@oK?B??GW@OE?g");
//        graph = UtilGraph.loadGraphG6("S??A?___?O_aOOCGCO?OG@AAB_??Fvw??");
//        graph = UtilGraph.loadGraphG6("Ss_?G?@???coH`CEABGR?AWDe?A_oAR??");
        graph = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/ca-AstroPh/ca-AstroPh.txt"));
//        graph = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/ca-HepPh/ca-HepPh.txt"));
//        graph = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/ca-CondMat/ca-CondMat.txt"));
//        graph = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/ca-GrQc/ca-GrQc.txt"));
//        graph = UtilGraph.loadBigDataset(
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/BlogCatalog3/nodes.csv"),
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/BlogCatalog3/edges.csv"));
//        graph = UtilGraph.loadBigDataset(
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/BlogCatalog/nodes.csv"),
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/BlogCatalog/edges.csv"));
//        graph = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/ca-GrQc/ca-GrQc.txt"));
//        GraphStatistics statistics = new GraphStatistics();
//        System.out.println(graph.getName() + ": " + statistics.doOperation(graph));
//        System.out.println(graph.getName());
//        graph = UtilGraph.loadGraphG6("U?GoA?ACCA?_E???O?@???@c@`_?Q_C`DGs?o_Q?");
//        graph = UtilGraph.loadGraph(new File("tmp.es"));
        System.out.println(graph.toResumedString());
//        op.K = 3;
        op.verbose = true;
////        System.out.println("Subgraph: ");
//        UndirectedSparseGraphTO subGraph = opsubgraph.subGraphInduced(graph, Set.of(388, 1129, 1185, 1654, 3584, 3997));
//        System.out.println(subGraph.getEdgeString());
//        System.out.println("Subgraph: ");
//        UndirectedSparseGraphTO subGraph = opsubgraphn.subGraphInduced(graph, Set.of(1381, 3088, 2630));
//        System.out.println(subGraph.getEdgeString());
        op.K = opref.K = 6;
        op.startVertice = false;

        if (false) {
            Map<Integer, Integer> numConnectedComponents = op.numConnectedComponents(graph);
            Map<Integer, Set<Integer>> connectedComponents = op.connectedComponents(graph);
            for (Entry<Integer, Set<Integer>> e : connectedComponents.entrySet()) {
                System.out.print("" + e.getKey() + ": \n -");
                for (Integer vn : e.getValue()) {
                    System.out.print(vn + ", ");
                }
                System.out.println();

                System.out.println("H: ");
                UndirectedSparseGraphTO subGraphInduced = opsubgraph.subGraphInduced(graph, e.getValue());
                Set h1 = op.buildOptimizedHullSet(subGraphInduced);
                System.out.println(" - hnv[" + h1.size() + "]:" + h1);
//            if (e.getValue().size() >= 1000) {
//            if (e.getValue().contains(1381)) {
//                System.out.println("Dump: ");
//                System.out.println(subGraphInduced.getEdgeString());
//
//            }
//            Set<Integer> h2 = optss.tssCordasco(subGraphInduced);
//            System.out.println(" - tss[" + h2.size() + "]:" + h2);
                if (e.getValue().size() <= 100) {
                    Set h3 = opref.buildOptimizedHullSet(subGraphInduced);
                    System.out.println(" - ref[" + h3.size() + "]:" + h3);
                    if (h1.size() != h3.size()) {
                        System.out.println(" - DIVERGETNE: " + h1.size() + " " + h3.size());
                    }
                }
            }
        }
        if (true) {
//            return;
        }
//        System.out.println();
//        op.K = 2;
//        op.etapaVerbose = 1;
//        op.startVertice = false;
        UtilProccess.printStartTime();
        Set<Integer> buildOptimizedHullSet = op.buildOptimizedHullSet(graph);

        UtilProccess.printStartTime();

        System.out.println(
                "S[" + buildOptimizedHullSet.size() + "]: " + buildOptimizedHullSet);

        Set<Integer> findMinHullSetGraph = opref.findMinHullSetGraph(graph);

        System.out.println(
                "REF-S[" + findMinHullSetGraph.size() + "]: " + findMinHullSetGraph);

        Integer[] optHuull = buildOptimizedHullSet.toArray(new Integer[0]);

        for (int i = 1;
                i < buildOptimizedHullSet.size();
                i++) {
            System.out.println("tentador constuir um conjunto " + i + " menor: " + findMinHullSetGraph.size());
            System.out.print("S: ");
            Integer[] arr = new Integer[i];
            for (int j = 0; j < i; j++) {
                arr[j] = optHuull[j];
                System.out.print(arr[j] + ", ");
            }
            System.out.println();
            Set<Integer> findHullSubSetBruteForce = op.findHullSubSetBruteForce(graph, findMinHullSetGraph.size(), arr);
            if (findHullSubSetBruteForce == null) {
                System.out.println(" Falhou ");
                break;
            }

            System.out.println("Encontrado-[" + findHullSubSetBruteForce.size() + "]: " + findHullSubSetBruteForce);
        }

        if (true) {
            return;
        }

//        Set<Integer> findHullSubSetBruteForce = op.findHullSubSetBruteForce(graph, findMinHullSetGraph.size(), 19, 14);
//        System.out.println("Try search[" + findHullSubSetBruteForce.size() + "]: " + findHullSubSetBruteForce);
        GraphGeneratorRandomGilbert generator = new GraphGeneratorRandomGilbert();

        op.setVerbose(
                true);

        System.out.print(
                "Dens:\t");
        for (double density = 0.1;
                density <= 0.9; density
                += 0.1) {
            System.out.printf("%.1f \t", density);

        }

        for (int nv = INI_V;
                nv <= MAX_V;
                nv++) {

            System.out.printf("%3d: \t", nv);

            for (double density = 0.1; density <= 0.9; density += 0.1) {
                graph = generator.generate(nv, density);
                findMinHullSetGraph = op.findMinHullSetGraph(graph);
                System.out.printf("%d", findMinHullSetGraph.size());

                System.out.printf("\t");

            }
            System.out.println();
        }
    }
}
