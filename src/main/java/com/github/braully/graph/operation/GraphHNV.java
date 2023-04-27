package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import static com.github.braully.graph.operation.GraphHullNumber.PARAM_NAME_HULL_NUMBER;
import static com.github.braully.graph.operation.GraphHullNumber.PARAM_NAME_HULL_SET;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
            if (minHullSet != null) {
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

    public Set<Integer> buildOptimizedHullSet(UndirectedSparseGraphTO<Integer, Integer> graph) {
        List<Integer> vertices = getVertices(graph);
        Set<Integer> hullSet = new LinkedHashSet<>();
        Set<Integer> s = new LinkedHashSet<>();

        Integer maxVertex = (Integer) graph.maxVertex() + 1;

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
            maiorDificuldadeTotal = 0;
            maiorDeltaHs = 0;
            maiorBonusParcial = 0;

            for (Integer i : vertices) {
                //Se vertice já foi adicionado, ignorar
                if (aux[i] >= kr[i] || pularAvaliacao[i] >= sizeHs) {
                    continue;
                }
                int profundidadeS = bdls.getDistanceSafe(graph, i);
                if (profundidadeS == -1 && (sizeHs > 0 && !esgotado)) {
                    continue;
                }

                int grauContaminacao = 0;
                int contaminadoParcialmente = 0;
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
                            pularAvaliacao[vertn] = sizeHs;
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
                        contaminadoParcialmente++;
                    }
                }

                dificuldadeTotal = dificuldadeHs;
                int deltaHsi = grauContaminacao;

                if (bestVertice == -1) {
                    bestVertice = i;
                    maiorDeltaHs = deltaHsi;
                    maiorDificuldadeTotal = dificuldadeTotal;
                    maiorBonusParcial = bonusParcial;
                } else {
                    double rank = dificuldadeTotal * deltaHsi;
                    double rankMaior = maiorDificuldadeTotal * maiorDeltaHs;
                    if (rank > rankMaior
                            || (rank == rankMaior && bonusParcial > maiorBonusParcial)) {
                        bestVertice = i;
                        maiorDeltaHs = deltaHsi;
                        maiorDificuldadeTotal = dificuldadeTotal;
                        maiorBonusParcial = bonusParcial;
                    }
                }
            }

            if (bestVertice == -1) {
                esgotado = true;
                s = tryMinimal(graph, s, sizeHs - offset);
                s = tryMinimal2Lite(graph, s, sizeHs - offset);

                offset = sizeHs;
                hullSet.addAll(s);
                s.clear();
                bdls.clearBfs();
                continue;
            }
            esgotado = false;
            sizeHs = sizeHs + addVertToS(bestVertice, s, graph, aux);
            bdls.incBfs(graph, bestVertice);
        }
        s = tryMinimal(graph, s, sizeHs - offset);
        s = tryMinimal2Lite(graph, s, sizeHs - offset);

        hullSet.addAll(s);
        s.clear();
        return hullSet;
    }

    Map<Integer, Integer> tamanhoT = new HashMap<>();
    int menorT = Integer.MAX_VALUE;
    int tamanhoReduzido = 0;

    public Set<Integer> tryMinimal(UndirectedSparseGraphTO<Integer, Integer> graphRead,
            Set<Integer> tmp, int tamanhoAlvo) {
        Set<Integer> s = tmp;

        tamanhoT.clear();
        tamanhoReduzido = 0;
        menorT = Integer.MAX_VALUE;

        if (s.size() <= 1) {
            return s;
        }

        if (verbose) {
            System.out.println("tentando reduzir: " + s.size());
//            System.out.println("s: " + s);
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
                Collection<Integer> neighbors = graphRead.getNeighborsUnprotected(v);
                for (Integer vertn : neighbors) {
                    scount[vertn]--;
                }
            } else {
                //            int tamt = contadd - t.size();
                int tamt = contadd;
                tamanhoT.put(v, tamt);
                if (tamt < menorT) {
                    menorT = tamt;
                }
            }
        }
//        if (verbose) {
//            System.out.println("reduzido para: " + s.size());
////            System.out.println("s: " + s);
//        }
        tamanhoReduzido = tmp.size() - s.size();
        return s;
    }
//    int tamanhoReduzido = 0;

    public Set<Integer> tryMinimal2Lite(UndirectedSparseGraphTO<Integer, Integer> graphRead,
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
        int menortRef = menorT + tamanhoReduzido + 1;

        for_p:
//        for (int h = 0; h < ltmp.size() / 2; h++) {

        for (int h = 0; h < ltmp.size(); h++) {
            Integer x = ltmp.get(h);
            if (degree[x] < kr[x] || !s.contains(x)) {
                continue;
            }
            Integer get = tamanhoT.get(x);
            if (get == null || get > menortRef) {
                if (scount[x] < kr[x] - 1) {
                    continue;
                }
            }
            if (verbose) {
//                System.out.println("  - tentando v " + h + "/" + (ltmp.size() - 1));
            }
//            Collection<Integer> nssY = graphRead.getNeighborsUnprotected(x);
            Collection<Integer> nsY = new LinkedHashSet<>();
            for (Integer ny : graphRead.getNeighborsUnprotected(x)) {
                if (!s.contains(ny)
                        && scount[ny] <= kr[ny] + 1) {
                    nsY.add(ny);
                }
            }
            for (int j = h + 1; j < ltmp.size(); j++) {
                Integer y = ltmp.get(j);
                Collection<Integer> nsX = graphRead.getNeighborsUnprotected(y);
                boolean xydisjoint = Collections.disjoint(nsX, nsY);
                if (degree[y] < kr[y]
                        || !s.contains(y)
                        || xydisjoint) {
                    continue;
                }

                if (verbose) {
//                    System.out.println("     -- tentando x " + j + "/" + (ltmp.size() - 1));
                }
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
                int c = 0;
                for (Integer z : verticesElegiveis) {
                    c++;
                    if (aux[z] >= kr[z] || z.equals(x) || z.equals(y)) {
                        continue;
                    }
                    if (pularAvaliacao[z] >= contadd) {
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

                    if (verbose) {
//                        System.out.println("        --- tentando z " + c + "/" + (verticesElegiveis.size() - 1) + " contz: " + contz + "/" + tamanhoAlvo);
                    }

                    if (contz == tamanhoAlvo) {
//                        if (verbose) {
//                            System.out.println("Reduzido removido: " + x + " " + y + " adicionado " + z);
//                            System.out.println("Na posição x " + "/" + (tmp.size() - 1));
//                            System.out.println(" - Detalhes de v: "
//                                    + x + " tamt: " + tamanhoT.get(x) + " [" + menorT + "," 
////                                    + maiorT 
//                                    + "]");
//                        }
                        for (Integer vertn : nsX) {
                            scount[vertn]--;
                        }
                        for (Integer vertn : graphRead.getNeighborsUnprotected(x)) {
                            scount[vertn]--;
                        }
                        for (Integer vertn : graphRead.getNeighborsUnprotected(z)) {
                            if ((++scount[vertn]) == kr[vertn] && t.contains(vertn)) {
                                t.remove(vertn);
                                Collection<Integer> nn = graphRead.getNeighborsUnprotected(vertn);
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
        graph = UtilGraph.loadGraphES("0-3,0-8,0-12,0-17,0-22,0-30,0-32,0-43,0-47,0-53,0-59,0-63,0-65,0-67,0-68,0-69,0-70,0-71,0-72,0-76,0-85,0-86,0-89,1-16,1-18,1-20,1-31,1-34,1-36,1-38,1-42,1-51,1-59,1-61,1-67,1-71,1-72,1-81,1-87,2-4,2-6,2-7,2-9,2-15,2-17,2-22,2-23,2-25,2-27,2-28,2-30,2-44,2-45,2-46,2-48,2-51,2-78,2-80,2-83,3-6,3-7,3-17,3-27,3-28,3-29,3-36,3-42,3-49,3-52,3-62,3-68,3-88,4-10,4-15,4-18,4-20,4-23,4-26,4-27,4-29,4-30,4-33,4-34,4-66,4-72,4-73,4-78,4-79,4-80,4-83,4-87,5-20,5-27,5-42,5-47,5-49,5-52,5-54,5-58,5-59,5-64,5-76,5-85,5-86,6-8,6-10,6-11,6-19,6-20,6-29,6-34,6-40,6-41,6-42,6-44,6-50,6-54,6-59,6-65,6-69,6-70,6-73,6-76,7-13,7-15,7-18,7-19,7-21,7-30,7-39,7-42,7-43,7-49,7-50,7-62,7-65,7-85,8-11,8-12,8-18,8-19,8-21,8-22,8-36,8-38,8-41,8-43,8-52,8-57,8-64,8-83,8-85,9-12,9-17,9-22,9-28,9-33,9-48,9-49,9-60,9-64,9-68,9-69,9-76,9-79,10-11,10-27,10-29,10-32,10-36,10-37,10-38,10-41,10-49,10-54,10-55,10-59,10-68,10-70,10-77,10-79,10-85,11-17,11-18,11-29,11-71,11-73,11-78,11-84,11-86,12-33,12-34,12-37,12-39,12-41,12-43,12-47,12-49,12-51,12-52,12-59,12-60,12-63,12-64,12-88,13-15,13-20,13-21,13-29,13-32,13-33,13-34,13-38,13-46,13-47,13-51,13-57,13-61,13-63,13-64,13-71,13-72,13-73,13-88,14-16,14-17,14-23,14-24,14-26,14-37,14-47,14-60,14-62,14-65,14-66,14-72,14-80,14-83,15-27,15-30,15-31,15-43,15-50,15-59,15-60,15-62,15-67,15-74,15-79,15-83,15-86,16-21,16-26,16-28,16-34,16-41,16-54,16-55,16-66,16-68,16-73,16-75,16-76,16-86,17-23,17-27,17-28,17-34,17-43,17-47,17-48,17-54,17-58,17-64,17-78,17-82,17-83,17-85,17-86,17-87,18-20,18-24,18-26,18-29,18-35,18-44,18-57,18-60,18-68,18-69,18-80,18-81,19-20,19-23,19-26,19-35,19-38,19-39,19-40,19-45,19-53,19-57,19-63,19-68,19-74,20-33,20-42,20-45,20-56,20-58,20-59,20-66,20-67,20-75,20-82,20-87,20-88,21-24,21-29,21-36,21-46,21-53,21-62,21-63,21-64,21-84,21-87,21-88,22-32,22-41,22-47,22-53,22-55,22-58,22-65,22-66,22-70,22-72,22-73,22-76,22-79,22-80,22-88,23-31,23-33,23-37,23-38,23-43,23-44,23-52,23-56,23-58,23-60,23-61,23-62,23-70,23-76,23-78,23-88,23-89,24-27,24-40,24-46,24-47,24-48,24-49,24-50,24-54,24-58,24-61,24-63,24-66,24-72,24-73,24-74,24-76,24-78,24-83,24-85,25-30,25-32,25-35,25-36,25-42,25-47,25-57,25-62,25-63,25-67,25-74,25-84,26-29,26-40,26-43,26-44,26-65,26-70,26-73,26-74,26-81,26-86,27-28,27-29,27-32,27-33,27-37,27-38,27-47,27-50,27-55,27-65,27-66,27-67,27-73,27-77,27-78,27-85,27-88,28-32,28-36,28-39,28-55,28-58,28-64,28-76,28-88,28-89,29-32,29-34,29-38,29-42,29-45,29-46,29-51,29-59,29-62,29-70,29-76,29-77,30-31,30-34,30-49,30-63,30-81,30-85,30-88,31-37,31-40,31-43,31-46,31-51,31-53,31-57,31-62,31-65,31-67,31-77,31-78,31-79,31-83,31-85,31-89,32-37,32-39,32-42,32-52,32-58,32-65,32-68,32-70,32-75,32-76,32-78,32-83,32-86,33-36,33-42,33-44,33-47,33-48,33-52,33-72,33-76,33-78,33-83,33-88,34-36,34-65,34-66,34-72,34-73,34-85,35-43,35-44,35-54,35-58,35-80,35-82,35-87,36-39,36-43,36-49,36-53,36-54,36-71,36-81,37-38,37-48,37-51,37-54,37-55,37-59,37-62,37-65,37-72,37-73,37-79,37-81,37-82,37-84,37-85,37-89,38-47,38-54,38-59,38-60,38-63,38-66,38-69,38-70,38-87,39-48,39-50,39-55,39-60,39-64,39-69,39-78,40-41,40-45,40-49,40-51,40-53,40-54,40-60,40-64,40-68,40-71,40-74,40-77,40-82,40-85,41-43,41-50,41-56,41-61,41-66,41-68,41-72,41-75,42-49,42-62,42-63,42-67,42-76,42-78,42-81,42-85,43-51,43-58,43-61,43-63,43-68,43-74,43-79,43-83,43-84,43-87,44-49,44-50,44-53,44-62,44-64,44-67,44-69,44-70,44-74,44-75,45-50,45-55,45-58,45-62,45-66,45-71,45-78,45-85,45-88,46-52,46-61,46-66,46-72,46-78,46-80,46-81,46-82,46-83,46-87,47-56,47-57,47-64,47-69,47-72,47-80,47-82,48-53,48-54,48-57,48-58,48-62,48-71,48-74,48-80,48-84,48-85,48-87,49-51,49-54,49-59,49-61,49-63,49-64,49-66,49-70,49-72,49-73,49-77,49-84,49-87,49-89,50-57,50-58,50-63,50-71,50-76,50-77,50-78,50-81,50-86,50-89,51-54,51-57,51-59,51-67,51-83,51-85,51-89,52-55,52-58,52-64,52-68,52-72,52-73,52-74,52-77,52-83,52-85,53-64,53-74,53-75,53-78,53-81,53-88,54-55,54-63,54-68,54-70,54-71,54-75,54-78,54-88,55-57,55-61,55-62,55-64,55-68,55-73,55-81,55-88,56-59,56-65,56-68,56-78,56-86,56-88,57-62,57-65,57-66,57-67,57-69,57-82,58-60,58-73,58-76,58-80,58-83,58-84,58-86,59-61,59-68,59-77,59-78,59-79,59-87,59-88,60-61,60-66,60-77,60-80,60-83,60-89,61-63,61-64,61-78,61-82,61-83,61-84,61-86,62-69,62-70,62-74,63-72,63-78,63-79,63-82,63-85,63-87,64-65,64-68,64-72,64-73,64-75,64-76,65-66,65-71,65-73,65-74,65-75,65-80,65-87,65-88,66-79,67-82,67-83,68-81,68-82,69-76,69-81,69-84,70-76,70-78,70-79,70-83,71-76,72-76,72-79,72-81,72-82,72-83,72-84,72-86,73-78,73-84,73-89,74-75,74-80,74-84,74-88,75-78,75-79,75-83,75-88,77-82,77-84,77-87,78-82,78-87,78-88,79-80,79-86,80-83,81-86,81-88,81-89,82-85,83-84,84-86,84-88,85-86,85-87,85-88,87-88,");
        GraphHNV op = new GraphHNV();
        op.setVerbose(true);
        op.setP(1);

        Set<Integer> buildOptimizedHullSet = null;
        UtilProccess.printStartTime();
        buildOptimizedHullSet = op.buildOptimizedHullSet(graph);
        UtilProccess.printEndTime();

        System.out.println(
                "S[" + buildOptimizedHullSet.size() + "]: "
                + buildOptimizedHullSet
        );

        boolean checkIfHullSet = op.checkIfHullSet(graph, buildOptimizedHullSet);
        if (!checkIfHullSet) {
            System.out.println("Is not hull set");
        }
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
