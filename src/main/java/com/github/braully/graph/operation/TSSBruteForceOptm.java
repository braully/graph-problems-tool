package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import static com.github.braully.graph.operation.GraphHullNumber.PARAM_NAME_HULL_NUMBER;
import static com.github.braully.graph.operation.GraphHullNumber.PARAM_NAME_HULL_SET;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.math3.util.CombinatoricsUtils;
import util.UtilProccess;

public class TSSBruteForceOptm
        extends GraphHNV implements IGraphOperation {

    static final String description = "TSS-BF-Optm";

    GraphHullNumberHeuristicV1 heuristic = new GraphHullNumberHeuristicV1();

    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Integer hullNumber = -1;
        Set<Integer> minHullSet = null;

        try {
            minHullSet = findMinHullSetGraph(graph);
            hullNumber = minHullSet.size();
        } catch (Exception ex) {
            ex.printStackTrace();
//            log.error(null, ex);
        }

        /* Processar a buscar pelo hullset e hullnumber */
        Map<String, Object> response = new HashMap<>();
        response.put(PARAM_NAME_HULL_NUMBER, hullNumber);
        response.put(PARAM_NAME_HULL_SET, minHullSet);
        response.put(IGraphOperation.DEFAULT_PARAM_NAME_RESULT, hullNumber);
        return response;
    }

    @Override
    public Set<Integer> findMinHullSetGraph(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Set<Integer> ceilling = calcCeillingHullNumberGraph(graph);
        Set<Integer> hullSet = ceilling;
        if (graph == null || graph.getVertices().isEmpty()) {
            return ceilling;
        }

        int minSizeSet = 2;
        int currentSize = ceilling.size() - 1;
        int countOneNeigh = 0;

        if (currentSize > 0) {
            Collection<Integer> vertices = graph.getVertices();

            for (Integer i : vertices) {
                if (graph.degree(i) == 1) {
                    countOneNeigh++;
                }
            }
            minSizeSet = Math.max(minSizeSet, countOneNeigh);
            if (verbose) {
                System.out.println(" - Teto heuristico: " + ceilling.size());
            }
//        System.out.println("Find hull number: min val " + minSizeSet);
            while (currentSize >= minSizeSet) {
//            System.out.println("Find hull number: current founded " + (currentSize + 1));
//            System.out.println("Find hull number: trying find " + currentSize);

//            System.out.println("trying : " + currentSize);
                Set<Integer> hs = findHullSetBruteForce(graph, currentSize);
                if (hs != null && !hs.isEmpty()) {
                    hullSet = hs;
                } else {
//                System.out.println("not find break ");
                    break;
                }
                currentSize--;
            }
            if (verbose) {
                int delta = hullSet.size() - ceilling.size();
                if (delta == 0) {
                    System.out.println(" - Heuristica match");
                } else {
                    System.out.println(" - Heuristica fail by: " + delta);

                }
            }
        }
        return hullSet;
    }

    private Set<Integer> calcCeillingHullNumberGraph(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Set<Integer> ceilling = new HashSet<>();
        if (graph != null) {
            Set<Integer> optimizedHullSet = super.buildOptimizedHullSet(graph);
            if (optimizedHullSet != null) {
                ceilling.addAll(optimizedHullSet);
            }
        }
        return ceilling;
    }

    public Set<Integer> findHullSetBruteForce(UndirectedSparseGraphTO<Integer, Integer> graph, int currentSetSize) {
        Set<Integer> hullSet = null;
        if (graph == null || graph.getVertexCount() <= 0) {
            return hullSet;
        }
        int[] aux = auxb;
        int tamanhoAlvo = graph.getVertexCount();

        List<Integer> verticesElegiveis = new ArrayList<>();
        Collection<Integer> vertices = graph.getVertices();
        for (Integer v : vertices) {
            if (kr[v] > 0 && degree[v] <= kr[v]) {
                verticesElegiveis.add(v);
            }
        }
        int size = verticesElegiveis.size();
        if (size == 0) {
            return hullSet;
        }
        Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(size, currentSetSize);
        while (combinationsIterator.hasNext()) {
            int[] currentSet = combinationsIterator.next();
            for (int i = 0; i < aux.length; i++) {
                aux[i] = 0;
            }
            int contadd = 0;
            mustBeIncluded.clear();
            for (Integer i : currentSet) {
                Integer iv = verticesElegiveis.get(i);
                mustBeIncluded.add(iv);
                aux[iv] = kr[iv];
            }
            while (!mustBeIncluded.isEmpty()) {
                Integer verti = mustBeIncluded.remove();
                contadd++;
                Collection<Integer> neighbors = graph.getNeighborsUnprotected(verti);
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
                hullSet = new HashSet<>(currentSetSize);
                for (int i : currentSet) {
                    hullSet.add(i);
                }
                break;
            }
        }
        return hullSet;
    }

    public String getName() {
        return description;
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        TSSBruteForceOptm opf = new TSSBruteForceOptm();
        GraphTSSCordasco tss = new GraphTSSCordasco();
        GraphHNV hnv2 = new GraphHNV();

        UndirectedSparseGraphTO<Integer, Integer> graph = null;

//        graph = UtilGraph.loadGraphES("0-18,0-32,0-53,1-33,1-35,2-5,2-12,2-13,2-16,2-25,2-35,3-11,3-46,4-11,4-13,4-31,4-37,5-7,5-11,5-14,5-35,5-53,6-13,6-19,6-31,6-52,7-9,7-15,7-21,7-24,8-10,8-13,8-22,8-25,8-26,8-37,8-40,8-53,8-54,9-10,9-19,9-31,9-40,10-11,10-31,10-34,10-36,10-50,10-54,11-35,11-46,11-54,12-16,12-44,13-23,13-24,13-31,13-46,13-48,13-52,14-17,14-24,14-27,14-37,15-32,15-33,15-38,15-54,16-31,16-35,17-20,17-23,17-42,17-44,18-20,18-33,18-45,19-21,19-39,19-43,19-48,20-24,20-29,20-33,20-34,20-39,20-40,21-23,21-28,21-40,21-44,21-45,21-50,22-23,22-42,22-49,23-26,23-31,24-26,24-41,24-44,24-54,25-37,26-28,26-52,27-33,27-38,27-39,28-34,28-38,28-44,29-36,30-37,30-46,30-49,30-54,32-35,33-36,33-53,34-44,35-48,35-52,36-52,37-38,37-48,37-51,37-52,38-48,39-47,39-53,40-46,40-50,40-51,41-47,41-54,43-46,43-49,43-54,44-47,46-49,47-49,");
//        opf.doOperation(graph);
//        opf.setMarjority(2);
        Set<Integer> optmHullSet = null;
        String strFile = "database/grafos-rand-densall-n5-100.txt";

        AbstractHeuristic[] operations = new AbstractHeuristic[]{
            opf,
            tss,
            hnv2
        };
        String[] grupo = new String[]{
            "Optm",
            "TSS",
            "HNV"
        };
        Integer[] result = new Integer[operations.length];
        long totalTime[] = new long[operations.length];

        for (String op : new String[]{
            "m", //            "r",
        //            "k"
        }) {
            for (int k = 1; k <= 10; k++) {
                if (op.equals("r")) {
                    tss.setR(k);
                    opf.setR(k);
                    hnv2.setR(k);
                    System.out.println("-------------\n\nR: " + k);
//                    if (k <= 2) {
//                        System.out.println("Pulando resultados já processados: " + op + " " + k);
//                        continue;
//                    }
                } else if (op.equals("m")) {
                    op = "m";
                    opf.setMarjority(k);
                    tss.setMarjority(k);
                    hnv2.setMarjority(k);
                    System.out.println("-------------\n\nm: " + k);
                } else {
                    op = "k";
                    opf.setK(k);
                    tss.setK(k);
                    hnv2.setK(k);
                    System.out.println("-------------\n\nk: " + k);
                }
                if (op.equals("m") && k == 1) {
                    System.out.println("Será ignorado m=1 e k=1");
                    continue;
                }
                BufferedReader files = new BufferedReader(new FileReader(strFile));
                String line = null;
                int contgraph = 0;
                int density = 1;

                while (null != (line = files.readLine())) {
                    graph = UtilGraph.loadGraphES(line);
                    graph.setName("rand-n5-100-dens0" + density + "-cont-" + contgraph);
                    String gname = graph.getName();
                    contgraph++;
                    if ((contgraph % 20) == 0) {
                        density++;
                    }

                    for (int i = 0; i < operations.length; i++) {
//                        String arquivadoStr = operations[i].getName() + "-" + op + k + "-" + s;
                        Map<String, Object> doOperation = null;
//                        System.out.println("*************");
//                        System.out.print(" - EXEC: " + operations[i].getName() + "-" + op + ": " + k + " g:" + s + " " + graph.getVertexCount() + " ");

                        UtilProccess.startTime();
                        doOperation = operations[i].doOperation(graph);
                        totalTime[i] += UtilProccess.endTime();

                        result[i] = (Integer) doOperation.get(IGraphOperation.DEFAULT_PARAM_NAME_RESULT);
//                        System.out.println(" - Result: " + result[i]);

                        String out = "Rand\t" + gname + "\t" + graph.getVertexCount() + "\t"
                                + graph.getEdgeCount()
                                + "\t" + op + "\t" + k + "\t" + grupo[i] + "\t" + operations[i].getName()
                                + "\t" + result[i] + "\t" + totalTime[i] + "\n";

                        System.out.print("xls: " + out);

                        if (doOperation != null) {
                            boolean checkIfHullSet = operations[i].checkIfHullSet(graph, ((Set<Integer>) doOperation.get(DEFAULT_PARAM_NAME_SET)));
                            if (!checkIfHullSet) {
                                System.out.println("ALERT: ----- RESULTADO ANTERIOR IS NOT HULL SET");
//                                System.err.println("ALERT: ----- RESULTADO ANTERIOR IS NOT HULL SET");
                                System.out.println(line);
//                            throw new IllegalStateException("CORDASSO IS NOT HULL SET");
                            }
                        }
//                        System.out.println();
                    }
                }
            }
        }
    }
}
