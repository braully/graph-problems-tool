/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.generator.GraphGeneratorRandomGilbert;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author strike
 */
public class GraphRandDensityGenearte {

    public static final int INI_V = 5;
    public static final int MAX_V = 100;
    public static final int NREPETICOES = 1;

    public static void main(String... args) throws IOException {
        GraphGeneratorRandomGilbert generator = new GraphGeneratorRandomGilbert();
        UndirectedSparseGraphTO<Integer, Integer> graph = null;
        String strResultFileGraphsAll = "grafos-rand-dens" + ("all") + "-n" + INI_V + "-" + MAX_V + ".txt";
        File resultFileGraphsAll = new File(strResultFileGraphsAll);
        BufferedWriter writerGraphsAll = new BufferedWriter(new FileWriter(resultFileGraphsAll, true));

        for (double density = 0.1; density <= 0.9; density += 0.1) {
            String strResultFileGraphs = "grafos-rand-dens" + ("" + density).replace(".", "") + "-n" + INI_V + "-" + MAX_V + ".txt";
            File resultFileGraphs = new File(strResultFileGraphs);
            BufferedWriter writerGraphs = new BufferedWriter(new FileWriter(resultFileGraphs, true));
            System.out.println("Gerando grafos de densidade: " + density);
            int cont = 0;
            for (int nv = INI_V; nv <= MAX_V; nv = nv + INI_V) {
                System.out.println("  - gerando grafos de tamnho: " + nv);
                for (int i = 0; i < NREPETICOES; i++) {
                    graph = generator.generate(nv, density);
                    String strGraph = graph.getEdgeString();
                    writerGraphs.write(strGraph);
                    writerGraphs.write("\n");
                    cont++;
                    writerGraphsAll.write(strGraph);
                    writerGraphsAll.write("\n");
                }
            }
            System.out.println("Foram gerados: " + cont);
            writerGraphs.flush();
            writerGraphs.close();
        }
        writerGraphsAll.flush();
        writerGraphsAll.close();
    }
}
