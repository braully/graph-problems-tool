/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.generator.GraphGeneratorRandomGilbert;
import com.github.braully.graph.operation.GraphHullNumberOptm;
import com.github.braully.graph.operation.GraphIterationNumberOptm;
import java.util.Set;

/**
 *
 * @author strike
 */
public class DensityInterationConjecture {

    public static final int INI_V = 5;
    public static final int MAX_V = 50;

    public static void main(String... args) {
        GraphGeneratorRandomGilbert generator = new GraphGeneratorRandomGilbert();
        GraphIterationNumberOptm operacao = new GraphIterationNumberOptm();
        UndirectedSparseGraphTO<Integer, Integer> graph = null;
        Set<Integer> minHullSet = null;

        System.out.print("Dens:\t");
        for (double density = 0.1; density <= 0.9; density += 0.1) {
            System.out.printf("%.1f \t", density);

        }
        System.out.println();

        for (int nv = INI_V; nv <= MAX_V; nv++) {

            System.out.printf("%3d: \t", nv);

            for (double density = 0.1; density <= 0.9; density += 0.1) {
                graph = generator.generate(nv, density);
                minHullSet = operacao.findMinHullSetGraph(graph);
                System.out.printf("%2d\t", minHullSet.size());

            }

            System.out.println();
        }
    }
}
