package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import static com.github.braully.graph.operation.GraphHNVOptm.allParameters;
import static com.github.braully.graph.operation.GraphHNVOptm.paux;
import static com.github.braully.graph.operation.GraphHNVOptm.pbonusParcial;
import static com.github.braully.graph.operation.GraphHNVOptm.pbonusParcialNormalizado;
import static com.github.braully.graph.operation.GraphHNVOptm.pbonusTotal;
import static com.github.braully.graph.operation.GraphHNVOptm.pbonusTotalNormalizado;
import static com.github.braully.graph.operation.GraphHNVOptm.pdeltaHsi;
import static com.github.braully.graph.operation.GraphHNVOptm.pdificuldadeParcial;
import static com.github.braully.graph.operation.GraphHNVOptm.pdificuldadeTotal;
import static com.github.braully.graph.operation.GraphHNVOptm.pprofundidadeS;
import static com.github.braully.graph.operation.GraphHullNumber.PARAM_NAME_HULL_NUMBER;
import static com.github.braully.graph.operation.GraphHullNumber.PARAM_NAME_HULL_SET;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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

public class GraphHNVOptmPoda
        extends AbstractHeuristic implements IGraphOperation {

    public boolean startVertice = true;
    public boolean marjorado = false;

    public boolean checkbfs = false;
    public boolean checkstartv = false;
    public boolean checkDeltaHsi = false;

    private static final Logger log = Logger.getLogger(GraphHNVOptmPoda.class);

    static final String description = "HHnV2";
    int etapaVerbose = -1;
    boolean checkaddirmao = true;
    boolean rollbackEnable = false;
    //

    public boolean realizarPoda = true;

    {

        setPularAvaliacaoOffset(true);
        setTryMinimal();
//        setParameter(pdeltaHsi, true);
        setParameter(pdificuldadeTotal, true);
        setParameter(pbonusParcialNormalizado, true);
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
        if (realizarPoda) {
            sb.append(":poda");
        }
        if (pularAvaliacaoOffset) {
            sb.append(":pulaOff");
        }
        return sb.toString();
    }

    public GraphHNVOptmPoda() {
    }

    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Integer hullNumber = -1;
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
    double maiorIndiceCentralidade = 0;
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
        if (krp[verti] > 0 && aux[verti] >= krp[verti]) {
            return countIncluded;
        }

        aux[verti] = aux[verti] + krp[verti];
        mustBeIncluded.clear();
        mustBeIncluded.add(verti);
        while (!mustBeIncluded.isEmpty()) {
            verti = mustBeIncluded.remove();
            Collection<Integer> neighbors = getNeighborsNaoPodados(graph, verti);
            for (Integer vertn : neighbors) {
                if ((++aux[vertn]) == krp[vertn]) {
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
        if (krp[verti] > 0 && aux[verti] >= krp[verti]) {
            return countIncluded;
        }

        aux[verti] = aux[verti] + krp[verti];
        if (s != null) {
            s.add(verti);
            Collection<Integer> neighbors = getNeighborsNaoPodados(graph, verti);
            for (Integer vertn : neighbors) {
                if ((++scount[vertn]) == krp[vertn] && s.contains(vertn)) {
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
            Collection<Integer> neighbors = getNeighborsNaoPodados(graph, verti);
            for (Integer vertn : neighbors) {
                if ((++aux[vertn]) == krp[vertn]) {
                    mustBeIncluded.add(vertn);
                }
            }
            countIncluded++;
        }

        return countIncluded;
    }
    boolean pularAvaliacaoOffset = false;
    int[] pularAvaliacao = null;
    int[] scount = null;
    int[] degree = null;
    Set<Integer> verticesTrabalho;
    Set<Integer> podados;
    int[] graup;
    int[] krp;
    BFSUtil bdlhs;
    BFSUtil bdls;

    Set<Integer> graphCenter;

//    @Override
    public Set<Integer> poda(UndirectedSparseGraphTO graph) {
        podados = new LinkedHashSet<>();
        Set<Integer> verticesTrabalhados = new LinkedHashSet<>(verticesTrabalho);
        //        initKr(graph);
        int n = (Integer) graph.maxVertex() + 1;

        int[] dist = new int[n];

        Set<Integer>[] N = new Set[n];

        for (Integer v : verticesTrabalhados) {
            N[v] = new LinkedHashSet<>(getNeighborsNaoPodados(graph, v));
            dist[v] = graup[v] - krp[v];
        }

        boolean flag = true;

        while (!verticesTrabalhados.isEmpty() && flag) {
            Integer v = null;
            Double maiorRanking = null;
            for (var vi : verticesTrabalhados) {
//                double rankvi = (graup[vi] - krp[vi]) * (bdlhs.getDistance(graph, vi) + 1);
                Integer distance = bdlhs.getDistance(graph, vi);
                int entropia = graup[vi] - krp[vi];
//                double rankvi = (distance + 1) / Math.max(1, entropia);
                double rankvi = distance;
                if (entropia == 0 && degree[vi] == graup[vi]) {
                    if (v == null) {
                        v = vi;
                        maiorRanking = rankvi;
                    } else if (rankvi > maiorRanking) {
                        v = vi;
                        maiorRanking = rankvi;
                    }
                }
            }
            if (v == null) {
                flag = false;
            } else {
                if (verbose) {
                    System.out.println("Removendo vertice de baixa qualidade: " + v);
                }
                for (Integer u : N[v]) {
                    graup[u]--;
                }
                verticesTrabalho.remove(v);
                podados.add(v);
            }
            verticesTrabalhados.remove(v);
        }
//        S = tryMinimal(graph, S);
        return verticesTrabalho;
    }

    public Set<Integer> buildOptimizedHullSet(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        List<Integer> verticesOriginais = (List<Integer>) graphRead.getVertices();
        verticesTrabalho = new LinkedHashSet<>(verticesOriginais);
        initKr(graphRead);
        findCenterOfGraph(graphRead);

        Set<Integer> hullSet = null;
        Integer vl = null;
        Set<Integer> sini = new LinkedHashSet<>();

        int vertexCount = (Integer) graphRead.maxVertex() + 1;
        int[] auxini = new int[vertexCount];
        scount = new int[vertexCount];
        degree = new int[vertexCount];
        pularAvaliacao = new int[vertexCount];
        graup = new int[vertexCount];
        krp = new int[vertexCount];
        graphCenter = new HashSet<>();
        int menorGrau = 0;
        int maiorGrau = 0;

        for (Integer i : verticesOriginais) {
            auxini[i] = 0;
            pularAvaliacao[i] = -1;
            scount[i] = 0;
            degree[i] = graphRead.degree(i);
            graup[i] = degree[i];
            krp[i] = kr[i];
        }
//        bdlhs = BFSUtil.newBfsUtilSimple(vertexCount + 1);
        int sizeHs = 0;
        for (Integer v : verticesTrabalho) {
            if (degree[v] < degree[menorGrau]) {
                menorGrau = v;
            }
            if (degree[v] > degree[maiorGrau]) {
                maiorGrau = v;
            }
            if (graup[v] <= krp[v] - 1) {
                sizeHs = sizeHs + addVertToS(v, sini, graphRead, auxini);
            }
            if (krp[v] == 0) {
                sizeHs = sizeHs + addVertToAux(v, graphRead, auxini);
            }
        }

//        bdlhs.
        for (int i = 0; i < vertexCount; i++) {
            auxini[i] = 0;
            pularAvaliacao[i] = -1;
            scount[i] = 0;
        }
        if (realizarPoda) {
            if (verbose) {
                System.out.println("Realizando poda: " + verticesTrabalho.size());
            }
            Set<Integer> tipDecomp = poda(graphRead);
            if (verbose) {
                System.out.println("Poda realizada tfinal: " + verticesTrabalho.size());
            }
            for (Integer v : verticesTrabalho) {
                if (degree[v] < degree[menorGrau]) {
                    menorGrau = v;
                }
                if (degree[v] > degree[maiorGrau]) {
                    maiorGrau = v;
                }
                if (graup[v] <= krp[v] - 1) {
                    sizeHs = sizeHs + addVertToS(v, sini, graphRead, auxini);
                }
                if (krp[v] == 0) {
                    sizeHs = sizeHs + addVertToAux(v, graphRead, auxini);
                }
            }
        }

        Set<Integer> s = new LinkedHashSet<>(sini);
        Collection<Integer> vertices = verticesTrabalho;
        vertexCount = vertices.size();
        Integer maxVertex = graphRead.maxVertex();
        int[] aux = auxini.clone();
        if (verbose) {
            System.out.println("Sini-Size: " + sini.size());
            System.out.println("Sini: " + sini);
        }
//        BFSDistanceLabeler<Integer, Integer> bdls = new BFSDistanceLabeler<>();
        bdls = BFSUtil.newBfsUtilSimple(maxVertex + 1);

        bdls.labelDistances(graphRead, s);

        bestVertice = -1;
        mapCount = new MapCountOpt(maxVertex + 1);

        while (sizeHs < vertexCount) {
            if (bestVertice != -1) {
                bdls.incBfs(graphRead, bestVertice);
            }

            bestVertice = -1;
            maiorIndiceCentralidade = 0;
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
            escolherMelhorVerticeAlvo(graphRead, aux, vertices, bdls, sizeHs);
            if (etapaVerbose == s.size()) {
                System.out.println(" - " + bestVertice);
                System.out.println(" - " + melhores);
                for (Integer ml : melhores) {
                    System.out.println("  -- " + ml + " [" + getNeighborsNaoPodados(graphRead, ml).size() + "]: " + getNeighborsNaoPodados(graphRead, ml));
                }

            }
            if (bestVertice == -1) {
                esgotado = true;
                continue;
            }
            if (maiorDeltaHs == 1 && esgotado) {

            }
            esgotado = false;
            sizeHs = sizeHs + addVertToS(bestVertice, s, graphRead, aux);
//            if (sizeHs < vertexCount) {
//                bdl.labelDistances(graph, s);
//            }
        }
//        System.out.println("Vertices de interesse[" + verticesInteresse.size() + "]: ");
        if (tryMiminal()) {
            s = tryMinimal(graphRead, s);
        }
//        s = tryMinimal2(graph, s);
        if (!checkIfHullSet(graphRead, s)) {
            System.err.println("" + s.size() + ": " + s);
            System.err.println("NOT HULL SET");
//            throw new IllegalStateException("NOT HULL SET");
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

    void escolherMelhorVerticeAlvo(UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux, Collection<Integer> vertices, BFSUtil bdls, int sizeHs) {
        for (Integer i : vertices) {
            //Se vertice já foi adicionado, ignorar
            if (aux[i] >= krp[i]) {
                continue;
            }
            int profundidadeS = bdls.getDistanceSafe(graph, i);
            if (profundidadeS == -1 && (sizeHs > 0 && !esgotado)) {
                continue;
            }
            if (pularAvaliacaoOffset && pularAvaliacao[i] >= sizeHs) {
                continue;
            }
//            profundidadeS = bdlhs.getDistanceSafe(graph, i);
            int grauContaminacao = 0;
            int contaminadoParcialmente = 0;
            double bonusParcialNormalizado = 0;
            double bonusTotalNormalizado = 0;
            double bonusParcial = 0;
            double dificuldadeParcial = 0;
            double ddouble = 0;
            double bonusTotal = 0;
            double dificuldadeTotal = 0;
            double indiceCentralidade = 0;
            int grauI = graup[i];
            double profundidateTotal = 0;
            double profundidateParcial = 0;

            double bonusHs = 0;
            double dificuldadeHs = 0;
            mustBeIncluded.clear();
            mapCount.clear();
            mustBeIncluded.add(i);
            mapCount.setVal(i, krp[i]);
//                System.out.println(s.size() + "-avaliando: " + i);
            while (!mustBeIncluded.isEmpty()) {
                Integer verti = mustBeIncluded.remove();
                Collection<Integer> neighbors = getNeighborsNaoPodados(graph, verti);
                for (Integer vertn : neighbors) {
                    if (vertn.equals(verti)
                            || vertn.equals(i)
                            || (aux[vertn] + mapCount.getCount(vertn)) >= krp[vertn]) {
                        continue;
                    }
                    Integer inc = mapCount.inc(vertn);
                    if ((inc + aux[vertn]) == krp[vertn]) {
                        mustBeIncluded.add(vertn);
//                        bonusHs += graup[vertn] - krp[vertn];
//                        dificuldadeHs += (krp[vertn] - aux[vertn]);
                        pularAvaliacao[vertn] = sizeHs;
                    }
                }
                double bonus = graup[verti] - krp[verti];
                double dificuldade = (krp[verti] - aux[verti]);

                bonusHs += bonus;
                dificuldadeHs += (krp[verti] - aux[verti]);
                bonusTotalNormalizado += (dificuldade / bonus);
//                pularAvaliacao[verti] = sizeHs;
                grauContaminacao++;
                profundidateTotal = bdlhs.getDistanceSafe(graph, verti);
            }

            profundidadeS = (int) profundidateTotal;

            for (Integer x : mapCount.keySet()) {
                if (mapCount.getCount(x) + aux[x] < krp[x]) {
                    int dx = graup[x];
//                        double bonus = Math.max(1, dx - krp[x]);
//                        double bonus = krp[x] - dx;
                    double bonus = dx - krp[x];
                    bonusParcial += bonus;
                    double dificuldade = (krp[x] - (aux[x]));
                    dificuldadeParcial += dificuldade;
                    contaminadoParcialmente++;
                    bonusParcialNormalizado += (dificuldade / bonus);
                    profundidateParcial = bdlhs.getDistanceSafe(graph, x);
                }
            }

//                dificuldadeTotal = krp[i] - aux[i];
//                bonusTotal = grauI - krp[i];
            bonusTotal = bonusHs;
            dificuldadeTotal = dificuldadeHs;
//            bonusTotalNormalizado = bonusTotal / dificuldadeTotal;
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
            int di = graup[i];
            int deltadei = di - aux[i];

//            ddouble = contaminadoParcialmente / graup[i];
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
                maiorIndiceCentralidade = indiceCentralidade;
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
                            case paux:
                                p1 = aux[i];
                                p2 = maiorAux;
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
                    maiorIndiceCentralidade = indiceCentralidade;
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

        UndirectedSparseGraphTO<Integer, Integer> graph = null;
        GraphHNVOptmPoda op = new GraphHNVOptmPoda();

//        op.setParameter(GraphBigHNVOptm.paux, true);
//        op.setParameter(GraphBigHNVOptm.pgrau, true);
//        op.setParameter(GraphBigHNVOptm.pbonusTotal, true);
//        graph = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/ca-GrQc/ca-GrQc.txt"));
//        graph = UtilGraph.loadBigDataset(
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/Douban/nodes.csv"),
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/Douban/edges.csv"));
////        graph = UtilGraph.loadBigDataset(
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/Delicious/nodes.csv"),
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/Delicious/edges.csv"));
//
//        graph = UtilGraph.loadBigDataset(
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/BlogCatalog3/nodes.csv"),
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/BlogCatalog3/edges.csv"));
//        graph = UtilGraph.loadBigDataset(
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/BlogCatalog/nodes.csv"),
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/BlogCatalog/edges.csv"));
        graph = UtilGraph.loadGraphG6("M??????_A?U?BoYG?");

        System.out.println(graph.toResumedString());
        System.out.println();
        op.setVerbose(true);
        op.setR(10);
        Set<Integer> buildOptimizedHullSet = op.buildOptimizedHullSet(graph);
        System.out.println(
                "S[" + buildOptimizedHullSet.size() + "]: ");
        if (true) {
            return;
        }

//        System.out.println("Conected componentes: ");
//        Map<Integer, Set<Integer>> connectedComponents = op.connectedComponents(graph);
//        for (Entry<Integer, Set<Integer>> e : connectedComponents.entrySet()) {
//            System.out.println("" + e.getKey() + ": " + e.getValue().size());
//        }
//        }
//        op.setR(10);
//        op.resetParameters();
//        op.setPularAvaliacaoOffset(true);
////        op.decompor = true;
////        op.setParameter(GraphBigHNVOptm.paux, true);
////        op.setParameter(GraphBigHNVOptm.pgrau, true);
////        op.realizarPoda = true;
//        op.realizarPoda = false;
////        op.setParameter(GraphBigHNVOptm.pdeltaHsi, true);
//        op.setParameter(GraphBigHNVOptm.pbonusTotal, true);
////        op.setParameter(GraphBigHNVOptm.pbonusTotalNormalizado, true);
////        op.setParameter(GraphBigHNVOptm.pbonusParcialNormalizado, true);
//        op.setParameter(GraphBigHNVOptm.pdificuldadeTotal, true);
////        op.setParameter(GraphBigHNVOptm.paux, false);
////        op.setParameter(GraphBigHNVOptm.pprofundidadeS, false);
//        op.setTryMinimal();
//        UtilProccess.printStartTime();
//        Set<Integer> buildOptimizedHullSet = op.buildOptimizedHullSet(graph);
//
//        UtilProccess.printEndTime();
//        System.out.println(op.getName());
//
//        System.out.println(
//                "S[" + buildOptimizedHullSet.size() + "]: ");// + buildOptimizedHullSet);
//        op.checkIfHullSet(graph, buildOptimizedHullSet);
//        boolean checkIfHullSet = op.checkIfHullSet(graph, buildOptimizedHullSet);
//        if (!checkIfHullSet) {
//            System.err.println("FAIL: fail on check hull setg");
//        }
        List<String> parametros = new ArrayList<>();
        parametros.addAll(List.of(
                pdeltaHsi, pdificuldadeTotal,
                pbonusTotal, pbonusParcial, pdificuldadeParcial,
                pbonusTotalNormalizado, pbonusParcialNormalizado,
                pprofundidadeS
        //        , pgrau, paux
        ));
        System.out.println("otimização individualizada");
        for (int ciclo = 1; ciclo <= 3; ciclo++) {
            System.out.println("Ciclo: " + ciclo);
//            op.setPularAvaliacaoOffset(false);
            op.setPularAvaliacaoOffset(true);
            op.realizarPoda = false;
            for (int r = 6; r <= 10; r++) {
                String line = null;
                int cont = 0;
                MapCountOpt contMelhor = new MapCountOpt(allParameters.size() * 100);
                op.setR(r);
                List<int[]> melhores = new ArrayList<>();
//                for (int ip = 0; ip < allParameters.size(); ip++) {

                int melhor = otimizarParametros(op, parametros, ciclo, graph, cont, r, melhores);
//                for (Integer i : melhores) {
                for (int[] ip : melhores) {
                    int i = array2idx(ip);
                    contMelhor.inc(i);
                }
                cont++;

                System.out.println("\n---------------");
                System.out.println("Resumo r:" + r + " melhor: " + melhor);

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
//            for (int ip = 0; ip < allParameters.size(); ip++) {
//                String p = allParameters.get(ip);
//                System.out.println(p + ": " + contMelhor.getCount(ip));
//            }
            }
        }
    }

    protected static int otimizarParametros(GraphHNVOptmPoda op, List<String> parametros, int k, UndirectedSparseGraphTO<Integer, Integer> graph, int cont, int r, List<int[]> melhores1) {
        Integer melhor = null;
        Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(parametros.size(), k);
        while (combinationsIterator.hasNext()) {
            int[] currentSet = combinationsIterator.next();
            for (String p1 : new String[]{pdeltaHsi, pdificuldadeTotal}) {
                op.resetParameters();
                op.setParameter(p1, true);
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
                System.out.println("resultado parcial: " + out);

                if (melhor == null) {
                    melhor = res;
                    melhores1.add(currentSet);
                } else if (melhor == res) {
                    melhores1.add(currentSet);
                } else if (melhor > res) {
                    melhor = res;
                    melhores1.clear();
                    melhores1.add(currentSet);
                    System.out.println(" melhor otimizado ");
                    System.out.println(out);
                }
                int[] currentRerverse = currentSet.clone();
                for (int i = 0; i < currentSet.length; i++) {
                    currentRerverse[i] = currentSet[(currentSet.length - 1) - i];
                }
                op.resetParameters();
                op.setParameter(p1, true);
                for (int ip : currentRerverse) {
                    String p = parametros.get(ip);
                    if (k == 1) {
                        op.setParameter(p, false);
                    } else {
                        op.setParameter(p, true);
                    }
                }
                optmHullSet = op.buildOptimizedHullSet(graph);
                name = op.getName();
                res = optmHullSet.size();
                out = "R\t g" + cont++ + "\t r"
                        + r + "\t" + name
                        + "\t" + res + "\n";
                System.out.println("resultado parcial: " + out);

                if (melhor == null) {
                    melhor = res;
                    melhores1.add(currentRerverse);
                } else if (melhor == res) {
                    melhores1.add(currentRerverse);
                } else if (melhor > res) {
                    melhor = res;
                    melhores1.clear();
                    melhores1.add(currentRerverse);
                    System.out.println(" melhor otimizado ");
                    System.out.println(out);
                }
            }
        }
        return melhor;
    }

    private static int apply(GraphHNVOptmPoda op, int[] currentSet, UndirectedSparseGraphTO<Integer, Integer> graph, int cont, int r, Integer melhor, List<int[]> melhores1) {
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

    void resetParameters() {
        this.parameters.clear();
    }

    void setParameter(String p, boolean b) {
        this.parameters.put(p, b);
    }

    private void setPularAvaliacaoOffset(boolean b) {
        this.pularAvaliacaoOffset = b;
    }

    private Collection<Integer> getNeighborsNaoPodados(UndirectedSparseGraphTO<Integer, Integer> graph, Integer v) {
        Collection<Integer> nvs = new LinkedHashSet<>(graph.getNeighborsUnprotected(v));
        if (podados != null) {
            nvs.removeAll(podados);
        }
        return nvs;
    }

    private void findCenterOfGraph(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Collection<Integer> vertices = graph.getVertices();
        int vertexCount = (Integer) graph.maxVertex() + 1;
        bdlhs = BFSUtil.newBfsUtilSimple(vertexCount + 1);
        bdls = BFSUtil.newBfsUtilSimple(vertexCount + 1);
        findCenterOfGraph(vertices, graph);
    }

    protected void findCenterOfGraph(Collection<Integer> vertices, UndirectedSparseGraphTO<Integer, Integer> graph) {

        while (bdlhs.discored < vertices.size()) {
            Integer maior = null;
            Integer menor = null;
            for (Integer v : vertices) {
                if (bdlhs.getDistance(graph, v) == null) {
                    if (maior == null) {
                        maior = v;
                        menor = v;
                    } else {
                        int dv = graph.degree(v);
                        if (dv > graph.degree(maior)) {
                            maior = v;
                        }
                        if (dv < graph.degree(menor)) {
                            menor = v;
                        }
                    }
                }
            }
            bdlhs.incBfs(graph, maior);
//            bdls.incBfs(graph, menor);
//            Integer maiorDistanciaDoMenor = 0;
//            Integer vMaiorDistanciaDoMenor = null;
//            Integer maiorDistanciaDoMaior = 0;
//            Integer vMaiorDistanciaDoMaior = null;
//            for (Integer v : vertices) {
//                if (vMaiorDistanciaDoMenor == null) {
//                    vMaiorDistanciaDoMenor = v;
//                    maiorDistanciaDoMenor = bdls.getDistance(graph, v);
//                    vMaiorDistanciaDoMaior = v;
//                    maiorDistanciaDoMaior = bdlhs.getDistance(graph, v);
//                } else {
//                    Integer distV = bdls.getDistance(graph, v);
//                    if (distV > maiorDistanciaDoMenor) {
//                        maiorDistanciaDoMenor = distV;
//                        vMaiorDistanciaDoMenor = v;
//                    }
//                    Integer distV1 = bdlhs.getDistance(graph, v);
//                    if (distV1 > vMaiorDistanciaDoMaior) {
//                        maiorDistanciaDoMaior = distV1;
//                        vMaiorDistanciaDoMaior = v;
//                    }
//                }
//            }
//
//            System.out.println("Maior distancia de " + menor + ": " + maiorDistanciaDoMenor);
//            System.out.println("Maior distancia de " + maior + ": " + maiorDistanciaDoMaior);
        }
    }
}
