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
import com.github.braully.graph.generator.GraphKneserGenarator;
import com.github.braully.graph.operation.GraphCaratheodoryNumberOptm;
import com.github.braully.graph.operation.GraphStatistics;
import static com.github.braully.graph.operation.OperationConvexityGraphResult.PARAM_NAME_CARATHEODORY_NUMBER;
import static com.github.braully.graph.operation.OperationConvexityGraphResult.PARAM_NAME_CARATHEODORY_SET;
import static com.github.braully.graph.operation.OperationConvexityGraphResult.PARAM_NAME_CONVEX_HULL;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author strike
 */
public class KneserConject {

    public static void main(String... args) {
        GraphKneserGenarator gerador = new GraphKneserGenarator();
//        GraphGeneratorHypercube gerador = new GraphGeneratorHypercube();
//        GraphHullNumberOptm operacao = new GraphHullNumberOptm();
//        TSSBruteForceOptm operacao = new TSSBruteForceOptm();
//        operacao.setK(2);
        GraphStatistics statistics = new GraphStatistics();
        GraphCaratheodoryNumberOptm operacao = new GraphCaratheodoryNumberOptm();

        UndirectedSparseGraphTO<Integer, Integer> graph = null;
        for (int n = 2; n < 100; n++) {
            graph = gerador.generate(n * 2 + 1, n);
            Map<String, Object> stats = statistics.doOperation(graph);
            System.out.println(graph.getName());
            System.out.println(" - statistics: " + stats);

//            Set<Integer> hullSet = operacao.findMinHullSetGraph(graph);
//            System.out.println(graph.getName() + ": " + hullSet.size());
//            System.out.println(hullSet);
            Map<String, Object> doOperation = operacao.doOperation(graph);
            Collection<Integer> set = (Collection<Integer>) doOperation.get(PARAM_NAME_CARATHEODORY_SET);
            Integer nc = (Integer) doOperation.get(PARAM_NAME_CARATHEODORY_NUMBER);
            Set hs = (Set) doOperation.get(PARAM_NAME_CONVEX_HULL);

            System.out.println(graph.getName() + ": " + nc);
            System.out.println("Caratheodory set: " + set);
            System.out.println("Convex hull size: " + hs.size());
            System.out.println("Convex hull: " + hs);
        }
    }
}
