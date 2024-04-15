/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV1;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV5;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV5Tmp;
import com.github.braully.graph.operation.GraphTSSCordasco;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 *
 * @author strike
 */
public class DebuHeurist {

    public static void main(String... args) throws IOException {
//        String g6graph = "S?U_?COGC?gCGOC_W??G?A???owCS_AIS";
        String g6graph = "T?H?_AP_`GG??@?S?W?O???k?@??WgwCJ_?K";
        UndirectedSparseGraphTO graph = UtilGraph.loadGraphG6(g6graph);
        System.out.println(graph);
        GraphHullNumberHeuristicV5Tmp heur5t = new GraphHullNumberHeuristicV5Tmp();
        heur5t.setVerbose(true);
        GraphHullNumberHeuristicV5 heur5 = new GraphHullNumberHeuristicV5();
        heur5.setVerbose(true);
        GraphHullNumberHeuristicV1 heur = new GraphHullNumberHeuristicV1();
        heur.setVerbose(true);
        GraphTSSCordasco tss = new GraphTSSCordasco();

        Collection<Integer> vertices = graph.getVertices();

        for (Integer i : vertices) {
            if (i.equals(18) || i.equals(7)) {
                continue;
            }
            for (Integer j : vertices) {
                if (j.equals(18)
                        || j.equals(7)
                        || i.equals(j)) {
                    continue;
                }
                if (heur.checkIfHullSet(graph, 18, 7, i, j)) {
                    System.out.println("é hull set: " + i + " " + j);
                }
            }
        }

        Set min = heur5.findMinHullSetGraph(graph);
        System.out.println(heur5.getName() + ": [" + min.size() + "]: " + min);

        min = heur5t.findMinHullSetGraph(graph);
        System.out.println(heur5t.getName() + ": [" + min.size() + "]: " + min);

        min = heur.findMinHullSetGraph(graph);
        System.out.println(heur.getName() + ": [" + min.size() + "]: " + min);

        min = tss.tssCordasco(graph);
        System.out.println(tss.getName() + ": [" + min.size() + "]: " + min);

    }
}
