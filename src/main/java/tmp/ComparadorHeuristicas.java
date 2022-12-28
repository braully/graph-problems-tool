/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV1;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV5Tmp2;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV5Tmp3;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV5Tmp3Bkp;
import com.github.braully.graph.operation.GraphTSSCordasco;
import com.github.braully.graph.operation.IGraphOperation;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author strike
 */
public class ComparadorHeuristicas {

    public static void main(String... args) throws FileNotFoundException, IOException {
        String strFile = "hog-graphs-ge20-le50-ordered.g6";
        UndirectedSparseGraphTO<Integer, Integer> graph = null;
        GraphHullNumberHeuristicV5Tmp3 heur5 = new GraphHullNumberHeuristicV5Tmp3();
        GraphHullNumberHeuristicV5Tmp3Bkp heur5b = new GraphHullNumberHeuristicV5Tmp3Bkp();

//        GraphHullNumberHeuristicV5Tmp2 heur5 = new GraphHullNumberHeuristicV5Tmp2();
//        GraphHullNumberHeuristicV5Tmp heur5 = new GraphHullNumberHeuristicV5Tmp();
        heur5.setVerbose(false);
        heur5b.setVerbose(false);

        GraphHullNumberHeuristicV1 heur = new GraphHullNumberHeuristicV1();
//        heur.setVerbose(false);
        heur.setVerbose(false);
        GraphTSSCordasco tss = new GraphTSSCordasco();

        int k = 2;

        IGraphOperation[] operations = new IGraphOperation[]{
                        heur,
//                        tss,
            heur5,
//            heur5b
        };

        heur.K = tss.K = heur5.K = heur5b.K = k;
        heur5.startVertice = true;
        int igual = 0;
        int melhor = 0;
        int pior = 0;
        BufferedReader files = new BufferedReader(new FileReader(strFile));
        String line = null;
        int idx = 1;
        int contdiff = 0;
        int primeiraFalha = -1;
        Integer resultarr[] = new Integer[operations.length];
        Set resultSetArr[] = new Set[operations.length];
        while (null != (line = files.readLine())) {
            int ml = -1;
            int pr = -1;
            graph = UtilGraph.loadGraphG6(line);

            for (int i = 0; i < operations.length; i++) {
                Map<String, Object> doOperation = operations[i].doOperation(graph);
                Integer result = (Integer) doOperation.get(IGraphOperation.DEFAULT_PARAM_NAME_RESULT);
                Set<Integer> resultSet = (Set<Integer>) doOperation.get(IGraphOperation.DEFAULT_PARAM_NAME_SET);
                resultarr[i] = result;
                resultSetArr[i] = resultSet;
                if (ml == -1) {
                    ml = result;
                } else if (result < ml) {
                    ml = result;
                }
                if (pr == -1) {
                    pr = result;
                } else if (result > pr) {
                    pr = result;
                }
            }
            int r1 = resultarr[0];
            int r2 = resultarr[1];
            if (r1 == r2) {
                igual++;
            } else {
                if (r1 < r2) {
                    pior++;
                    for (int i = 0; i < operations.length; i++) {
                        System.out.printf("%s ", resultSetArr[i]);
                    }
                    System.out.println();
                    System.out.println(line);
                } else {
                    melhor++;
                }
            }

            if (pr != ml) {
                if (primeiraFalha == -1) {
                    primeiraFalha = idx;
                }

                contdiff++;
                System.out.printf("%d: ", idx);
                for (int i = 0; i < operations.length; i++) {
                    System.out.printf("%d ", resultarr[i]);

                }
                System.out.println();
            }
            idx++;
        }
        if (primeiraFalha != -1) {
            System.out.println("Primeira falha em: " + primeiraFalha);
        }
        System.out.printf("Differentes %d/%d (%dpct)\n", contdiff, idx, (contdiff * 100 / idx));
        System.out.println("Resultado ");
        System.out.println("total: " + idx);
        System.out.println("Melhor: " + melhor);
        System.out.println("Pior: " + pior);
        System.out.println("Pior: " + (pior * 100 / idx) + "pct");
    }
}
