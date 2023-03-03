package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import static com.github.braully.graph.operation.GraphHullNumber.PARAM_NAME_HULL_NUMBER;
import static com.github.braully.graph.operation.GraphHullNumber.PARAM_NAME_HULL_SET;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

public class GraphHNV
        extends AbstractHeuristicOptm implements IGraphOperation {

    private static final Logger log = Logger.getLogger(GraphHNV.class);

    static final String description = "HHnV2-final";

    public static String getDescription() {
        return description;
    }

    @Override
    public String getName() {
        return "HHnV2:st:pa:tt2";
    }

    public GraphHNV() {
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

    int[] auxb = null;

    @Override
    public List<Integer> getVertices(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        List<Integer> vertices = new ArrayList<>((List<Integer>) graphRead.getVertices());
        vertices.sort(Comparator
                .comparingInt((Integer v) -> -graphRead.degree(v))
        //                .thenComparing(v -> -v)
        );
        return vertices;
    }

    public Set<Integer> buildOptimizedHullSet(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        List<Integer> vertices = getVertices(graphRead);
        Set<Integer> hullSet = new LinkedHashSet<>();
        Set<Integer> s = new LinkedHashSet<>();

        Integer maxVertex = (Integer) graphRead.maxVertex() + 1;

        int[] aux = new int[maxVertex];
        scount = new int[maxVertex];
        degree = new int[maxVertex];
        pularAvaliacao = new int[maxVertex];
        auxb = new int[maxVertex];

        for (int i = 0; i < maxVertex; i++) {
            aux[i] = 0;
            pularAvaliacao[i] = -1;
            scount[i] = 0;

        }
        initKr(graphRead);

        int sizeHs = 0;
        for (Integer v : vertices) {
            degree[v] = graphRead.degree(v);
            if (degree[v] <= kr[v] - 1) {
                sizeHs = sizeHs + addVertToS(v, s, graphRead, aux);
            }
            if (kr[v] == 0) {
                sizeHs = sizeHs + addVertToAux(v, graphRead, aux);
            }
        }

        int vertexCount = graphRead.getVertexCount();
        int offset = 0;

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

            for (Integer i : vertices) {
                //Se vertice já foi adicionado, ignorar
                if (aux[i] >= kr[i]) {
                    continue;
                }
                int profundidadeS = bdls.getDistanceSafe(graphRead, i);
                if (profundidadeS == -1 && (sizeHs > 0 && !esgotado)) {
                    continue;
                }
                if (pularAvaliacao[i] >= sizeHs) {
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

                profundidadeS = 0;
                int di = 0;

                while (!mustBeIncluded.isEmpty()) {
                    Integer verti = mustBeIncluded.remove();
                    Collection<Integer> neighbors = graphRead.getNeighborsUnprotected(verti);
                    for (Integer vertn : neighbors) {
                        if ((aux[vertn] + mapCount.getCount(vertn)) >= kr[vertn]) {
                            continue;
                        }
                        Integer inc = mapCount.inc(vertn);
                        if ((inc + aux[vertn]) == kr[vertn]) {
                            mustBeIncluded.add(vertn);
                            pularAvaliacao[vertn] = sizeHs;
                        }
                    }
                    double bonus = degree[verti] - kr[verti];
                    double dificuldade = (kr[verti] - aux[verti]);

                    bonusHs += bonus;
                    dificuldadeHs += dificuldade;
                    profundidadeS += bdls.getDistanceSafe(graphRead, verti) + 1;
                    grauContaminacao++;
                    di += degree[verti];
                }

                for (Integer x : mapCount.keySet()) {
                    if (mapCount.getCount(x) + aux[x] < kr[x]) {
                        int dx = degree[x];
                        double bonus = dx - kr[x];
                        bonusParcial += bonus;
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
                    double rank = dificuldadeTotal * deltaHsi;
                    double rankMaior = maiorDificuldadeTotal * maiorDeltaHs;
                    if (rank > rankMaior
                            || (rank == rankMaior && bonusParcial > maiorBonusParcial)) {
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

            if (bestVertice == -1) {
                esgotado = true;
                s = tryMinimal(graphRead, s, sizeHs - offset);
                s = tryMinimal2Lite(graphRead, s, sizeHs - offset);

                offset = sizeHs;
                hullSet.addAll(s);
                s.clear();
                bdls.clearBfs();
                continue;
            }
            esgotado = false;
            sizeHs = sizeHs + addVertToS(bestVertice, s, graphRead, aux);
            bdls.incBfs(graphRead, bestVertice);
        }
        s = tryMinimal(graphRead, s, sizeHs - offset);
        s = tryMinimal2Lite(graphRead, s, sizeHs - offset);

        hullSet.addAll(s);
        s.clear();
        return hullSet;
    }

    public Set<Integer> tryMinimal(UndirectedSparseGraphTO<Integer, Integer> graphRead,
            Set<Integer> tmp, int tamanhoAlvo) {
        Set<Integer> s = tmp;
//        if (verbose) {
//            System.out.println("tentando reduzir: " + s.size());
////            System.out.println("s: " + s);
//        }
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
                if (scount[i] > maiorScount) {
                    maiorScount = scount[i];
                }
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
            }
        }
//        if (verbose) {
//            System.out.println("reduzido para: " + s.size());
////            System.out.println("s: " + s);
//        }
        return s;
    }

    public Set<Integer> tryMinimal2Lite(UndirectedSparseGraphTO<Integer, Integer> graphRead,
            Set<Integer> tmp, int tamanhoAlvo) {
        Set<Integer> s = tmp;
//        if (verbose) {
//            System.out.println("tentando reduzir-2-lite: " + s.size() + " tamanho alvo: " + tamanhoAlvo);
////            System.out.println("s: " + s);
//        }
        List<Integer> ltmp = new ArrayList<>(tmp);
        Collection<Integer> vertices = graphRead.getVertices();
        List<Integer> verticesElegiveis = new ArrayList<>();
        for (Integer v : vertices) {
            Integer distance = bdls.getDistance(graphRead, v);
            if (!s.contains(v) && distance != null && distance <= 1) {
                verticesElegiveis.add(v);
            }
        }

//        if (verbose) {
//            System.out.println("vertices elegiveis " + verticesElegiveis.size());
////            System.out.println("s: " + s);
//        }

        for_p:
//        for (int h = 0; h < ltmp.size() / 2; h++) {

        for (int h = 0; h < ltmp.size(); h++) {
            Integer x = ltmp.get(h);
            if (degree[x] < kr[x] || !s.contains(x)) {
                continue;
            }
//            if (verbose) {
//                System.out.println("  - tentando v " + h + "/" + (ltmp.size() - 1));
//            }
            Collection<Integer> nsY = graphRead.getNeighborsUnprotected(x);
            for (int j = h + 1; j < ltmp.size(); j++) {
                Integer y = ltmp.get(j);
                Collection<Integer> nsX = graphRead.getNeighborsUnprotected(y);
                boolean xydisjoint = Collections.disjoint(nsX, nsY);
                if (degree[y] < kr[y]
                        || !s.contains(y)
                        || xydisjoint) {
                    continue;
                }

//                if (verbose) {
//                    System.out.println("     -- tentando x " + j + "/" + (ltmp.size() - 1));
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
                        if (aux[vertn] <= kr[vertn] - 1) {
                            aux[vertn] = aux[vertn] + 1;
                            if (aux[vertn] == kr[vertn]) {
                                mustBeIncluded.add(vertn);
                            }
                        }
                    }
                    aux[verti] += kr[verti];
                }
//                int c = 0;
                for (Integer z : verticesElegiveis) {
//                    c++;
                    if (aux[z] >= kr[z] || z.equals(x) || z.equals(y)) {
                        continue;
                    }
                    if (pularAvaliacaoOffset && pularAvaliacao[z] >= contadd) {
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
                                pularAvaliacao[vertn] = contadd;
                            }
                        }
                    }

//                    if (verbose) {
//                        System.out.println("        --- tentando z " + c + "/" + (verticesElegiveis.size() - 1) + " contz: " + contz + "/" + tamanhoAlvo);
//                    }

                    if (contz == tamanhoAlvo) {
                        t.add(z);
                        s = t;
                        ltmp = new ArrayList<>(s);
                        h--;
//                        h = 0;
//                        h = h / 2;
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

        UndirectedSparseGraphTO<Integer, Integer> graph = null;
        GraphHNV op = new GraphHNV();
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
//
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
//        graph = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/BuzzNet/BuzzNet.txt"));
        graph = UtilGraph.loadBigDataset(
                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/BuzzNet/nodes.csv"),
                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/BuzzNet/edges.csv")
        );

        GraphTSSCordasco tss = new GraphTSSCordasco();
        op.map.put(0, new int[0]);
        op.setPularAvaliacaoOffset(true);
        op.setTryMinimal();
        op.setTryMinimal2();
        op.setSortByDegree(true);
        op.setR(2);

        op.setVerbose(true);
        Map<Integer, Set<Integer>> connectedComponents = op.connectedComponents(graph);

        System.out.println("num conected comps: " + connectedComponents.size());
        Set<Integer> buildOptimizedHullSet = null;
        //
//        UtilProccess.printStartTime();
//        buildOptimizedHullSet = op.buildOptimizedHullSet(graph);
//        UtilProccess.printEndTime();
//        System.out.println(
//                "S[" + buildOptimizedHullSet.size() + "]: " + buildOptimizedHullSet);

        GraphHNV hnv2 = new GraphHNV();
        hnv2.setVerbose(true);
        hnv2.setR(3);
        UtilProccess.printStartTime();
        buildOptimizedHullSet = hnv2.buildOptimizedHullSet(graph);
        UtilProccess.printEndTime();

        System.out.println(
                "S[" + buildOptimizedHullSet.size() + "]: " + buildOptimizedHullSet);

    }

    private static int apply(GraphHNV op, int[] currentSet, UndirectedSparseGraphTO<Integer, Integer> graph, int cont, int r, Integer melhor, List<int[]> melhores1) {
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
