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
import com.github.braully.graph.operation.GraphCycleChordlessDetec;
import com.github.braully.graph.operation.GraphHullNumber;
import com.github.braully.graph.operation.IGraphOperation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author strike
 */
public class CycleHullCheck implements IGraphOperation {

    static final String type = "General";
    static final String description = "Cycle-Hull";

    @Override
    public String getTypeProblem() {
        return type;
    }

    @Override
    public String getName() {
        return description;
    }

    GraphCycleChordlessDetec cycle = new GraphCycleChordlessDetec();
    GraphHullNumber hull = new GraphHullNumber();

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        /* Processar a buscar pelo hullset e hullnumber */
        int hulln = 0;
        int cycleSize = 6;
        Map<String, Object> response = new HashMap<>();
        List<Integer> findedCycle = cycle.findCycleBruteForce(graph, cycleSize);
        if (findedCycle != null && !findedCycle.isEmpty()) {
            Set<Integer> hullSet = hull.calcMinHullNumberGraph(graph);
            hulln = hullSet.size();
        }
        response.put(IGraphOperation.DEFAULT_PARAM_NAME_RESULT, hulln);
        return response;
    }

}
