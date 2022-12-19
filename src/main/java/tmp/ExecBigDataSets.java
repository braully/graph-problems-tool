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
import com.github.braully.graph.operation.GraphHullNumberHeuristicV1;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV2;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV3;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV4;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV5;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV5Tmp;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV5Tmp2;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV5Tmp3;
import com.github.braully.graph.operation.GraphTSSCordasco;
import com.github.braully.graph.operation.IGraphOperation;
import static com.github.braully.graph.operation.IGraphOperation.DEFAULT_PARAM_NAME_SET;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
public class ExecBigDataSets {

    public static final Map<String, int[]> resultadoArquivado = new HashMap<>();

    static {
        resultadoArquivado.put("TSS-Cordasco-k1-BlogCatalog", new int[]{1, 93977});
//        k=2
        resultadoArquivado.put("TSS-Cordasco-k2-BlogCatalog", new int[]{20290, 8869});
        resultadoArquivado.put("TSS-Cordasco-k2-BlogCatalog2", new int[]{27638, 25148});
        resultadoArquivado.put("TSS-Cordasco-k2-BlogCatalog3", new int[]{270, 25597});
        resultadoArquivado.put("TSS-Cordasco-k2-BuzzNet", new int[]{7561, 29806});
        resultadoArquivado.put("TSS-Cordasco-k2-Delicious", new int[]{76681, 136437});
        resultadoArquivado.put("TSS-Cordasco-k2-Douban", new int[]{103158, 399065});
        resultadoArquivado.put("TSS-Cordasco-k2-Livemocha", new int[]{7182, 403179});
        resultadoArquivado.put("TSS-Cordasco-k2-ca-AstroPh", new int[]{1859, 403469});
        resultadoArquivado.put("TSS-Cordasco-k2-ca-CondMat", new int[]{3554, 403742});
        resultadoArquivado.put("TSS-Cordasco-k2-ca-GrQc", new int[]{1665, 403772});
        resultadoArquivado.put("TSS-Cordasco-k2-ca-HepPh", new int[]{2003, 403921});
        resultadoArquivado.put("TSS-Cordasco-k2-ca-HepTh", new int[]{2724, 404013});
//        k=3
        resultadoArquivado.put("TSS-Cordasco-k3-BlogCatalog", new int[]{30769, 429021});
        resultadoArquivado.put("TSS-Cordasco-k3-BlogCatalog3", new int[]{643, 483882});
        resultadoArquivado.put("TSS-Cordasco-k3-BlogCatalog2", new int[]{40662, 483277});
        resultadoArquivado.put("TSS-Cordasco-k3-BuzzNet", new int[]{19630, 490866});
        resultadoArquivado.put("TSS-Cordasco-k3-Delicious", new int[]{84478, 667665});
        resultadoArquivado.put("TSS-Cordasco-k3-Douban", new int[]{125197, 1128142});
        resultadoArquivado.put("TSS-Cordasco-k3-Livemocha", new int[]{13603, 1136458});
        resultadoArquivado.put("TSS-Cordasco-k3-ca-AstroPh", new int[]{3624, 1136878});
        resultadoArquivado.put("TSS-Cordasco-k3-ca-CondMat", new int[]{6970, 1137443});
        resultadoArquivado.put("TSS-Cordasco-k3-ca-GrQc", new int[]{2679, 1137502});
        resultadoArquivado.put("TSS-Cordasco-k3-ca-HepPh", new int[]{3799, 1137702});
        resultadoArquivado.put("TSS-Cordasco-k3-ca-HepTh", new int[]{4614, 1137855});
//        k=4
        resultadoArquivado.put("TSS-Cordasco-k4-BlogCatalog", new int[]{37616, 1183203});
        resultadoArquivado.put("TSS-Cordasco-k4-Delicious", new int[]{88704, 1427150});
        resultadoArquivado.put("TSS-Cordasco-k4-BuzzNet", new int[]{23647, 1258586});
        resultadoArquivado.put("TSS-Cordasco-k4-BlogCatalog3", new int[]{995, 1252150});
        resultadoArquivado.put("TSS-Cordasco-k4-BlogCatalog2", new int[]{48604, 1251645});
        resultadoArquivado.put("TSS-Cordasco-k4-Douban", new int[]{133846, 1819357});
        resultadoArquivado.put("TSS-Cordasco-k4-ca-HepTh", new int[]{5837, 1825771});
        resultadoArquivado.put("TSS-Cordasco-k4-ca-HepPh", new int[]{5143, 1825560});
        resultadoArquivado.put("TSS-Cordasco-k4-ca-GrQc", new int[]{3332, 1825314});
        resultadoArquivado.put("TSS-Cordasco-k4-ca-CondMat", new int[]{9817, 1825251});
        resultadoArquivado.put("TSS-Cordasco-k4-ca-AstroPh", new int[]{5131, 1824618});
        resultadoArquivado.put("TSS-Cordasco-k4-Livemocha", new int[]{19345, 1824287});
        resultadoArquivado.put("TSS-Cordasco-k5-BlogCatalog", new int[]{42440, 1864074});
        resultadoArquivado.put("TSS-Cordasco-k5-BlogCatalog2", new int[]{54171, 1887778});
        resultadoArquivado.put("TSS-Cordasco-k5-BlogCatalog3", new int[]{1369, 1888279});
        resultadoArquivado.put("TSS-Cordasco-k5-BuzzNet", new int[]{27242, 1894889});
        resultadoArquivado.put("TSS-Cordasco-k5-Delicious", new int[]{91423, 1948439});
        resultadoArquivado.put("TSS-Cordasco-k5-Douban", new int[]{138187, 2353261});
        resultadoArquivado.put("TSS-Cordasco-k5-Livemocha", new int[]{24466, 2362462});
        resultadoArquivado.put("TSS-Cordasco-k5-ca-AstroPh", new int[]{6302, 2362884});
        resultadoArquivado.put("TSS-Cordasco-k5-ca-CondMat", new int[]{12008, 2363805});
        resultadoArquivado.put("TSS-Cordasco-k5-ca-GrQc", new int[]{3744, 2363887});
        resultadoArquivado.put("TSS-Cordasco-k5-ca-HepPh", new int[]{6031, 2364200});
        resultadoArquivado.put("TSS-Cordasco-k5-ca-HepTh", new int[]{6636, 2364473});
        resultadoArquivado.put("TSS-Cordasco-k6-BlogCatalog", new int[]{46054, 2413544});
        resultadoArquivado.put("TSS-Cordasco-k6-BlogCatalog2", new int[]{58325, 2492578});
        resultadoArquivado.put("TSS-Cordasco-k6-BlogCatalog3", new int[]{1736, 2493113});
        resultadoArquivado.put("TSS-Cordasco-k6-BuzzNet", new int[]{30850, 2500321});
        resultadoArquivado.put("TSS-Cordasco-k6-Delicious", new int[]{93376, 2657732});
        resultadoArquivado.put("TSS-Cordasco-k6-Douban", new int[]{140694, 2949447});
        resultadoArquivado.put("TSS-Cordasco-k6-Livemocha", new int[]{29079, 2959369});
    }

    public static void main(String... args) throws FileNotFoundException, IOException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String[] dataSets = new String[]{
            "ca-GrQc", "ca-HepTh",
            "ca-CondMat", "ca-HepPh",
            "ca-AstroPh",
            "Douban",
            "Delicious",
            "BlogCatalog3",
            "BlogCatalog2",
            "Livemocha",
            "BlogCatalog",
            "BuzzNet", //            "Last.fm"
        //            ,"YouTube2"
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

        heur5t2.setVerbose(false);

        GraphTSSCordasco tss = new GraphTSSCordasco();

        IGraphOperation[] operations = new IGraphOperation[]{
            tss, //            heur1,
            //            heur2, 
            //            heur3, heur4,
            //            heur5,
            //            heur5t,
            heur5t2
        };
        long totalTime[] = new long[operations.length];
        Integer[] result = new Integer[operations.length];
        Integer[] delta = new Integer[operations.length];

        Arrays.sort(dataSets);

        for (int k = 2; k <= 10; k++) {
            heur1.K = heur2.K = heur3.K
                    = heur4.K = heur5.K = heur5t.K = heur5t2.K = tss.K = k;
            System.out.println("-------------\n\nK: " + k);

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
                    String arquivadoStr = operations[i].getName() + "-k" + k + "-" + s;
                    Map<String, Object> doOperation = null;
//                    BeanUtils.setProperty(operations[i], "K", k);
//                    PropertyUtils.setSimpleProperty(operations[i], "K", k);
                    System.out.println("*************");
                    System.out.print(" - EXEC: " + operations[i].getName() + "-k: " + k + " g:" + s + " " + graphES.getVertexCount() + " ");
                    int[] get = resultadoArquivado.get(arquivadoStr);
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

                    System.out.println("xls: " + operations[i].getName()
                            + "\t" + k + "\t" + s + "\t" + graphES.getVertexCount()
                            + "\t" + result[i] + "\t" + totalTime[i]);

                    if (doOperation != null) {
                        boolean checkIfHullSet = heur1.checkIfHullSet(graphES, ((Set<Integer>) doOperation.get(DEFAULT_PARAM_NAME_SET)).toArray(new Integer[0]));
                        if (!checkIfHullSet) {
                            System.out.println("ALERT: ----- RESULTADO ANTERIOR IS NOT HULL SET");
                            System.err.println("ALERT: ----- RESULTADO ANTERIOR IS NOT HULL SET");
//                            throw new IllegalStateException("CORDASSO IS NOT HULL SET");
                        }
                    }
                    if (i == 0 && get == null) {
                        delta[i] = 0;

                    } else {
                        delta[i] = result[0] - result[i];

                        long deltaTempo = totalTime[0] - totalTime[i];
                        System.out.print(" - Tempo: ");

                        if (deltaTempo >= 0) {
                            System.out.println(" +RAPIDO " + deltaTempo);
                        } else {
                            System.out.println(" +LENTO " + deltaTempo);
                        }
                        System.out.print(" - Delta: " + delta[i] + " ");
                        if (delta[i] == 0) {
                            System.out.println(" = igual");
                        } else if (delta[i] > 0) {
                            System.out.println(" + MELHOR ");
                        } else {
                            System.out.println(" - PIOR ");
                        }
                        System.out.println(delta[i]);
                    }
                    System.out.println();
                }
            }
        }
    }
}
