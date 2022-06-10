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
import com.github.braully.graph.UtilGraph;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.junit.Test;
import util.BFSUtil;
import util.MapCountOpt;

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

    @Test
    public void testFindAllCyclesBruteForce() throws FileNotFoundException, IOException {
        System.out.println("findCycleBruteForce");
        UndirectedSparseGraphTO<Integer, Integer> graph = UtilGraph.loadGraphES(new FileInputStream("./grafo-moore-50.es"));
        int contCycle = 0;
        int currentSize = 5;
        Integer vind = 47;
        List<Integer> vertices = new ArrayList<>(graph.getVertices());
        vertices.remove(vind);

        int[] setcheck = new int[5 + 1];
        setcheck[0] = vind;

        List<Integer> cycle = null;
        int veticesCount = vertices.size();
        if (currentSize < veticesCount) {
            Integer maxV = Collections.max(vertices) + 1;
            MapCountOpt mcount = new MapCountOpt(maxV);
            BFSUtil bfsUtil = BFSUtil.newBfsUtilCompactMatrix(maxV);
            bfsUtil.labelDistancesCompactMatrix(graph);
            int curPos = -1;
            Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(veticesCount, currentSize);
            Boolean isCycle = null;
            while (combinationsIterator.hasNext()) {
                int[] currentSet = combinationsIterator.next();
//                if (curPos != currentSet[currentSet.length - 1]) {
////                    System.out.println("new cycle: " + curPos + " " + currentSet[currentSet.length - 1]);
//                    curPos = currentSet[currentSet.length - 1];
//                }
                mcount.clear();
                isCycle = null;
                for (int i = 0; i < currentSet.length; i++) {
                    setcheck[i + 1] = vertices.get(currentSet[i]);
                }
                for (int iv : setcheck) {
                    Integer v = iv;
                    for (int iw : setcheck) {
                        Integer w = iw;
                        if (bfsUtil.get(v, w) == 1) {
                            Integer inc = mcount.inc(v);
                            if (inc > 2) {
                                //V tem mais de dois vizinhos no ciclo, 
                                // não é permitido em um ciclo chordless
                                isCycle = false;
                                break;
                            }
                        }
                    }
                }

                if (isCycle == null) {
                    isCycle = true;
                    for (int iv : setcheck) {
                        Integer v = iv;
                        isCycle = isCycle && mcount.getCount(v) == 2;
                    }
                }
                if (isCycle) {
                    cycle = new ArrayList<>();
                    for (int i : setcheck) {
                        cycle.add(i);
                    }
                    contCycle++;
                    System.out.printf("%4d-", contCycle);
                    System.out.println(cycle);
//                    break;
                }
            }
            System.out.println("V: " + vind);
            System.out.println("Cycle count:  " + contCycle);
        }

    }

}
