/*
 * The MIT License
 *
 * Copyright 2022 strike.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import com.github.braully.graph.operation.AbstractHeuristic;
import com.github.braully.graph.operation.AbstractHeuristicOptm;
import static com.github.braully.graph.operation.AbstractHeuristicOptm.pdeltaHsi;
import static com.github.braully.graph.operation.AbstractHeuristicOptm.pdificuldadeTotal;
import com.github.braully.graph.operation.GraphHNV;
import com.github.braully.graph.operation.GraphHNVOptm;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV1;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV2;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV3;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV4;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV5;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV5Tmp;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV5Tmp3;
import com.github.braully.graph.operation.GraphTSSCordasco;
import com.github.braully.graph.operation.GraphTSSGreedy;
import com.github.braully.graph.operation.IGraphOperation;
import static com.github.braully.graph.operation.IGraphOperation.DEFAULT_PARAM_NAME_SET;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import util.UtilProccess;

/**
 *
 * @author strike
 */
public class ExecBigDataSets3 {

    public static void main(String... args) throws FileNotFoundException, IOException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String[] dataSets = new String[]{
            //            "ca-GrQc", "ca-HepTh",
            //            "ca-CondMat",
            //            "ca-HepPh",
            //            "ca-AstroPh",
            //            "Douban",
            //            "Delicious",
            //            "BlogCatalog3",
            //            "BlogCatalog2",
            //            "Livemocha",
            //            "BlogCatalog",
            //            "BuzzNet",
            //            "Last.fm", 
            "YouTube2"
        };
//        GraphHullNumberHeuristicV5Tmp heur = new GraphHullNumberHeuristicV5Tmp();

        GraphHullNumberHeuristicV4 heur4 = new GraphHullNumberHeuristicV4();
        heur4.setVerbose(false);
        GraphHullNumberHeuristicV3 heur3 = new GraphHullNumberHeuristicV3();
        heur3.setVerbose(false);
        GraphHullNumberHeuristicV2 heur2 = new GraphHullNumberHeuristicV2();
        heur2.setVerbose(false);
        GraphHullNumberHeuristicV1 heur1 = new GraphHullNumberHeuristicV1();
//        heur1.fatorLimite = 2;
        heur1.setVerbose(true);
        GraphHullNumberHeuristicV5 heur5 = new GraphHullNumberHeuristicV5();
        heur5.setVerbose(false);
        GraphHullNumberHeuristicV5Tmp heur5t = new GraphHullNumberHeuristicV5Tmp();
        heur5t.setVerbose(false);
//        GraphHullNumberHeuristicV5Tmp2 heur5t2 = new GraphHullNumberHeuristicV5Tmp2();
        GraphHullNumberHeuristicV5Tmp3 heur5t2 = new GraphHullNumberHeuristicV5Tmp3();
//        GraphBigHNVOptm optm = new GraphBigHNVOptm();
        GraphHNVOptm optm = new GraphHNVOptm();

        heur5t2.setVerbose(false);
        heur5t2.startVertice = false;

        optm.resetParameters();
        optm.setPularAvaliacaoOffset(true);
        optm.setSortByDegree(true);
        optm.setTryMinimal();
//        optm.setTryMinimal2();
//        optm.setParameter(pdificuldadeTotal, true);
//        optm.setParameter(pbonusParcialNormalizado, true);
//        optm.setParameter(pdeltaHsi, true);
//        optm.setParameter(pdificuldadeTotal, true);
        optm.setParameter(AbstractHeuristicOptm.pdeltaHsixdificuldadeTotal, true);
        optm.setParameter(AbstractHeuristicOptm.pbonusParcial, true);
//        optm.setParameter(AbstractHeuristicOptm.pdeltaHsi, true);
//        optm.setParameter(AbstractHeuristicOptm.pbonusParcial, true);

//        optm.setVerbose(true);
//        optm.setParameter(GraphBigHNVOptm.pdeltaHsi, true);
//        optm.setParameter(GraphBigHNVOptm.pgrau, true);
//        optm.setParameter(GraphBigHNVOptm.pbonusTotal, true);
//        optm.setParameter(GraphBigHNVOptm.pdificuldadeParcial, true);
//        optm.setParameter(GraphBigHNVOptm.pbonusParcialNormalizado, true);
//        optm.setParameter(GraphBigHNVOptm.pdificuldadeTotal, true);
//        optm.setParameter(GraphBigHNVOptm.pdificuldadeParcial, false);
//        optm.setParameter(GraphBigHNVOptm.pbonusTotal, false);
        GraphTSSCordasco tss = new GraphTSSCordasco();
        GraphTSSGreedy tssg = new GraphTSSGreedy();
        GraphHNV hnv2 = new GraphHNV();

        AbstractHeuristic[] operations = new AbstractHeuristic[]{
            //            tss, //            heur1,
            //            heur2, 
            //            heur3, heur4,
            //            heur5,
            //            heur5t,
            //            tssg,
            //            heur5t2
            //            optm
            hnv2
        };
        long totalTime[] = new long[operations.length];
        Integer[] result = new Integer[operations.length];
        Integer[] delta = new Integer[operations.length];
        int[] contMelhor = new int[operations.length];
        int[] contPior = new int[operations.length];
        int[] contIgual = new int[operations.length];
        for (int i = 0; i < operations.length; i++) {
            contMelhor[i] = contPior[i] = contIgual[i] = 0;
        }

        Arrays.sort(dataSets);

        String strResultFile = "resultado-" + ExecBigDataSets.class.getSimpleName() + ".txt";
        File resultFile = new File(strResultFile);
        BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile, true));
        for (String op : new String[]{
            //                        "k",
            //            "r"
            "m"
        }) {
            for (int k = 10; k >= 1; k--) {
//            heur1.K = heur2.K = heur3.K
//                    = heur4.K = heur5.K = heur5t.K = heur5t2.K = tss.K = tssg.K = k;
//            tss.setR(k);
//                String op = "r";
                if (op.equals("r")) {
                    heur5t2.setR(k);
                    optm.setR(k);
                    tss.setR(k);
                    hnv2.setR(k);
                    System.out.println("-------------\n\nR: " + k);
                } else if (op.equals("m")) {
                    op = "m";
                    heur5t2.setMarjority(k);
                    optm.setMarjority(k);
                    tss.setMarjority(k);
                    hnv2.setMarjority(k);
                    System.out.println("-------------\n\nm: " + k);
                } else {
                    op = "k";
                    heur5t2.setK(k);
                    optm.setK(k);
                    tss.setK(k);
                    hnv2.setK(k);
                    System.out.println("-------------\n\nk: " + k);
                }
                for (String s : dataSets) {
                    System.out.println("\n-DATASET: " + s);

                    UndirectedSparseGraphTO<Integer, Integer> graphES
                            = null;
                    try {
                        graphES = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/" + s + "/nodes.csv"),
                                new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/" + s + "/edges.csv"));
                    } catch (FileNotFoundException e) {
                        graphES = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/" + s + "/" + s + ".txt"));
                    }
                    if (graphES == null) {
                        System.out.println("Fail to Load GRAPH: " + s);
                    }
                    System.out.println("Loaded Graph: " + s + " " + graphES.getVertexCount() + " " + graphES.getEdgeCount());

                    for (int i = 0; i < operations.length; i++) {
                        String arquivadoStr = operations[i].getName() + "-" + op + k + "-" + s;
                        Map<String, Object> doOperation = null;
//                    BeanUtils.setProperty(operations[i], "K", k);
//                    PropertyUtils.setSimpleProperty(operations[i], "K", k);
                        System.out.println("*************");
                        System.out.print(" - EXEC: " + operations[i].getName() + "-" + op + ": " + k + " g:" + s + " " + graphES.getVertexCount() + " ");
                        int[] get = ExecBigDataSets.resultadoArquivado.get(arquivadoStr);
                        if (get != null) {
                            result[i] = get[0];
                            totalTime[i] = get[1];
                        } else {
                            UtilProccess.printStartTime();
                            doOperation = operations[i].doOperation(graphES);
                            result[i] = (Integer) doOperation.get(IGraphOperation.DEFAULT_PARAM_NAME_RESULT);
                            totalTime[i] += UtilProccess.printEndTime();
                            System.out.println(" - arquivar: resultadoArquivado.put(\"" + arquivadoStr + "\", new int[]{" + result[i] + ", " + totalTime[i] + "});");
                        }
                        System.out.println(" - Result: " + result[i]);

                        String out = "Big\t" + s + "\t" + graphES.getVertexCount() + "\t"
                                + graphES.getEdgeCount()
                                + "\t" + op + "\t" + k + "\t" + " " + "\t" + operations[i].getName()
                                + "\t" + result[i] + "\t" + totalTime[i] + "\n";

                        System.out.print("xls: " + out);

                        writer.write(out);
//                        writer.write(resultProcess);
                        writer.flush();

                        if (doOperation != null) {
                            boolean checkIfHullSet = operations[i].checkIfHullSet(graphES, ((Set<Integer>) doOperation.get(DEFAULT_PARAM_NAME_SET)));
                            if (!checkIfHullSet) {
                                System.out.println("ALERT: ----- RESULTADO ANTERIOR IS NOT HULL SET");
                                System.err.println("ALERT: ----- RESULTADO ANTERIOR IS NOT HULL SET");
//                            throw new IllegalStateException("CORDASSO IS NOT HULL SET");
                            }
                        }
                        if (i == 0) {
                            if (get == null) {
                                delta[i] = 0;
                            }
                        } else {
                            delta[i] = result[0] - result[i];

                            long deltaTempo = totalTime[0] - totalTime[i];
                            System.out.print(" - g:" + s + " " + op + " " + k + "  tempo: ");

                            if (deltaTempo >= 0) {
                                System.out.print(" +RAPIDO " + deltaTempo);
                            } else {
                                System.out.print(" +LENTO " + deltaTempo);
                            }
                            System.out.print(" - Delta: " + delta[i] + " ");
                            if (delta[i] == 0) {
                                System.out.print(" = igual");
                                contIgual[i]++;
                            } else if (delta[i] > 0) {
                                System.out.print(" + MELHOR ");
                                contMelhor[i]++;
                            } else {
                                System.out.print(" - PIOR ");
                                contPior[i]++;
                            }
                            System.out.println(delta[i]);
                        }
                        System.out.println();
                    }
                }
            }
        }
        writer.flush();
        writer.close();
        System.out.println("Resumo ");
        for (int i = 1; i < operations.length; i++) {
            System.out.println("Operacao: " + operations[i].getName());
            System.out.println("Melhor: " + contMelhor[i]);
            System.out.println("Pior: " + contPior[i]);
            System.out.println("Igual: " + contIgual[i]);
        }
    }
}
