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
import com.github.braully.graph.UtilGraph;
import com.github.braully.graph.operation.GraphHullSetNC;
import com.github.braully.graph.operation.OperationConvexityGraphResult;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author strike
 */
public class ExecTmp {

    public static void main(String... args) throws FileNotFoundException, IOException {
        GraphHullSetNC subgraph = new GraphHullSetNC();
//        UndirectedSparseGraphTO<Integer, Integer> graphES = UtilGraph.loadGraphES(new FileInputStream("./estripado-esqueleto-grafo-moore-50.es"));
//        UndirectedSparseGraphTO<Integer, Integer> graphES = UtilGraph.loadGraphES(new FileInputStream("./esqueleto-grafo-moore-50.es"));
//        String viz = "0, 6, 49";
        UndirectedSparseGraphTO<Integer, Integer> graphES = UtilGraph.loadGraphES(new FileInputStream("./esqueleto-ultimo-grafo-moore.es"));
        String viz = "0, 56, 3249";
        Set<Integer> set = new LinkedHashSet<>();
        String[] split = viz.split(",");
        for (String str : split) {
            int parseInt = Integer.parseInt(str.trim());
            set.add(parseInt);
        }

        List<Integer> na = new ArrayList<>();
        OperationConvexityGraphResult hsGraph = subgraph.hsp3(graphES, set);

        for (Integer v : (Collection<Integer>) graphES.getVertices()) {
            if (hsGraph.convexHull.contains(v)) {
                continue;
            }
            Collection<Integer> ns = graphES.getNeighborsUnprotected(v);
            if (Collections.disjoint(hsGraph.convexHull, ns)) {
                na.add(v);
            }
        }
        Collections.sort(na);

        for (Integer sa : na) {
//        for (Integer sa : new Integer[]{30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46}) {
            LinkedHashSet<Integer> seta = new LinkedHashSet<>();
            seta.addAll(set);
            seta.add(sa);
            graphES.setSet(seta);
            hsGraph = subgraph.hsp3(graphES, seta);
            if (hsGraph.convexHull.size() < graphES.getVertexCount()) {
                System.out.println(sa + ": não é envoltoria");
//                System.out.println(hsGraph.toMap());
            } else {
                System.out.println(sa + ": ok");
            }
        }
    }
}
