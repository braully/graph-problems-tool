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
import com.github.braully.graph.operation.GraphHullNumberHeuristicV4;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV5Tmp;
import com.github.braully.graph.operation.GraphTSSCordasco;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

        GraphHullNumberHeuristicV4 heur = new GraphHullNumberHeuristicV4();
//        GraphHullNumberHeuristicV2 heur = new GraphHullNumberHeuristicV2();
        GraphTSSCordasco tss = new GraphTSSCordasco();

//        tss.K = heur.K = 8;
        tss.K = heur.K = 2;

        for (String s : dataSets) {
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

            int tsize = 0;
            UtilProccess.printStartTime();
            Set<Integer> tssCordasco = tss.tssCordasco(graphES);
            tsize = tssCordasco.size();
            System.out.println("|tssCordasco|: " + tsize);
            UtilProccess.printEndTime();

            int hsize = 0;
            UtilProccess.printStartTime();
            Set<Integer> buildOptimizedHullSet = heur.buildOptimizedHullSet(graphES);
            hsize = buildOptimizedHullSet.size();
            System.out.println("|p3-heuristc|: " + hsize);
            UtilProccess.printEndTime();
//            UtilProccess.printCurrentItme();
            if (hsize == tsize) {
                System.out.println(" === IGUAIS === ");
            } else if (hsize < tsize) {
                System.out.println(" +++ MELHOR +++ ");
            } else {
                System.out.println(" --- PIOR ---- ");
            }
            System.out.println(" delta resultado: " + (tsize - hsize));

        }
    }
}
