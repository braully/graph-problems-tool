package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import static com.github.braully.graph.operation.GraphHullNumber.PARAM_NAME_HULL_NUMBER;
import static com.github.braully.graph.operation.GraphHullNumber.PARAM_NAME_HULL_SET;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.log4j.Logger;
import util.BFSUtil;
import util.MapCountOpt;
import util.UtilProccess;

public class GraphHNVOptm
        extends AbstractHeuristicOptm implements IGraphOperation {

    private static final Logger log = Logger.getLogger(GraphHNVOptm.class);

    static final String description = "HHnV2Optm";
    int etapaVerbose = -1;

    //
    public boolean decompor = false;

    {
        setPularAvaliacaoOffset(true);
        setTryMinimal();
//        setParameter(pdeltaHsi, true);
        setParameter(pdificuldadeTotal, true);
        setParameter(pbonusParcialNormalizado, true);
    }

    public static String getDescription() {
        return description;
    }

    public GraphHNVOptm() {
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
            minHullSet = findMinHullSetGraph(graph);
            if (minHullSet != null && !minHullSet.isEmpty()) {
                hullNumber = minHullSet.size();
            }
        } catch (Exception ex) {
            log.error(null, ex);
        }

        /* Processar a buscar pelo hullset e hullnumber */
        Map<String, Object> response = new HashMap<>();
        response.put("R", this.R);
        response.put(PARAM_NAME_HULL_NUMBER, hullNumber);
        response.put(PARAM_NAME_HULL_SET, minHullSet);
        response.put(IGraphOperation.DEFAULT_PARAM_NAME_RESULT, hullNumber);
        return response;
    }

    public Set<Integer> findMinHullSetGraph(UndirectedSparseGraphTO<Integer, Integer> graph) {
        return buildOptimizedHullSet(graph);

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

    protected void escolherMelhorVertice(UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux, Collection<Integer> vertices, int sizeHs) {
        for (Integer i : vertices) {
            //Se vertice já foi adicionado, ignorar
            if (aux[i] >= kr[i]) {
                continue;
            }
            int profundidadeS = bdls.getDistanceSafe(graph, i);
            if (profundidadeS == -1 && (sizeHs > 0 && !esgotado)) {
                grafoconexo = false;
                continue;
            }
            if (pularAvaliacaoOffset && pularAvaliacao[i] >= sizeHs) {
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
            double bonusHs = 0;
            double dificuldadeHs = 0;

            mapCount.clear();
            mapCount.setVal(i, kr[i]);

            mustBeIncluded.clear();
            mustBeIncluded.add(i);

//                System.out.println(s.size() + "-avaliando: " + i);
            profundidadeS = 0;
            int di = 0;
//            int dei = degree[i];
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
                        pularAvaliacao[vertn] = sizeHs;
                    }
                }
                double bonus = degree[verti] - kr[verti];
                double dificuldade = (kr[verti] - aux[verti]);

                bonusHs += bonus;
                dificuldadeHs += dificuldade;
//                pularAvaliacao[verti] = sizeHs;
                profundidadeS += bdls.getDistanceSafe(graph, verti) + 1;
                grauContaminacao++;
                di += degree[verti];
//                if (dei < degree[verti]) {
//                    System.out.println("existem vertices de grau maior no bloco");
//                }
            }

            for (Integer x : mapCount.keySet()) {
                if (mapCount.getCount(x) + aux[x] < kr[x]) {
                    int dx = degree[x];
//                        double bonus = Math.max(1, dx - kr[x]);
//                        double bonus = kr[x] - dx;
                    double bonus = dx - kr[x];
                    bonusParcial += bonus;
//                    double dificuldade = (kr[x] - aux[x]);
                    double dificuldade = mapCount.getCount(x);
                    dificuldadeParcial += dificuldade;
                    contaminadoParcialmente++;
                }
            }

            bonusTotal = bonusHs;
            dificuldadeTotal = dificuldadeHs;
            bonusTotalNormalizado = bonusTotal / grauContaminacao;
            bonusParcialNormalizado = bonusParcial / contaminadoParcialmente;
            int deltaHsi = grauContaminacao;

            ddouble = contaminadoParcialmente / degree[i];

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
                maiorDouble = ddouble;
                maiorAux = aux[i];
                maiorGrau = di;
                maiorBonusTotalNormalizado = bonusTotalNormalizado;
                maiorBonusParcial = bonusParcial;
                maiorDificuldadeParcial = dificuldadeParcial;
            } else {
                double[] list = null;
//                list = new double[parameters.size() * 2];
                if (rankMult) {
                    list = calcularRankingMulti(i, deltaHsi, contaminadoParcialmente, bonusTotal, bonusParcial, bonusTotalNormalizado, dificuldadeTotal, dificuldadeParcial, profundidadeS, bonusParcialNormalizado, di);
                } else {
                    list = calcularRanking(i, deltaHsi, contaminadoParcialmente, bonusTotal, bonusParcial, bonusTotalNormalizado, dificuldadeTotal, dificuldadeParcial, profundidadeS, bonusParcialNormalizado, di);
                }
                Boolean greater = isGreater(list);
                if (greater == null) {
                    melhores.add(i);
                } else if (greater) {
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
                    maiorDouble = ddouble;
                    maiorAux = aux[i];
                    maiorGrau = di;
                    maiorBonusTotalNormalizado = bonusTotalNormalizado;
                    maiorBonusParcial = bonusParcial;
                    maiorDificuldadeParcial = dificuldadeParcial;
                }
            }
        }
    }

    double multi(double x, int y, int cont) {
        if (cont == 2) {
            return x / (y + 1);
        }
        return x * (y + 1);
    }

    double multi(double x, double y, int cont) {
        if (cont == 2) {
            return x / (y + 1);
        }
        return x * (y + 1);
    }

    protected double[] calcularRankingMulti(int v, int deltaHsi, int contaminadoParcialmente, double bonusTotal, double bonusParcial, double bonusTotalNormalizado, double dificuldadeTotal, double dificuldadeParcial, int profundidadeS, double bonusParcialNormalizado, int di) {
        double[] list = new double[2];
        int cont = 0;
        for (String p : parameters.keySet()) {
            Boolean get = parameters.get(p);
            double p1 = 1, p2 = 1;
            if (get != null) {
                int m = cont - parameters.size() - 2;
                switch (p) {
                    case pdeltaHsi:
                        p1 = multi(p1, deltaHsi, cont);
                        p2 = multi(p2, maiorDeltaHs, cont);
                        break;
                    case pdeltaParcial:
                        p1 = multi(p1, contaminadoParcialmente, cont);
                        p2 = multi(p2, maiorContaminadoParcialmente, cont);
                        break;
                    case pbonusTotal:
                        p1 = multi(p1, bonusTotal, cont);
                        p2 = multi(p2, maiorBonusTotal, cont);
                        break;
                    case pbonusParcial:
                        p1 = multi(p1, bonusParcial, cont);
                        p2 = multi(p2, maiorBonusParcial, cont);
                        break;
                    case pbonusTotalNormalizado:
                        p1 = multi(p1, bonusTotalNormalizado, cont);
                        p2 = multi(p2, maiorBonusTotalNormalizado, cont);
//                                p1 = dificuldadeTotal * deltaHsi;
//                                p2 = maiorDificuldadeTotal * maiorDeltaHs;
                        break;
                    case pdificuldadeTotal:
                        p1 = multi(p1, dificuldadeTotal, cont);
                        p2 = multi(p2, maiorDificuldadeTotal, cont);
                        break;
                    case pdificuldadeParcial:
                        p1 = multi(p1, dificuldadeParcial, cont);
                        p2 = multi(p2, maiorDificuldadeParcial, cont);
                        break;
                    case pprofundidadeS:
                        p1 = multi(p1, profundidadeS, cont);
                        p2 = multi(p2, maiorProfundidadeS, cont);
                        break;
                    case pdeltaHsixdificuldadeTotal:
                        p1 = multi(p1, dificuldadeTotal * deltaHsi, cont);
                        p2 = multi(p2, maiorDificuldadeTotal * maiorDeltaHs, cont);
                        break;
                    case paux:
                        p1 = multi(p1, bonusParcial * contaminadoParcialmente, cont);
                        p2 = multi(p2, maiorBonusParcial * maiorContaminadoParcialmente, cont);
//                                p1 = aux[i];
//                                p2 = maiorAux;
//                                p1 = dificuldadeParcial * contaminadoParcialmente;
//                                p2 = maiorDificuldadeParcial * maiorContaminadoParcialmente;
                        break;
                    case pbonusParcialNormalizado:
                        p1 = multi(p1, bonusParcialNormalizado, cont);
                        p2 = multi(p2, maiorBonusParcialNormalizado, cont);
                        break;
                    case pgrau:
                        p1 = multi(p1, di, cont);
                        p2 = multi(p2, maiorGrau, cont);
                        break;
                    default:
                        break;
                }
            }
            list[0] = p1;
            list[1] = p2;
            cont++;
        }
        return list;
    }

    protected double[] calcularRanking(int v, int deltaHsi, int contaminadoParcialmente, double bonusTotal, double bonusParcial, double bonusTotalNormalizado, double dificuldadeTotal, double dificuldadeParcial, int profundidadeS, double bonusParcialNormalizado, int di) {
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
                    case pdeltaParcial:
                        p1 = contaminadoParcialmente;
                        p2 = maiorContaminadoParcialmente;
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
                        p1 = bonusTotalNormalizado;
                        p2 = maiorBonusTotalNormalizado;
//                                p1 = dificuldadeTotal * deltaHsi;
//                                p2 = maiorDificuldadeTotal * maiorDeltaHs;
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
                    case pdeltaHsixdificuldadeTotal:
                        p1 = dificuldadeTotal * deltaHsi;
                        p2 = maiorDificuldadeTotal * maiorDeltaHs;
                        break;
                    case paux:
                        p1 = bonusParcial * contaminadoParcialmente;
                        p2 = maiorBonusParcial * maiorContaminadoParcialmente;
//                                p1 = aux[i];
//                                p2 = maiorAux;
//                                p1 = dificuldadeParcial * contaminadoParcialmente;
//                                p2 = maiorDificuldadeParcial * maiorContaminadoParcialmente;
                        break;
                    case pbonusParcialNormalizado:
                        p1 = bonusParcialNormalizado;
                        p2 = maiorBonusParcialNormalizado;
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
        return list;
    }

    public int poda(UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux, Collection<Integer> vertices) {
        int cont = 0;
        for (var vi : vertices) {
            if (aux[vi] >= kr[vi]) {
                continue;
            }
            int entropia = graup[vi] - (kr[vi] - aux[vi]);
            if (entropia == 0) {
                aux[vi] = kr[vi];
                for (Integer vn : graph.getNeighborsUnprotected(vi)) {
                    if (aux[vn] >= kr[vn]) {
                        continue;
                    }
                    graup[vn]--;
                }
                cont++;
            }
        }
//        System.out.println("Numero de vertices podados: " + cont);
        return cont;
    }
    int[] auxb = null;

    public Set<Integer> buildOptimizedHullSet(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        List<Integer> vertices = getVertices(graphRead);
        Set<Integer> hullSet = new LinkedHashSet<>();
        Set<Integer> s = new LinkedHashSet<>();

        Integer vl = null;
        Integer maxVertex = (Integer) graphRead.maxVertex() + 1;

        int[] aux = new int[maxVertex];
        scount = new int[maxVertex];
        degree = new int[maxVertex];
        graup = new int[maxVertex];
        pularAvaliacao = new int[maxVertex];
        auxb = new int[maxVertex];

        for (int i = 0; i < maxVertex; i++) {
            aux[i] = 0;
            pularAvaliacao[i] = -1;
            scount[i] = 0;

        }
        grafoconexo = true;
        initKr(graphRead);
        if (sortByDegree) {

        }
        int sizeHs = 0;
        for (Integer v : vertices) {
            degree[v] = graphRead.degree(v);
            graup[v] = degree[v];
            if (degree[v] <= kr[v] - 1) {
                sizeHs = sizeHs + addVertToS(v, s, graphRead, aux);
            }
            if (kr[v] == 0) {
                sizeHs = sizeHs + addVertToAux(v, graphRead, aux);
            }
        }

        if (realizarPoda) {
            int poda = poda(graphRead, aux, vertices);
            sizeHs = sizeHs + poda;
            for (Integer v : vertices) {
                if (aux[v] >= kr[v]) {
                    continue;
                }
                if (graup[v] <= (kr[v] - aux[v]) - 1) {
                    sizeHs = sizeHs + addVertToS(v, s, graphRead, aux);
                }
            }
        }

        int vertexCount = graphRead.getVertexCount();
        int offset = 0;
        if (verbose) {
//            System.out.println("Sini-Size: " + sini.size());
//            System.out.println("Sini: " + sini);
        }

        bdls = BFSUtil.newBfsUtilSimple(maxVertex);
        bdls.labelDistances(graphRead, s);

        bestVertice = -1;

        mapCount = new MapCountOpt(maxVertex);

        while (sizeHs < vertexCount) {
            if (bestVertice != -1) {
                bdls.incBfs(graphRead, bestVertice);
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

            escolherMelhorVertice(graphRead, aux, vertices, sizeHs);
            if (etapaVerbose == s.size()) {
                System.out.println(" - " + bestVertice);
                System.out.println(" - " + melhores);
                for (Integer ml : melhores) {
                    System.out.println("  -- " + ml + " [" + graphRead.getNeighborsUnprotected(ml).size() + "]: " + graphRead.getNeighborsUnprotected(ml));
                }

            }
            if (bestVertice == -1) {
                esgotado = true;
                if (tryMiminal()) {
                    s = tryMinimal(graphRead, s, sizeHs - offset);
                }
                if (tryMiminal2()) {
                    s = tryMinimal2Lite(graphRead, s, sizeHs - offset);
                }
                offset = sizeHs;
                hullSet.addAll(s);
                s.clear();
                bdls.clearBfs();
                continue;
            }
            esgotado = false;
            sizeHs = sizeHs + addVertToS(bestVertice, s, graphRead, aux);
            bdls.incBfs(graphRead, bestVertice);

//            if (sizeHs < vertexCount) {
//                bdl.labelDistances(graph, s);
//            }
        }

//        System.out.println("Vertices de interesse[" + verticesInteresse.size() + "]: ");
        if (tryMiminal()) {
            s = tryMinimal(graphRead, s, sizeHs - offset);
        }
        if (tryMiminal2()) {
            s = tryMinimal2Lite(graphRead, s, sizeHs - offset);
//            s = tryMinimal2(graphRead, s);
        }
        hullSet.addAll(s);
        s.clear();
        return hullSet;
    }

    Map<Integer, Integer> tamanhoT = new HashMap<>();
    int maiorScount = 0;
    int menorScount = Integer.MAX_VALUE;
    int maiorT = 0;
    int menorT = Integer.MAX_VALUE;
    int tamanhoReduzido = 0;

    public Set<Integer> tryMinimal(UndirectedSparseGraphTO<Integer, Integer> graphRead,
            Set<Integer> tmp, int tamanhoAlvo) {
        Set<Integer> s = tmp;
        if (verbose) {
            System.out.println("tentando reduzir: " + s.size());
//            System.out.println("s: " + s);
        }
        tamanhoT.clear();
        maiorScount = 0;
        tamanhoReduzido = 0;
        menorScount = Integer.MAX_VALUE;
        maiorT = 0;
        menorT = Integer.MAX_VALUE;
        for (Integer i : s) {
            if (scount[i] > maiorScount) {
                maiorScount = scount[i];
            }
            if (scount[i] < menorScount) {
                menorScount = scount[i];
            }
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
                Collection<Integer> neighbors = graphRead.getNeighborsUnprotected(verti);
                for (Integer vertn : neighbors) {
                    if (vertn.equals(verti)) {
                        continue;
                    }
                    if (!vertn.equals(verti) && aux[vertn] <= kr[vertn] - 1) {
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
                if (verbose) {
                    System.out.println(
                            "Reduzido removido: " + v
                            + " na posição " + cont + "/" + (tmp.size() - 1)
                    );
                    System.out.println(
                            " - Detalhes de " + graphRead.getName() + " v: "
                            + v + " degree: " + degree[v] + " scount: "
                            + scount[v] + "/[" + menorScount + ","
                            + maiorScount + "] kr:" + kr[v] + " posicao: "
                            + cont + "/" + (s.size() - 1) + " " + grafoconexo
                    );
                }
                if (cont > (tmp.size() / 2) && grafoconexo) {
                    System.out.println(
                            "Poda de v:  " + v
                            + " realizada depois de 50% em grafo conexo "
                            + cont + "/" + (tmp.size() - 1)
                    );
                    System.out.println(
                            " - Detalhes de v: "
                            + v + " degree: " + graphRead.degree(v) + " scount: "
                            + scount[v] + " kr:" + kr[v]
                    );
                }
            } else {
                //            int tamt = contadd - t.size();
                int tamt = contadd;
                tamanhoT.put(v, tamt);
                if (tamt > maiorT) {
                    maiorT = tamt;
                }
                if (tamt < menorT) {
                    menorT = tamt;
                }
            }
        }

        tamanhoReduzido = tmp.size() - s.size();

        if (tamanhoReduzido > 0) {
//            bdls.clearBfs();
//            bdls.labelDistances(graphRead, s);
        }

        if (verbose) {
            System.out.println("reduzido para: " + s.size());
//            System.out.println("s: " + s);
        }
        return s;
    }

    public Set<Integer> tryMinimal2Lite(UndirectedSparseGraphTO<Integer, Integer> graphRead,
            Set<Integer> tmp, int tamanhoAlvo) {
        Set<Integer> s = tmp;
        int menortRef = menorT + tamanhoReduzido + 1;

        if (s.size() <= 2) {
            return s;
        }
        int contVizinhoComum = 0;
        int contSemVizinhoComum = 0;

        List<Integer> tmps = new ArrayList<>(tmp);

//        tmps.sort(Comparator
//                .comparingInt((Integer v) -> -scount[v])
//        //                .thenComparing(v -> -tamanhoT.get(v))
//        );
        maiorScount = 0;
        menorScount = Integer.MAX_VALUE;

        for (int i = 0; i < auxb.length; i++) {
            auxb[i] = 0;
            if (scount[i] > maiorScount) {
                maiorScount = scount[i];
            }
            if (scount[i] < menorScount) {
                menorScount = scount[i];
            }
        }

        List<Integer> ltmp = new ArrayList<>(tmp);
        if (verbose) {
            System.out.println("tentando reduzir-2-lite: " + s.size() + " tamanho alvo: " + tamanhoAlvo);
//            System.out.println("s: " + s);
        }
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
        int cont = 0;
//        int menortRef = menorT + tamanhoReduzido + 1;

        for_p:
//        for (int h = 0; h < ltmp.size() / 2; h++) {

        for (int h = 0; h < ltmp.size(); h++) {
//            UtilProccess.printEndTime();
//            UtilProccess.printStartTime();
            Integer x = ltmp.get(h);
            if (graphRead.degree(x) < kr[x] || !s.contains(x)) {
                continue;
            }
            Integer get = tamanhoT.get(x);
            if (get == null || get > menortRef) {
                if (scount[x] < kr[x] - 1) {
                    continue;
                }
            }
            if (verbose) {
                System.out.println("  - tentando v " + x + " pos: " + h + "/" + (ltmp.size() - 1));
            }
            Collection<Integer> nsY = new LinkedHashSet<>();
            Collection<Integer> nnsy = graphRead.getNeighborsUnprotected(x);
            for (Integer ny : nnsy) {
                if (scount[ny] < kr[ny] + 2) {
                    nsY.add(ny);
                }
            }
            for (int j = h + 1; j < ltmp.size(); j++) {
                Integer y = ltmp.get(j);
                if (degree[y] < kr[y]
                        || !s.contains(y)) {
                    continue;
                }
                Collection<Integer> nsX = graphRead.getNeighborsUnprotected(y);
                boolean xydisjoint = Collections.disjoint(nsX, nsY);
                if (xydisjoint) {
                    continue;
                }
//                Set<Integer> tmpns = new HashSet<>();
//                tmpns.addAll(nsX);
//                tmpns.retainAll(nsY);
//                System.out.println("Vertices em comum x y :" + tmpns);
//                for (Integer vn : tmpns) {
//                    if (s.contains(vn)) {
//                        System.out.println(" - vn " + vn + " e está em s " + scount[vn] + "/" + kr[vn]);
//                    } else {
//                        System.out.println(" - vn " + vn + " não está em s " + scount[vn] + "/" + kr[vn]);
//                    }
//                }
//                if (verbose) {
//                    System.out.println("     --- tentando vx " + x + " pos: " + h + " x " + y + " pos: " + j + "/" + (ltmp.size() - 1));
//                }
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
                        if (vertn.equals(verti)) {
                            continue;
                        }
                        if (!vertn.equals(verti) && aux[vertn] <= kr[vertn] - 1) {
                            aux[vertn] = aux[vertn] + 1;
                            if (aux[vertn] == kr[vertn]) {
                                mustBeIncluded.add(vertn);
                            }
                        }
                    }
                    aux[verti] += kr[verti];
                }
//                Set<Integer> verticesTest = new LinkedHashSet<>(nsX);
//                verticesTest.retainAll(nsY);
//                for (Integer z : vertices) {
                int c = 0;
                for (Integer z : verticesElegiveis) {
                    c++;
                    if (aux[z] >= kr[z] || z.equals(x) || z.equals(y)) {
                        continue;
                    }
                    if (pularAvaliacaoOffset && pularAvaliacao[z] >= contadd) {
                        continue;
                    }
                    if (verbose) {
//                        System.out.println("        --- tentando z " + c + "/" + (verticesElegiveis.size() - 1));
                    }
                    int contz = contadd;
                    int[] auxb = (int[]) aux.clone();
                    mustBeIncluded.add(z);
                    auxb[z] = kr[z];
                    while (!mustBeIncluded.isEmpty()) {
                        Integer verti = mustBeIncluded.remove();
                        contz++;
                        Collection<Integer> neighbors = graphRead.getNeighborsUnprotected(verti);
                        for (Integer vertn : neighbors) {
                            if (vertn.equals(verti)) {
                                continue;
                            }
                            if (!vertn.equals(verti) && auxb[vertn] <= kr[vertn] - 1) {
                                auxb[vertn] = auxb[vertn] + 1;
                                if (auxb[vertn] == kr[vertn]) {
                                    mustBeIncluded.add(vertn);
                                    pularAvaliacao[vertn] = contadd;
                                }
                            }
                        }
                        auxb[verti] += kr[verti];
                    }

                    if (contz == tamanhoAlvo) {
                        if (verbose) {
                            System.out.println("Reduzido removido: " + x + " " + y + " adicionado " + z);
                            System.out.println("Na posição " + cont + "/" + (tmp.size() - 1));
                            System.out.println(" - Detalhes de v: "
                                    + x + " tamt: " + tamanhoT.get(x) + " [" + menorT + "," + maiorT + "]");
                        }
                        if (cont > (tmp.size() / 2)) {
                            System.out.println("Poda dupla em grafo conexo removido:  "
                                    + x + "," + y + " realizada depois de 50% "
                                    + cont + "/" + (tmp.size() - 1));
//                            System.out.println(" - Detalhes de v: "
//                                    + x + " degree: " + graphRead.degree(x) + " scount: "
//                                    + scount[x] + " kr:" + kr[x] + " tamt: " + tamanhoT.get(x));
//                            System.out.println(" - Detalhes de y: "
//                                    + y + " degree: " + graphRead.degree(y) + " scount: "
//                                    + scount[y] + " kr:" + kr[y] + " tamt: " + tamanhoT.get(y)
//                                    + "[" + menorT + "," + maiorT + "]"
//                            );
//                            System.out.println(" - Detalhes de z: "
//                                    + z + " degree: " + graphRead.degree(z) + " scount: "
//                                    + scount[z] + " kr:" + kr[z] + " distancia de s: "
//                                    + bdls.getDistance(graphRead, z));
                        }

                        try {

                            System.out.println(" - Detalhes de z: "
                                    //                                    + z + " degree: " + degree[z] 
                                    + " scount: "
                                    + scount[z]
                                    //                                    + " degreexy:" + degree[x] + "/" + degree[y]
                                    + " " + " scount x/y: " + scount[x] + "/" + scount[y]
                                    + " tamt x/y: " + tamanhoT.get(x) + "/" + tamanhoT.get(y)
                                    + " [" + menorT + "," + menortRef + "," + maiorT + "] " + " está no menor "
                                    + (tamanhoT.get(x) <= menortRef || tamanhoT.get(y) <= menortRef)
                            //+ " distancia de s: "
                            //                                + bdls.getDistance(graphRead, z)
                            );
                        } catch (Exception e) {

                        }

                        for (Integer vertn : nsX) {
                            scount[vertn]--;
                        }
                        for (Integer vertn : nnsy) {
                            scount[vertn]--;
                        }

                        for (Integer vertn : graphRead.getNeighborsUnprotected(z)) {
                            if ((++scount[vertn]) == kr[vertn] && t.contains(vertn)) {
                                t.remove(vertn);
                                Collection<Integer> nn = getNeighborsNaoPodados(graphRead, vertn);
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
                        if (Collections.disjoint(graphRead.getNeighborsUnprotected(x), graphRead.getNeighborsUnprotected(y))) {
                            contSemVizinhoComum++;
                        } else {
                            contVizinhoComum++;
                        }
                        continue for_p;
                    }
                }

            }
            cont++;

        }
        if (contVizinhoComum != 0 || contSemVizinhoComum != 0) {
//            System.out.println("Minimal: sem vizinhos comum " + contSemVizinhoComum + " com vizinhos comuns " + contVizinhoComum);
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

        UndirectedSparseGraphTO<Integer, Integer> graph = null;
        GraphHNVOptm op = new GraphHNVOptm();
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
        graph = UtilGraph.loadBigDataset(
                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/BlogCatalog/nodes.csv"),
                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/BlogCatalog/edges.csv"));

//        System.out.println(graph.toResumedString());
//
//        graph = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/ca-HepTh/ca-HepTh.txt"));
//        op.setR(3);
//        op.resetParameters();
//        op.setPularAvaliacaoOffset(true);
//        op.setTryMinimal(false);
//
//        UtilProccess.printStartTime();
//        Set<Integer> buildOptimizedHullSet = op.buildOptimizedHullSet(graph);
//        System.out.println(op.getName());
//        UtilProccess.printEndTime();
//////        Set<Integer> buildOptimizedHullSet = op.buildOptimizedHullSet(graph);
//        System.out.println(
//                "S[" + buildOptimizedHullSet.size() + "]: " + buildOptimizedHullSet);
//        op.checkIfHullSet(graph, buildOptimizedHullSet);
//        boolean checkIfHullSet = op.checkIfHullSet(graph, buildOptimizedHullSet);
//        if (!checkIfHullSet) {
//            System.err.println("FAIL: fail on check hull setg");
//        }
//        graph = UtilGraph.loadBigDataset(
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/BuzzNet/nodes.csv"),
//                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/BuzzNet/edges.csv"));
        List<String> parametros = new ArrayList<>();
        parametros.addAll(List.of(
                pdeltaHsi, pdeltaParcial,
                pdificuldadeTotal, pdificuldadeParcial,
                pbonusTotal, pbonusParcial,
                //                pbonusTotalNormalizado, 
                //                pbonusParcialNormalizado,
                pprofundidadeS, pgrau,
                paux,
                pdeltaHsixdificuldadeTotal
        //        , pgrau, paux
        ));
        GraphTSSCordasco tss = new GraphTSSCordasco();
        op.map.put(0, new int[0]);
        op.setPularAvaliacaoOffset(true);
        op.setTryMinimal();
        op.setTryMinimal2();
        op.setSortByDegree(true);
        op.setVerbose(true);
//        op.setR(2);
        op.setMarjority(2);
//        op.setRankMult(true);
//        op.setRealizarPoda(true);

//        graph = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/ca-HepTh/ca-HepTh.txt"));
        Map<Integer, Set<Integer>> connectedComponents = op.connectedComponents(graph);

        System.out.println("num conected comps: " + connectedComponents.size());

        UtilProccess.printStartTime();
        Set<Integer> buildOptimizedHullSet = op.buildOptimizedHullSet(graph);
        UtilProccess.printEndTime();

        System.out.println(
                "S[" + buildOptimizedHullSet.size() + "]: " + buildOptimizedHullSet);

//        op.resetParameters();
        int totalGlobal = 0;
        int melhorGlobal = 0;
        int piorGlobal = 0;

//        if (true) {
//            return;
//        }
//        String strFile = "hog-graphs-ge20-le50-ordered.g6";
        String strFile = "database/grafos-rand-densall-n50-150.txt";

        for (int t = 3; t <= 5; t++) //
        {
            System.out.println("Testando ciclo: " + t);

            for (int r = 10; r >= 7; r--) {
                BufferedReader files = new BufferedReader(new FileReader(strFile));
                String line = null;
                int cont = 0;
                MapCountOpt contMelhor = new MapCountOpt(allParameters.size() * 100000);

                while (null != (line = files.readLine())) {
                    graph = UtilGraph.loadGraphES(line);
//                    op.setK(r);
                    op.setR(r);
                    tss.setR(r);
                    Set<Integer> tssCordasco = tss.tssCordasco(graph);
                    Integer melhor = tssCordasco.size();
                    List<int[]> melhores = new ArrayList<>();
                    melhores.add(new int[0]);

//                for (int ip = 0; ip < allParameters.size(); ip++) {
                    Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(parametros.size(), t);
                    while (combinationsIterator.hasNext()) {

                        int[] currentSet = combinationsIterator.next();
//                        op.setPularAvaliacaoOffset(true);
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

                        if (t > 1) {
                            int[] currentRerverse = currentSet.clone();
                            for (int i = 0; i < currentSet.length; i++) {
                                currentRerverse[i] = currentSet[(currentSet.length - 1) - i];
                            }
//                        op.setPularAvaliacaoOffset(false);
                            op.resetParameters();
                            for (int ip : currentRerverse) {
                                String p = parametros.get(ip);
                                op.setParameter(p, true);
                            }
                            optmHullSet = op.buildOptimizedHullSet(graph);
                            name = op.getName();
                            int res2 = optmHullSet.size();
                            out = "R\t g" + cont++ + "\t r"
                                    + r + "\t" + name
                                    + "\t" + res2 + "\n";
                            if (melhor == null) {
                                melhor = res2;
                                melhores.add(currentRerverse);
                            } else if (melhor == res2) {
                                melhores.add(currentRerverse);
                            } else if (melhor > res2) {
                                melhor = res2;
                                melhores.clear();
                                melhores.add(currentRerverse);
                            }
//                        if (res != res2) {
//                            System.out.print("Diferença nos resultados pra o parametro: ");
//                            for (int s : currentSet) {
//                                System.out.print(parametros.get(s));
//                            }
//                            System.out.println();
//                            System.out.println(res + "!=" + res2);
//                        }
                        }
                    }
//                for (Integer i : melhores) {
                    for (int[] ip : melhores) {
                        int i = op.array2idx(ip);
                        contMelhor.inc(i);
                    }
                    cont++;
                }
                files.close();
                System.out.println("\n---------------");
                System.out.println("Resumo r:" + r + " toal de grafos: " + cont);
                op.resetParameters();
                System.out.println("Otimizações iniciais: " + op.getName());

                Map<String, Integer> map = new HashMap<>();
//            for (int ip = 0; ip < allParameters.size(); ip++) {
//                String p = allParameters.get(ip);
////                System.out.println(p + ": " + contMelhor.getCount(ip));
//                map.put(p, contMelhor.getCount(ip));
//            }
                for (int[] i : op.allarrays()) {
                    StringBuilder sb = new StringBuilder();
                    for (int ip : i) {
                        String get = parametros.get(ip);
                        sb.append(get);
//                    String p = allParameters.get(ip);
                        sb.append("-");
//                System.out.println(p + ": " + contMelhor.getCount(ip));
                    }
                    if (sb.length() == 0) {
                        sb.append("tss");
                    }
                    map.put(sb.toString(), contMelhor.getCount(op.array2idx(i)));
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

    private static int apply(GraphHNVOptm op, int[] currentSet, UndirectedSparseGraphTO<Integer, Integer> graph, int cont, int r, Integer melhor, List<int[]> melhores1) {
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

    Map<Integer, int[]> map = new HashMap<>();
    Map<Integer, int[]> mapCiclo = new HashMap<>();

    static final int[] offset = new int[]{1, 10, 100, 1000, 10000};

    public int array2idx(int[] ip) {
        int cont = 0;
        for (int i = 0; i < ip.length; i++) {
            int ipp = ip[i] + 1;
            cont = cont + (offset[i] * ipp);
        }
        map.get(cont);
        int[] put = map.put(cont, ip);
        if (put != null && Arrays.compare(put, ip) != 0) {
            throw new IllegalStateException("Arrays diferentes no mesmo contador: " + cont + " " + Arrays.asList(ip) + " " + Arrays.asList(put));
        }
        return cont;
    }

    public Collection<int[]> allarrays() {
        return map.values();
    }

}
