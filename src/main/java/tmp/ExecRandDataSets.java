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
import static com.github.braully.graph.operation.AbstractHeuristicOptm.pbonusParcial;
import static com.github.braully.graph.operation.AbstractHeuristicOptm.pdeltaHsi;
import static com.github.braully.graph.operation.AbstractHeuristicOptm.pdeltaHsixdificuldadeTotal;
import com.github.braully.graph.operation.GraphHNV;
import com.github.braully.graph.operation.GraphHNVOptm;
import com.github.braully.graph.operation.GraphHNVOptmPoda;
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import util.UtilProccess;
import static util.UtilProccess.printTimeFormated;

/**
 *
 * @author strike
 */
public class ExecRandDataSets {

    public static final Map<String, int[]> resultadoArquivado = new HashMap<>();
    private static boolean verbose;

    public static void main(String... args) throws FileNotFoundException, IOException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String[] dataSets = new String[]{
            "grafos-rand-dens01-n10-100.txt",
            "grafos-rand-dens02-n10-100.txt",
            "grafos-rand-dens03-n10-100.txt",
            "grafos-rand-dens04-n10-100.txt",
            "grafos-rand-dens05-n10-100.txt",
            "grafos-rand-dens06-n10-100.txt",
            "grafos-rand-dens07-n10-100.txt",
            "grafos-rand-dens08-n10-100.txt",
            "grafos-rand-dens09-n10-100.txt"
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

        GraphHNVOptmPoda optmpoda = new GraphHNVOptmPoda();

//        optmpoda.setVerbose(false);
//        optmpoda.resetParameters();
//        optmpoda.setPularAvaliacaoOffset(true);
//        optmpoda.setTryMinimal();
//        optmpoda.setRealizarPoda(true);
//        optmpoda.setParameter(pdificuldadeTotal, true);
////        optm.setParameter(GraphBigHNVOptm.pdificuldadeTotal, true);
//        optmpoda.setParameter(pbonusParcialNormalizado, true);
////        optmpoda.setParameter(GraphHNVOptm.pprofundidadeS, false);
        heur5t2.setVerbose(false);
        heur5t2.startVertice = false;

        GraphHNVOptm optm = new GraphHNVOptm();

        optm.resetParameters();
        optm.setVerbose(false);
        optm.setPularAvaliacaoOffset(true);
        optm.setSortByDegree(true);
//        optm.setTryMinimal(false);
//        optm.setRealizarPoda(true);
        optm.setTryMinimal();
        optm.setTryMinimal2();
//        optm.setParameter(GraphBigHNVOptm.pdeltaHsi, true);
//        optm.setParameter(pdeltaHsi, true);
        if (true) {
            optm.setParameter(pdeltaHsixdificuldadeTotal, true);
//            optm.setParameter(pdeltaHsi, true);
            optm.setParameter(pbonusParcial, true);
//            optm.setParameter(pdeltaHsixdificuldadeTotal, true);
//            optm.setParameter(pbonusParcial, true);
//            optm.setSortByDegree(true);
        } else {
//        optm.setParameter(AbstractHeuristicOptm.paux, true);
            optm.setParameter(pdeltaHsi, true);
            optm.setParameter(pbonusParcial, true);
        }
//        optm.setParameter(AbstractHeuristicOptm.paux, true);

//        optm.setParameter(AbstractHeuristicOptm.pdificuldadeTotal, true);
//        optm.setParameter(AbstractHeuristicOptm.pbonusParcialNormalizado, true);
//        optm.setParameter(GraphBigHNVOptm.pdificuldadeTotal, true);
//        optm.setParameter(AbstractHeuristicOptm.pprofundidadeS, false);
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
            //                        optm,
            tss, //            heur1,
            //            heur2, 
            //            heur3, heur4,
            //            heur5,
            //            heur5t,
            //            tssg,
            //            heur5t2,
            hnv2
//            optm
//            optmpoda
        };
        long totalTime[] = new long[operations.length];
        Integer[] result = new Integer[operations.length];
        Integer[] delta = new Integer[operations.length];
        int contMelhorGlobal = 0, contPiorGlobal = 0, contIgualGlobal = 0;
        int[] contMelhor = new int[operations.length];
        int[] contPior = new int[operations.length];
        int[] contIgual = new int[operations.length];
        for (int i = 0; i < operations.length; i++) {
            contMelhor[i] = contPior[i] = contIgual[i] = 0;
        }

        Arrays.sort(dataSets);

        String strResultFile = "resultado-" + ExecRandDataSets.class.getSimpleName() + ".txt";
        File resultFile = new File(strResultFile);
        BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile, true));

        Map<String, Boolean> piorou = new HashMap<>();

        for (int k = 2; k <= 10; k++) {
            if (false) {
                optm.setK(k);
                tss.setK(k);
                optmpoda.setK(k);
                heur5t2.setK(k);
                hnv2.setK(k);
            } else {
                optm.setR(k);
                tss.setR(k);
                optmpoda.setR(k);
                heur5t2.setR(k);
                hnv2.setR(k);
            }
            System.out.println("-------------\n\nR: " + k);

            for (String s : dataSets) {
//                if (verbose) {
                    System.out.println("\n-DATASET: " + s);
//                }
//                /home/strike/Workspace/graph-problems-tool/database/rand10/
                UndirectedSparseGraphTO<Integer, Integer> graphES = null;
                String strFile = "/home/strike/Workspace/graph-problems-tool/database/rand10/" + s;
                BufferedReader files = new BufferedReader(new FileReader(strFile));
                String line = null;
                int cont = 0;

                while (null != (line = files.readLine())) {
                    String id = s + "-" + cont;
                    graphES = UtilGraph.loadGraphES(line);
                    graphES.setName(id);
                    cont++;
                    if (verbose) {
                        System.out.println("Loaded Graph: " + s + " " + graphES.getVertexCount() + " " + graphES.getEdgeCount());
                    }

                    for (int i = 0; i < operations.length; i++) {
                        String arquivadoStr = operations[i].getName() + "-k" + k + "-" + s;
                        Map<String, Object> doOperation = null;
//                    BeanUtils.setProperty(operations[i], "K", k);
//                    PropertyUtils.setSimpleProperty(operations[i], "K", k);
                        if (verbose) {
                            System.out.println("*************");
                            System.out.print(" - EXEC: " + operations[i].getName() + "-k: " + k + " g:" + s + " " + graphES.getVertexCount() + " ");
                        }
                        int[] get = resultadoArquivado.get(arquivadoStr);
                        if (get != null) {
                            result[i] = get[0];
                            totalTime[i] = get[1];
                        } else {
                            UtilProccess.printStartTime();
                            doOperation = operations[i].doOperation(graphES);
                            result[i] = (Integer) doOperation.get(IGraphOperation.DEFAULT_PARAM_NAME_RESULT);
                            if (verbose) {
                                totalTime[i] += UtilProccess.printEndTime();
                            } else {
                                totalTime[i] += UtilProccess.endTime();
                            }
                            if (verbose) {
                                System.out.println(" - arquivar: resultadoArquivado.put(\"" + arquivadoStr + "\", new int[]{" + result[i] + ", " + totalTime[i] + "});");
                            }
                        }
                        if (verbose) {
                            System.out.println(" - Result: " + result[i]);
                        }

                        String out = "Big\t" + s + "\t" + graphES.getVertexCount() + "\t"
                                + graphES.getEdgeCount()
                                + "\t" + k + "\t" + operations[i].getName()
                                + "\t" + result[i] + "\t" + totalTime[i] + "\n";

                        if (verbose) {
                            System.out.print("xls: " + out);
                        }

                        writer.write(out);
//                        writer.write(resultProcess);
                        writer.flush();

                        if (doOperation != null) {
                            boolean checkIfHullSet = operations[i].checkIfHullSet(graphES, ((Set<Integer>) doOperation.get(DEFAULT_PARAM_NAME_SET)));
                            if (!checkIfHullSet) {
                                System.out.println("ALERT: ----- RESULTADO ANTERIOR IS NOT HULL SET");
                                System.err.println("ALERT: ----- RESULTADO ANTERIOR IS NOT HULL SET");
                                System.out.println(graphES.getEdgeString());
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
                            if (verbose) {
                                System.out.print(" - Tempo: ");
                            }
                            if (verbose) {
                                if (deltaTempo >= 0) {
                                    System.out.println(" +RAPIDO " + deltaTempo);
                                } else {
                                    System.out.println(" +LENTO " + deltaTempo);
                                }
                                System.out.print(" - Delta: " + delta[i] + " ");
                            }
                            if (delta[i] == 0) {
                                if (verbose) {
                                    System.out.println(" = igual");
                                }
                                contIgual[i]++;
                                contIgualGlobal++;
                            } else if (delta[i] > 0) {
                                if (verbose) {
                                    System.out.println(" + MELHOR ");
                                }
                                contMelhor[i]++;
                                contMelhorGlobal++;
                                if (k == 2) {
                                    piorou.put(id, true);
                                }
                            } else {
                                if (verbose) {
                                    System.out.println(" - PIOR ");
                                }
                                contPior[i]++;
                                contPiorGlobal++;
                                Boolean get1 = piorou.get(id);
                                if (get1 != null && get1) {
//                                    System.out.println("grafo piorou: " + id + " em k: " + k);
//                                    System.out.println(graphES.getEdgeString());
                                    piorou.remove(id);
                                }
                            }
                            if (verbose) {
                                System.out.println(delta[i]);
                            }
                        }
                        if (verbose) {
                            System.out.println();
                        }
                    }
                }

            }
            if (true) {
                System.out.println("Resumo parcial: " + k);
                for (int i = 1; i < operations.length; i++) {
                    int total = contMelhor[i] + contPior[i] + contIgual[i];

                    System.out.println("Operacao: " + operations[i].getName());
                    System.out.println("Melhor: " + contMelhor[i]);
                    System.out.println("Pior: " + contPior[i]);
                    System.out.println("Igual: " + contIgual[i]);
                    System.out.println("Total: " + total);
                    System.out.println("------------");
                    System.out.println("Melhor: " + (contMelhor[i] * 100 / total) + "pct");
                    System.out.println("Igual: " + ((total - (contMelhor[i] + contPior[i]))
                            * 100 / total) + "pct"
                    );
                    System.out.println("Pior: " + (contPior[i] * 100 / total) + "pct");
                }
                for (int i = 0; i < operations.length; i++) {
                    contMelhor[i] = contPior[i] = contIgual[i] = 0;
                }
            }
        }
        writer.flush();
        writer.close();
        System.out.println("\n\nResumo Global");
        for (int i = 1; i < operations.length; i++) {
            System.out.println("Operacao: " + operations[i].getName());
            System.out.println("Melhor: " + contMelhorGlobal);
            System.out.println("Pior: " + contPiorGlobal);
            System.out.println("Igual: " + contIgualGlobal);

            System.out.println("------------");
            int total = contMelhorGlobal + contPiorGlobal + contIgualGlobal;
            if (total > 0) {
                System.out.println("Melhor: " + (contMelhorGlobal * 100 / total) + "pct");
                System.out.println("Igual: " + ((total - (contMelhorGlobal + contPiorGlobal))
                        * 100 / total) + "pct"
                );
                System.out.println("Pior: " + (contPiorGlobal * 100 / total) + "pct");
            }
        }
        for (int i = 0; i < operations.length; i++) {
            System.out.println(operations[i].getName());
            System.out.print("time: ");
            printTimeFormated(totalTime[i]);
//            System.out.println();

        }
    }
}
