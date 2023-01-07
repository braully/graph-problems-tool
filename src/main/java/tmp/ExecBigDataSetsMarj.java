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
import com.github.braully.graph.operation.GraphHullNumberHeuristicV5Tmp3Marjority;
import com.github.braully.graph.operation.GraphTSSCordasco;
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
public class ExecBigDataSetsMarj {

    public static final Map<String, int[]> resultadoArquivado = new HashMap<>();

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
            "BuzzNet",
            "Last.fm", //             "YouTube2"
        };
//        GraphHullNumberHeuristicV5Tmp heur = new GraphHullNumberHeuristicV5Tmp();

//        GraphHullNumberHeuristicV5Tmp2 heur5t2 = new GraphHullNumberHeuristicV5Tmp2();
        GraphHullNumberHeuristicV5Tmp3Marjority heur5t2 = new GraphHullNumberHeuristicV5Tmp3Marjority();

        heur5t2.setVerbose(false);
//        heur5t2.startVertice = false;

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

        String strResultFile = "resultado-" + ExecBigDataSetsMarj.class.getSimpleName() + ".txt";
        File resultFile = new File(strResultFile);
        BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile, true));

        for (int k = 6; k <= 10; k++) {
            heur5t2.marjority = tss.marjority = k;
            System.out.println("-------------\n\nK-Marjority: " + k);

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
                    String arquivadoStr = operations[i].getName() + "-k-marjo-" + k + "-" + s;
                    Map<String, Object> doOperation = null;
//                    BeanUtils.setProperty(operations[i], "K", k);
//                    PropertyUtils.setSimpleProperty(operations[i], "K", k);
                    System.out.println("*************");
                    System.out.print(" - EXEC: " + operations[i].getName() + "-k-marjo-" + k + " g:" + s + " " + graphES.getVertexCount() + " ");
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

                    String out = "Big\t" + s + "\t" + graphES.getVertexCount() + "\t"
                            + graphES.getEdgeCount()
                            + "\t" + k + "\t" + operations[i].getName()
                            + "\t" + result[i] + "\t" + totalTime[i] + "\n";

                    System.out.print("xls: " + out);

                    writer.write(out);
//                        writer.write(resultProcess);
                    writer.flush();

                    if (doOperation != null) {
                        boolean checkIfHullSet = heur5t2.checkIfHullSet(graphES, ((Set<Integer>) doOperation.get(DEFAULT_PARAM_NAME_SET)).toArray(new Integer[0]));
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
        writer.flush();
        writer.close();
    }
}
