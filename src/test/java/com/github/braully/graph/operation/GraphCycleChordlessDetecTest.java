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
package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author Braully Rocha da Silva
 */
public class GraphCycleChordlessDetecTest {

    String strGraph = "4-2,3-0,3-2,2-0,1-3,1-0,";

    UndirectedSparseGraphTO graphForTest1 = new UndirectedSparseGraphTO(strGraph);

    public GraphCycleChordlessDetecTest() {
    }

    @Test
    public void testFindCycleBruteForce() {
        System.out.println("findCycleBruteForce");
        UndirectedSparseGraphTO<Integer, Integer> graph = this.graphForTest1.clone();
        int currentSize = 4;
        GraphCycleChordlessDetec instance = new GraphCycleChordlessDetec();
        List<Integer> result = instance.findCycleBruteForce(graph, currentSize);
        System.out.println("Cycle: " + result);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

}
