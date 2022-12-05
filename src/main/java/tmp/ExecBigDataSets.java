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
import com.github.braully.graph.operation.GraphHullNumberHeuristicV2;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV3;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV4;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV5;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV5Tmp;
import com.github.braully.graph.operation.GraphTSSCordasco;
import com.github.braully.graph.operation.IGraphOperation;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import util.UtilProccess;

/**
 *
 * @author strike
 */
public class ExecBigDataSets {

    public static void main(String... args) throws FileNotFoundException, IOException {
        String[] dataSets = new String[]{
            "ca-GrQc", "ca-HepTh",
            "ca-CondMat", "ca-HepPh",
            "BlogCatalog3",
            "ca-AstroPh",
            "Douban",
            "Delicious",
            "BlogCatalog2",
            "Livemocha",
            "BlogCatalog",
            "BuzzNet",
            "YouTube2",
            "Last.fm"
        };
//        GraphHullNumberHeuristicV5Tmp heur = new GraphHullNumberHeuristicV5Tmp();

        GraphHullNumberHeuristicV4 heur4 = new GraphHullNumberHeuristicV4();
        heur4.setVerbose(false);
        GraphHullNumberHeuristicV3 heur3 = new GraphHullNumberHeuristicV3();
        heur3.setVerbose(false);
        GraphHullNumberHeuristicV2 heur2 = new GraphHullNumberHeuristicV2();
        heur2.setVerbose(false);
        GraphHullNumberHeuristicV5 heur5 = new GraphHullNumberHeuristicV5();
        heur5.setVerbose(false);
        GraphHullNumberHeuristicV5Tmp heur5t = new GraphHullNumberHeuristicV5Tmp();
        heur5t.setVerbose(false);

        GraphTSSCordasco tss = new GraphTSSCordasco();

        IGraphOperation[] operations = new IGraphOperation[]{
            tss,
            //            heur2, 
            heur3, heur4,
            heur5,
            heur5t
        };
        long totalTime[] = new long[operations.length];
        Integer[] result = new Integer[operations.length];
        Integer[] delta = new Integer[operations.length];

        for (int k = 1; k <= 10; k++) {

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
                    System.out.println("*************");
                    System.out.print(" - EXEC: " + operations[i].getName() + " k: " + k + " g:" + s);
                    System.out.print("  - ");
                    UtilProccess.printStartTime();
                    Map<String, Object> doOperation = operations[i].doOperation(graphES);
                    System.out.print("  - ");
                    totalTime[i] += UtilProccess.printEndTime();
                    result[i] = (Integer) doOperation.get(IGraphOperation.DEFAULT_PARAM_NAME_RESULT);
                    System.out.print(" - Result: " + result[i]);
                    if (i == 0) {
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