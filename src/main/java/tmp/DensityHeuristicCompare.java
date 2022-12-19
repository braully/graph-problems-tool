/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.generator.GraphGeneratorRandomGilbert;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV1;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV5Tmp;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV5Tmp2;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV5Tmp3;
import com.github.braully.graph.operation.GraphIterationNumberOptm;
import com.github.braully.graph.operation.GraphTSSCordasco;
import com.github.braully.graph.operation.IGraphOperation;
import java.util.Map;

/**
 *
 * @author strike
 */
public class DensityHeuristicCompare {

    public static final int INI_V = 1;
    public static final int MAX_V = 50;

    public static void main(String... args) {
        GraphGeneratorRandomGilbert generator = new GraphGeneratorRandomGilbert();
        GraphIterationNumberOptm operacao = new GraphIterationNumberOptm();
        UndirectedSparseGraphTO<Integer, Integer> graph = null;

        GraphHullNumberHeuristicV5Tmp3 heur5 = new GraphHullNumberHeuristicV5Tmp3();
//        GraphHullNumberHeuristicV5Tmp2 heur5 = new GraphHullNumberHeuristicV5Tmp2();
//        GraphHullNumberHeuristicV5Tmp heur5 = new GraphHullNumberHeuristicV5Tmp();
        heur5.setVerbose(false);
        GraphHullNumberHeuristicV1 heur = new GraphHullNumberHeuristicV1();
//        heur.setVerbose(false);
        heur.setVerbose(true);
        GraphTSSCordasco tss = new GraphTSSCordasco();

        IGraphOperation[] operations = new IGraphOperation[]{
            tss,
            //            heur, 
            heur5,
        };

        heur.setVerbose(false);

        int contMelhor[] = new int[operations.length];
        int contIgual[] = new int[operations.length];
        int contPior[] = new int[operations.length];
        int[] cont = new int[9];
        int idx = 0;
        System.out.print("Dens:\t");
        for (double density = 0.1; density <= 0.9; density += 0.1) {
            System.out.printf("%.1f \t", density);
            cont[idx++] = 0;

        }
        for (int i = 0; i < operations.length; i++) {
            contPior[i] = contIgual[i] = contMelhor[i] = 0;
            System.out.printf("T(%s) \t", operations[i].getName());
        }
        for (int i = 1; i < operations.length; i++) {
            System.out.printf("Delta(%s) \t", operations[i].getName());
        }
        System.out.println();

        long totalTime[] = new long[operations.length];
        Integer[] result = new Integer[operations.length];
        Integer[] delta = new Integer[operations.length];

        for (int i = 0; i < operations.length; i++) {
            totalTime[i] = 0;
        }

        int ngraphs = 10;
        int windows = 10;

        for (int nv = INI_V; nv <= MAX_V; nv++) {
            tss.K = heur.K = heur5.K = 10;

            System.out.printf("%3d: \t", nv, (nv + windows));

            for (double density = 0.1; density <= 0.9; density += 0.1) {
                graph = generator.generate(nv, density);
                int mindelta = Integer.MAX_VALUE;
                int maxdelta = Integer.MIN_VALUE;
                for (int i = 0; i < operations.length; i++) {
                    long currentTimeMillis = System.currentTimeMillis();
//                    System.out.println("Do: " + operations[i].getName());
                    Map<String, Object> doOperation = operations[i].doOperation(graph);
                    currentTimeMillis = System.currentTimeMillis() - currentTimeMillis;
//                    currentTimeMillis = currentTimeMillis / 1000;
                    totalTime[i] += currentTimeMillis;
                    result[i] = (Integer) doOperation.get(IGraphOperation.DEFAULT_PARAM_NAME_RESULT);
                    cont[(int) (density * 10) - 1]++;
                    if (i == 0) {
//                        System.out.printf("%d", result[i]);
                    } else {
                        if (result[i].equals(result[0])) {
                            contIgual[i]++;
//                            System.out.print("'");
                        } else if (result[i] < result[0]) {
//                            System.out.print("-");
                            contMelhor[i]++;
                        } else {
//                            System.out.print("+");
                            contPior[i]++;
                        }
                    }
                    if (i > 0) {
                        delta[i] = result[0] - result[i];
                        if (delta[i] > maxdelta) {
                            maxdelta = delta[i];
                        }
                        if (delta[i] < mindelta) {
                            mindelta = delta[i];
                        }
                        System.out.printf("%d", delta[i]);
                    }
                }

//                System.out.printf("[%d,%d]", mindelta, maxdelta);
                System.out.printf("\t");

            }

            for (int i = 0; i < operations.length; i++) {
                System.out.printf("%d \t", totalTime[i]);
            }
            for (int i = 1; i < operations.length; i++) {
                System.out.printf("%d \t", delta[i]);
            }
            System.out.println();
        }

        System.out.println();
        for (int i = 0; i < operations.length; i++) {
            System.out.printf("%s: \n - igual: %d melhor: %d pior: %d tempo: %d \n",
                    operations[i].getName(), contIgual[i], contMelhor[i],
                    contPior[i], totalTime[i]);
        }
    }
}
