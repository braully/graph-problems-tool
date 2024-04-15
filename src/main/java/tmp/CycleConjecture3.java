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
import com.github.braully.graph.operation.GraphHullNumberHeuristicV1;
import com.github.braully.graph.operation.GraphHullNumberOptm;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.math3.util.CombinatoricsUtils;
import static tmp.CycleConjecture.tentarMelhorar;
import util.PairShort;

/**
 *
 * @author strike
 */
public class CycleConjecture3 {

    static GraphHullNumberHeuristicV1 operacaoHeuristica = new GraphHullNumberHeuristicV1();
    static GraphHullNumberOptm operacaoBruta = new GraphHullNumberOptm();

    public static final int FATOR_REDUCAO = 2;

    public static void main(String... args) {
        GraphGeneratorCycle gerador = new GraphGeneratorCycle();
        GraphGeneratorPath geradorCaminho = new GraphGeneratorPath();
//        operacao.setVerbose(true);

//        GraphCaratheodoryNumberBinary operacao = new GraphCaratheodoryNumberBinary();
        UndirectedSparseGraphTO<Integer, Integer> graph = null;
        List<PairShort> arestasPossiveis = new ArrayList<>();

        int n = 20;
        graph = gerador.generateCycleGraph(n);
//            graph = geradorCaminho.generatePathGraph(n);
        System.out.println(graph.getName());
        System.out.println("V: " + graph.getVertices());

        int numeroArestasAdd = 4;
        int exato = (numeroArestasAdd / FATOR_REDUCAO);
//        double fat = ((double) numeroArestasAdd / (double) FATOR_REDUCAO);
        long teto = Math.round((double) ((double) numeroArestasAdd / (double) FATOR_REDUCAO));

        Set<Integer> hullSet = operacaoHeuristica.findMinHullSetGraph(graph);
//            System.out.println(graph.getName() + ": " + hullSet.size());
        System.out.println("h: " + hullSet.size());
        System.out.println("add: " + numeroArestasAdd);
        System.out.println("melhoria esperada piso: " + exato);
        System.out.println("melhoria esperada teto: " + teto);
        int hn = hullSet.size();

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n - 1; j++) {
                PairShort get = PairShort.get(i, j + 1);
                if (graph.isNeighbor(i, j + 1)) {
                    continue;
                }
                arestasPossiveis.add(get);
            }
        }
        System.out.println("Arestas disponiveis: " + arestasPossiveis.size());
        System.out.println(arestasPossiveis);
        int contm = 0;
        Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(arestasPossiveis.size(), numeroArestasAdd);
//            System.out.println(hullSet);
        while (combinationsIterator.hasNext()) {
            int[] currentSet = combinationsIterator.next();
            UndirectedSparseGraphTO clone = graph.clone();
            StringBuilder name = new StringBuilder();
            name.append(graph.getName());
            name.append("+(");
            for (int x : currentSet) {
                PairShort p = arestasPossiveis.get(x);
                clone.addEdge(p.key, p.value);
                name.append(p.toString());
                name.append("|");
            }
            clone.setName(name.append(")").toString());
            hullSet = operacaoHeuristica.findMinHullSetGraph(clone);
            if (hn == hullSet.size()) {
                System.out.println("Não melhorado");
                System.out.println(" -" + clone.getName() + ": " + hullSet.size());
//                        System.out.println(" -" + hullSet);
                tentarMelhorar(clone, n, hn);
            } else {
                int deltam = hn - hullSet.size();

                if (deltam < exato) {
                    Set hullForce = operacaoBruta.findMinHullSetGraph(clone);
                    deltam = hn - hullForce.size();
                    if (hullSet.size() != hullForce.size() && deltam < exato) {
                        System.out.println("CONFIRMADO pela força bruta");
                        System.out.println("Melhorados: " + contm);
                        System.out.println("Melhorado: " + clone.getName() + " em " + (deltam));
                        System.out.println(new TreeSet<>(hullSet));
                    } else {
                        contm++;
//                        deltam = hn - hullForce.size();
//                        System.out.println("Melhorado-reavalização: " + clone.getName() + " em " + (deltam));
                    }
//                    System.out.println();
                } else {
                    contm++;
                }
            }
        }
    }

}
