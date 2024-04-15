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
import com.github.braully.graph.generator.GraphGeneratorCycle;
import com.github.braully.graph.generator.GraphGeneratorPath;
import com.github.braully.graph.operation.GraphHullNumberOptm;
import java.util.Set;

/**
 *
 * @author strike
 */
public class PathConjecture {

    static GraphHullNumberOptm operacao = new GraphHullNumberOptm();

    public static void main(String... args) {
        GraphGeneratorCycle gerador = new GraphGeneratorCycle();
        GraphGeneratorPath geradorCaminho = new GraphGeneratorPath();
//        GraphCaratheodoryNumberBinary operacao = new GraphCaratheodoryNumberBinary();
        int arestasAdd = 1;
        operacao.setVerbose(false);
        UndirectedSparseGraphTO<Integer, Integer> graph1 = null;
        UndirectedSparseGraphTO<Integer, Integer> graph2 = null;

        for (int n = 5; n < 20; n++) {
            System.out.println("\n\n---------------------------");
            graph1 = gerador.generateCycleGraph(n);
            graph2 = geradorCaminho.generatePathGraph(n);
            System.out.println(graph1.getName());
            System.out.println("V: " + graph1.getVertices());
            Set<Integer> hullSet = operacao.findMinHullSetGraph(graph1);
//            System.out.println(graph.getName() + ": " + hullSet.size());
            System.out.println("h: " + hullSet.size());

            System.out.println(graph2.getName());
            System.out.println("V: " + graph2.getVertices());
            hullSet = operacao.findMinHullSetGraph(graph2);
//            System.out.println(graph.getName() + ": " + hullSet.size());
            System.out.println("h: " + hullSet.size());

//            int hn = hullSet.size();
////            System.out.println(hullSet);
//            for (int i = 0; i < n; i++) {
//                for (int j = i + 2; j < n; j++) {
//                    if (graph.isNeighbor(i, j)) {
//                        continue;
//                    }
//                    UndirectedSparseGraphTO clone = graph.clone();
//                    clone.addEdge(i, j);
//                    clone.setName("C" + n + "+" + i + "," + j);
//                    hullSet = operacao.findMinHullSetGraph(clone);
//                    if (hn == hullSet.size()) {
//                        System.out.println("Não melhorado");
//                        System.out.println(" -" + clone.getName() + ": " + hullSet.size());
////                        System.out.println(" -" + hullSet);
//                        tentarMelhorar(clone, n, hn);
//                    } else {
//                        System.out.println("Melhorado: " + clone.getName() + " em " + (hn - hullSet.size()));
//                    }
//
//                }
//            }
        }
    }

    static void tentarMelhorar(UndirectedSparseGraphTO graph, int n, int hn) {

        boolean melhorado = true;
        int best = 0;
        int worst = -1;
        for (int i = 0; i < n; i++) {
            for (int j = i + 2; j < n; j++) {
                if (graph.isNeighbor(i, j)) {
                    continue;
                }
                UndirectedSparseGraphTO clone = graph.clone();
                clone.addEdge(i, j);
                clone.setName(graph.getName() + "+" + i + "," + j);
                Set<Integer> hullSet = operacao.findMinHullSetGraph(clone);
                if (hn == hullSet.size()) {
                    melhorado = false;
                    System.out.println("     -NÃO MELHORADOOOOO");
                    System.out.println("     -" + clone.getName() + ": " + hullSet.size());
                    System.out.println("     -" + hullSet);
                } else {
                    int melhora = hn - hullSet.size();
                    if (melhora > best) {
                        best = melhora;
                    }
                    if (worst == -1) {
                        worst = melhora;
                    } else if (worst > melhora) {
                        worst = melhora;
                    }
                }

            }

        }
        if (melhorado) {
            System.out.println(" -Foi melhorado em todos os casos: w:" + worst + " b:" + best);
        }
    }
}
