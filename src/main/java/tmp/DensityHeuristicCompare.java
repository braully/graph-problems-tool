/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.generator.GraphGeneratorRandomGilbert;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV1;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV5Tmp3;
import com.github.braully.graph.operation.GraphIterationNumberOptm;
import com.github.braully.graph.operation.GraphTSSCordasco;
import com.github.braully.graph.operation.IGraphOperation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author strike
 */
public class DensityHeuristicCompare {

    public static final int INI_V = 1;
    public static final int MAX_V = 100;

    public static void main(String... args) throws IOException {
        GraphGeneratorRandomGilbert generator = new GraphGeneratorRandomGilbert();
        GraphIterationNumberOptm operacao = new GraphIterationNumberOptm();
        UndirectedSparseGraphTO<Integer, Integer> graph = null;
        GraphHullNumberHeuristicV5Tmp3 heur5 = new GraphHullNumberHeuristicV5Tmp3();
//        GraphHullNumberHeuristicV5Tmp3 heur5 = new GraphHullNumberHeuristicV5Tmp3();
//        GraphHullNumberHeuristicV5Tmp2 heur5 = new GraphHullNumberHeuristicV5Tmp2();
//        GraphHullNumberHeuristicV5Tmp heur5 = new GraphHullNumberHeuristicV5Tmp();
        heur5.setVerbose(false);
        GraphHullNumberHeuristicV1 heur = new GraphHullNumberHeuristicV1();
//        heur.setVerbose(false);
        heur.setVerbose(false);
        GraphTSSCordasco tss = new GraphTSSCordasco();

        String strResultFile = "resultado-" + DensityHeuristicCompare.class.getSimpleName() + ".txt";
        File resultFile = new File(strResultFile);
        BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile, true));

        String strResultFileGraphs = "grafos-rand-" + DensityHeuristicCompare.class.getSimpleName() + ".txt";
        File resultFileGraphs = new File(strResultFileGraphs);
        BufferedWriter writerGraphs = new BufferedWriter(new FileWriter(resultFileGraphs, true));

        IGraphOperation[] operations = new IGraphOperation[]{
            //            heur,
            tss,
            heur5,};

        heur.setVerbose(false);

        for (int k = 2; k <= 10; k++) {

            int contMelhor[] = new int[operations.length];
            int contIgual[] = new int[operations.length];
            int contPior[] = new int[operations.length];
            int[] cont = new int[9];
            int idx = 0;
            System.out.println("----------- -----------------");

            System.out.println("K: " + k);
            System.out.print("K\tDens:\t");
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
                tss.setR(k);
                heur5.setR(k);

                System.out.printf("%3d %3d \t", k, nv);

                for (double density = 0.1; density <= 0.9; density += 0.1) {
                    graph = generator.generate(nv, density);
                    String strGraph = graph.getName() + "\t" + k + "\t" + graph.getEdgeString() + "\n";
                    writerGraphs.write(strGraph);
                    writerGraphs.flush();

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
                        String out = "Big\t" + graph.getName() + "\t" + graph.getVertexCount() + "\t"
                                + graph.getEdgeCount()
                                + "\t" + k + "\t" + operations[i].getName()
                                + "\t" + result[i] + "\t" + totalTime[i] + "\n";

                        System.out.print("xls: " + out);

                        writer.write(out);
//                        writer.write(resultProcess);
                        writer.flush();
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
        writerGraphs.flush();
        writerGraphs.close();
    }
}
