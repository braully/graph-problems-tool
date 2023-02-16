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
import com.github.braully.graph.operation.GraphHullNumberHeuristicV5Tmp3Bkp;
import com.github.braully.graph.operation.GraphIterationNumberOptm;
import com.github.braully.graph.operation.GraphTSSCordasco;
import com.github.braully.graph.operation.IGraphOperation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 *
 * @author strike
 */
public class GraphRandDensityGenearte {
    
    public static final int INI_V = 10;
    public static final int MAX_V = 100;
    public static final int NREPETICOES = 10;
    
    public static void main(String... args) throws IOException {
        GraphGeneratorRandomGilbert generator = new GraphGeneratorRandomGilbert();
        UndirectedSparseGraphTO<Integer, Integer> graph = null;
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
                }
            }
            System.out.println("Foram gerados: " + cont);
            writerGraphs.flush();
            writerGraphs.close();
        }
    }
}
