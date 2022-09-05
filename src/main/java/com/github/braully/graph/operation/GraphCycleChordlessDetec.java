package com.github.braully.graph.operation;

import com.github.braully.graph.GraphWS;
import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.log4j.Logger;
import util.BFSUtil;
import util.MapCountOpt;

public class GraphCycleChordlessDetec implements IGraphOperation {

    static final String type = "General";
    static final String description = "Cycle chordless detect";

    private static final Logger log = Logger.getLogger(GraphWS.class);

    GraphSubgraph graphSubgraph = new GraphSubgraph();

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        /* Processar a buscar pelo hullset e hullnumber */
        Map<String, Object> response = new HashMap<>();
        Integer size = null;

        try {
            String inputData = graph.getInputData();
            size = Integer.parseInt(inputData);
        } catch (Exception e) {

        }
        if (size == null) {
            size = 6;
//            throw new IllegalArgumentException("Input invalid (not integer): " + graph.getInputData());
        }
        Integer cycleSize = 0;
        Collection cycle = null;
        if (size >= 2) {
            cycle = findCycleBruteForce(graph, size);
        }
        if (cycle != null) {
            response.put("Cycle find ", cycle);
            cycleSize = cycle.size();
        } else {
            response.put("Cycle not find ", 0);
        }
        response.put(IGraphOperation.DEFAULT_PARAM_NAME_RESULT, cycleSize);
        return response;
    }

    public List<Integer> findCycleBruteForce(UndirectedSparseGraphTO<Integer, Integer> graph, int currentSize) {
        List<Integer> vertices = (List<Integer>) graph.getVertices();
        List<Integer> cycle = null;
        int veticesCount = vertices.size();
        BFSDistanceLabeler<Integer, Integer> bfs = new BFSDistanceLabeler();
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
                if (curPos != currentSet[currentSet.length - 1]) {
//                    System.out.println("new cycle: " + curPos + " " + currentSet[currentSet.length - 1]);
                    curPos = currentSet[currentSet.length - 1];
                }
                mcount.clear();
                isCycle = null;
                for (int iv : currentSet) {
                    Integer v = graph.verticeByIndex(iv);
                    for (int iw : currentSet) {
                        Integer w = graph.verticeByIndex(iw);
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
                    for (int iv : currentSet) {
                        Integer v = graph.verticeByIndex(iv);
                        isCycle = isCycle && mcount.getCount(v) == 2;
                    }
                }
                if (isCycle) {
                    cycle = new ArrayList<>();
                    for (int i : currentSet) {
                        cycle.add(i);
                    }
                    UndirectedSparseGraphTO subGraphInduced = graphSubgraph.subGraphInduced(graph, cycle);
                    Integer v0 = subGraphInduced.verticeByIndex(0);
                    bfs.labelDistances(subGraphInduced, v0);
                    for (Integer v : currentSet) {
                        int distance = bfs.getDistance(subGraphInduced, v);
                        if (distance < 0) {
                            cycle = null;
                            break;
                        }
                    }
//                    break;
                }
                if (cycle != null) {
                    break;
                }
            }
        }
        return cycle;
    }

    public String getTypeProblem() {
        return type;
    }

    public String getName() {
        return description;
    }

    //https://www.geeksforgeeks.org/detect-cycle-in-an-undirected-graph-using-bfs/
    public boolean isChordlessCycle(UndirectedSparseGraphTO<Integer, Integer> graph, int[] currentSet) {
        boolean ret = false;

        return ret;
    }
}
