package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import static com.github.braully.graph.operation.GraphHullNumber.PARAM_NAME_HULL_NUMBER;
import static com.github.braully.graph.operation.GraphHullNumber.PARAM_NAME_HULL_SET;
import com.sun.jna.platform.win32.ShellAPI;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
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
//            if (profundidadeS == -1 && (sizeHs > 0 && !esgotado)) {
//                grafoconexo = false;
//                continue;
//            }
//            if (pularAvaliacaoOffset && pularAvaliacao[i] >= sizeHs) {
//                continue;
//            }

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
                                p1 = dificuldadeTotal * deltaHsi;
                                p2 = maiorDificuldadeTotal * maiorDeltaHs;
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

    public Set<Integer> buildOptimizedHullSet(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        List<Integer> vertices = new ArrayList<>((List<Integer>) graphRead.getVertices());
        Set<Integer> hullSet = null;
        Integer vl = null;
        Set<Integer> sini = new LinkedHashSet<>();
        Integer maxVertex = (Integer) graphRead.maxVertex() + 1;

        int[] aux = new int[maxVertex];
        scount = new int[maxVertex];
        degree = new int[maxVertex];
        pularAvaliacao = new int[maxVertex];
        for (int i = 0; i < maxVertex; i++) {
            aux[i] = 0;
            pularAvaliacao[i] = -1;
            scount[i] = 0;

        }
        grafoconexo = true;
        initKr(graphRead);
        int sizeHs = 0;
        for (Integer v : vertices) {
            degree[v] = graphRead.degree(v);
            if (degree[v] <= kr[v] - 1) {
                sizeHs = sizeHs + addVertToS(v, sini, graphRead, aux);
            }
            if (kr[v] == 0) {
                sizeHs = sizeHs + addVertToAux(v, graphRead, aux);
            }
        }

        Set<Integer> s = new LinkedHashSet<>(sini);
        int vertexCount = graphRead.getVertexCount();
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
        if (tryMiminal2()) {
            s = tryMinimal2Lite(graphRead, s);
//            s = tryMinimal2(graphRead, s);
        }

        hullSet = s;

        if (hullSet == null) {
            hullSet = sini;

        }
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

    public static void main(String... args) throws IOException {

        UndirectedSparseGraphTO<Integer, Integer> graph = null;
        GraphHNVOptm op = new GraphHNVOptm();

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
//
//        System.out.println(graph.toResumedString());
//
////        System.out.println("Conected componentes: ");
////        Map<Integer, Set<Integer>> connectedComponents = op.connectedComponents(graph);
////        for (Entry<Integer, Set<Integer>> e : connectedComponents.entrySet()) {
////            System.out.println("" + e.getKey() + ": " + e.getValue().size());
////        }
////        }
        graph = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/ca-HepTh/ca-HepTh.txt"));
//        op.setR(3);
        op.resetParameters();
        op.setPularAvaliacaoOffset(true);
//        op.setRealizarPoda(false);
//        op.setVerbose(true);
//        op.setTryMinimal();
        op.setTryMinimal(false);
//        op.setParameter(pdificuldadeTotal, true);
//        op.setParameter(pbonusParcialNormalizado, true);
////        op.decompor = true;
////        op.setParameter(GraphBigHNVOptm.paux, true);
////        op.setParameter(GraphBigHNVOptm.pgrau, true);
//        op.setParameter(GraphBigHNVOptm.pdeltaHsi, true);
//        op.setParameter(pbonusTotal, true);

//        op.setParameter(pdificuldadeParcial, true);
        UtilProccess.printStartTime();
        Set<Integer> buildOptimizedHullSet = op.buildOptimizedHullSet(graph);
        System.out.println(op.getName());
        UtilProccess.printEndTime();
//
        System.out.println(
                "S[" + buildOptimizedHullSet.size() + "]: " + buildOptimizedHullSet);
        op.checkIfHullSet(graph, buildOptimizedHullSet);
        boolean checkIfHullSet = op.checkIfHullSet(graph, buildOptimizedHullSet);
        if (!checkIfHullSet) {
            System.err.println("FAIL: fail on check hull setg");
        }
        List<String> parametros = new ArrayList<>();
        parametros.addAll(List.of(
                pdeltaHsi, pdeltaParcial, pdificuldadeTotal,
                pbonusTotal, pbonusParcial, pdificuldadeParcial,
                pbonusTotalNormalizado, pbonusParcialNormalizado,
                pprofundidadeS, pgrau, paux
        //        , pgrau, paux
        ));
        int totalGlobal = 0;
        int melhorGlobal = 0;
        int piorGlobal = 0;

//        if (true) {
//            return;
//        }
//        String strFile = "hog-graphs-ge20-le50-ordered.g6";
        String strFile = "database/grafos-rand-densall-n50-150.txt";

        for (int t = 1; t <= 3; t++) //
        {
            System.out.println("Testando ciclo: " + t);
            for (int r = 4; r <= 10; r++) {
                BufferedReader files = new BufferedReader(new FileReader(strFile));
                String line = null;
                int cont = 0;
                MapCountOpt contMelhor = new MapCountOpt(allParameters.size() * 100);

                while (null != (line = files.readLine())) {
                    graph = UtilGraph.loadGraphES(line);
                    op.setR(r);
                    Integer melhor = null;
                    List<int[]> melhores = new ArrayList<>();
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
                        int i = array2idx(ip);
                        contMelhor.inc(i);
                    }
                    cont++;
                }
                files.close();
                System.out.println("\n---------------");
                System.out.println("Resumo r:" + r + " toal de grafos: " + cont);

                Map<String, Integer> map = new HashMap<>();
//            for (int ip = 0; ip < allParameters.size(); ip++) {
//                String p = allParameters.get(ip);
////                System.out.println(p + ": " + contMelhor.getCount(ip));
//                map.put(p, contMelhor.getCount(ip));
//            }
                for (int[] i : allarrays()) {
                    StringBuilder sb = new StringBuilder();
                    for (int ip : i) {
                        String get = allParameters.get(ip);
                        sb.append(get);
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

}
