package com.github.braully.graph.operation;

import com.github.braully.graph.GraphWS;
import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.iterators.PermutationIterator;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.log4j.Logger;
import util.BFSUtil;

public class GraphPathDetec implements IGraphOperation {

    static final String type = "General";
    static final String description = "Path hamilton check";

    private static final Logger log = Logger.getLogger(GraphWS.class);

    GraphSubgraph graphSubgraph = new GraphSubgraph();
    BFSUtil bfsUtil = null;
    Integer maxV;

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

        Collection path = null;
        List<Integer> vertices = (List<Integer>) graph.getVertices();
        Integer cycleSize = vertices.size();

        if (size == null) {
            size = cycleSize;
//            throw new IllegalArgumentException("Input invalid (not integer): " + graph.getInputData());
        }
        maxV = Collections.max(vertices) + 1;
        bfsUtil = BFSUtil.newBfsUtilCompactMatrix(maxV);
        bfsUtil.labelDistancesCompactMatrix(graph);
        if (size >= 2) {
            path = findPathBruteForce(graph, size);
        }

        if (path != null) {
            response.put("Path find", path);
            cycleSize = path.size();
            System.out.println("Path find[" + cycleSize + "]: " + path);
        } else {
            System.out.println("Not find path of size: " + cycleSize);
            response.put("Not find", 0);
            size = 0;
        }
        response.put(IGraphOperation.DEFAULT_PARAM_NAME_RESULT, size);
        return response;
    }

    public List<Integer> findPathBruteForce(UndirectedSparseGraphTO<Integer, Integer> graph, int currentSize) {
        List<Integer> vertices = (List<Integer>) graph.getVertices();
        List<Integer> path = null;
        int veticesCount = vertices.size();
        if (currentSize <= veticesCount) {
            PermutationIterator perm = new PermutationIterator(vertices);

//            Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(veticesCount, currentSize);
            while (perm.hasNext()) {
                List<Integer> next = perm.next();
//                int[] currentSet = combinationsIterator.next();
                Boolean isPath = true;
                //
                for (int i = 0; i < next.size() - 1; i++) {
                    Integer v = next.get(i);
                    Integer w = next.get(i + 1);
//                    System.out.println("Chekc vertices: " + v + " " + w + ": " + bfsUtil.get(v, w));
                    if (bfsUtil.get(v, w) != 1) {
                        isPath = false;
                        break;
                    }
                }

                if (isPath) {
                    path = next;

                }
                if (path != null) {
                    break;
                }
            }
        }
        return path;
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

//    public static void main(String... args) throws IOException {
//        GraphPathDetec pathDetect = new GraphPathDetec();
//        pathDetect.findPathBruteForce(graph, 0);
//    }
}
