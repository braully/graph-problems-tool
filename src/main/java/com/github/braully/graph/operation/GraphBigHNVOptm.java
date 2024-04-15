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
        extends GraphHNVOptm implements IGraphOperation {

    private static final Logger log = Logger.getLogger(GraphBigHNVOptm.class);

    static final String description = "HHnV2Big";

    public static String getDescription() {
        return description;
    }

    public GraphBigHNVOptm() {
    }

    public static void main(String... args) throws IOException {
        GraphBigHNVOptm op = new GraphBigHNVOptm();
        int totalGlobal = 0;
        int melhorGlobal = 0;
        int piorGlobal = 0;

        String[] dataSets = new String[]{
            "ca-GrQc", "ca-HepTh",
            //            "ca-CondMat", 
            "ca-HepPh",
            //            "ca-AstroPh",
            //            "Douban", //            "Delicious",
            "BlogCatalog3", //            //            "BlogCatalog2",
        //            //            "Livemocha",
        //            "BlogCatalog",
        //            //            "BuzzNet",
        //            "Last.fm", //             "YouTube2"
        };
//        GraphHullNumberHeuristicV5Tmp heur = new GraphHullNumberHeuristicV5Tmp();
        List<String> parametros = new ArrayList<>();
        parametros.addAll(List.of(
                pdeltaHsixdificuldadeTotal,
                pdificuldadeTotal, pdeltaHsi, pdeltaParcial,
                pbonusTotal, pbonusParcial, pdificuldadeParcial,
                //                pbonusTotalNormalizado, pbonusParcialNormalizado,
                //                pprofundidadeS, 
                pgrau, paux, pprofundidadeS
        //        , pgrau, paux
        ));
        UndirectedSparseGraphTO<Integer, Integer> graph = null;
        //
        op.resetParameters();
        op.setPularAvaliacaoOffset(true);
        op.setTryMinimal();
        op.setSortByDegree(true);

        for (int t = 1; t <= 3; t++) {
            MapCountOpt contMelhorCiclo = new MapCountOpt(allParameters.size() * 100);
            op.resetParameters();
            System.out.println("Inicio Ciclo: " + t);
            System.out.println("Otimizações iniciais: " + op.getName());
            for (int r = 10; r >= 8; r--) {
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
                    graph.setName(s);
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
                        String out = "R\t" + graph.getName() + "\t r"
                                + r + "\t" + name
                                + "\t" + res + "\n";
                        if (melhor == null) {
                            melhor = res;
                            melhores.add(currentSet);
                            System.out.print("inicial: ");
                            System.out.println(out);
                        } else if (melhor == res) {
                            melhores.add(currentSet);
                        } else if (melhor > res) {
                            melhor = res;
                            melhores.clear();
                            melhores.add(currentSet);
                            System.out.print("melhorado: ");
                            System.out.println(out);

                        }
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
                                System.out.print("melhorado: ");
                                System.out.println(out);
                            }
                            System.out.println(out);
                        }
                    }
//                for (Integer i : melhores) {
                    for (int[] ip : melhores) {
                        int i = op.array2idx(ip);
                        contMelhor.inc(i);
                        contMelhorCiclo.inc(i);
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
                    for (int[] i : op.allarrays()) {
                        StringBuilder sb = new StringBuilder();
                        for (int ip : i) {
                            sb.append(parametros.get(ip));
//                    String p = allParameters.get(ip);
                            sb.append("-");
//                System.out.println(p + ": " + contMelhor.getCount(ip));
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
                }

            }
            Map<String, Integer> map = new HashMap<>();
            System.out.println("----------------------");
            System.out.println("Resumo Ciclo: " + t);
            op.resetParameters();
            System.out.println("Otimizações iniciais: " + op.getName());
            for (int[] i : op.allarrays()) {
                StringBuilder sb = new StringBuilder();
                for (int ip : i) {
                    sb.append(parametros.get(ip));
//                    String p = allParameters.get(ip);
                    sb.append("-");
//                System.out.println(p + ": " + contMelhor.getCount(ip));
                }
                map.put(sb.toString(), contMelhorCiclo.getCount(op.array2idx(i)));
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
    }
}
