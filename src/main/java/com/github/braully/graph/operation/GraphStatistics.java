package com.github.braully.graph.operation;

import com.github.braully.graph.GraphWS;
import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import org.apache.log4j.Logger;

public class GraphStatistics implements IGraphOperation {
    
    static final String type = "Graph Class";
    static final String description = "Statistics";
    
    private static final Logger log = Logger.getLogger(GraphWS.class);
    
    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        /* Processar a buscar pelo hullset e hullnumber */
        Map<String, Object> response = new HashMap<>();
        try {
            DistanceStatistics distanceStatistics = new DistanceStatistics();
            double diameter = distanceStatistics.diameter(graph);
            response.put("Diameter", diameter);
            int girth = girth(graph);
            if (girth > 0) {
                response.put("Girth", girth);
            } else {
                response.put("Girth", "infinity");
            }
            
            response.put("n", graph.getVertexCount());
            response.put("m", graph.getEdgeCount());
            
            int Lambda = 0, lambda = Integer.MAX_VALUE;
            for (Integer i : (Collection<Integer>) graph.getVertices()) {
                lambda = Math.min(lambda, graph.getNeighborCount(i));
                Lambda = Math.max(Lambda, graph.getNeighborCount(i));
            }
            response.put("δ", lambda);
            response.put("Δ", Lambda);
        } catch (Exception ex) {
            log.error(null, ex);
        }
        return response;
    }

    /* 
    Reference: 
    https://github.com/jaspervdj/Genus/blob/master/src/genus/FindGirth.java
    https://stackoverflow.com/questions/12890106/find-the-girth-of-a-graph
    
     */
    public int girth(UndirectedSparseGraphTO<Integer, Integer> graph) {
//        double girth = -1;
//        BFSDistanceLabeler<Integer, Integer> bdl = new BFSDistanceLabeler<>();
//        Collection<Integer> vertices = graph.getVertices();
//        for (Integer v : vertices) {
//            bdl.labelDistances(graph, v);
//        }
//        return girth;
        /**
         * Local inner class to represent vertices found at a certain depth.
         */
        class Node {
            
            public int vertex, depth;
            
            public Node(int vertex, int depth) {
                this.vertex = vertex;
                this.depth = depth;
            }
        }
        
        String path = "";
        /* Used for labelling vertices. */
        int[] labels = new int[graph.getVertexCount()];

        /* Best (smallest) cycle length. */
        int best = graph.getVertexCount() - 1;

        /* Queue for our BFS. */
        Queue<Node> queue = new LinkedList<Node>();

        /* Start a BFS from every vertex except the last two (not needed). */
        int root = 0;
        while (root < graph.getVertexCount() - 2 && best > 3) {

            /* Reset labels. */
            for (int i = 0; i < labels.length; i++) {
                labels[i] = -1;
            }

            /* Add initial node to the queue. */
            labels[root] = 0;
            queue.add(new Node(root, 0));

            /* Take next item from the queue. */
            Node node = queue.poll();
            while (node != null && best > 3 && (node.depth + 1) * 2 - 1 < best) {

                /* Depth of neighbours. */
                int depth = node.depth + 1;

                /* Check all neighbours. */
                Collection<Integer> vertNeighbors = graph.getNeighbors(node.vertex);
                for (Integer neighbour
                        : vertNeighbors) {
                    /* We haven't seen this neighbour before. */
                    if (labels[neighbour] < 0) {
                        queue.add(new Node(neighbour, depth));
                        labels[neighbour] = depth;
                        /* Cycle with odd number of edges. */
                    } else if (labels[neighbour] == depth - 1) {
                        if (depth * 2 - 1 < best) {
                            best = depth * 2 - 1;
                            path = root + "-" + neighbour;
                        }
                        /* Cycle with even number of edges. */
                    } else if (labels[neighbour] == depth) {
                        if (depth * 2 < best) {
                            best = depth * 2;
                            path = root + "-" + neighbour;
                        }
                    }
                }

                /* Take next item from the queue. */
                node = queue.poll();
            }

            /* Clear the queue and prepare to start a BFS from a next vertex. */
            queue.clear();
            root++;
        }
        System.out.println("Path: " + path);
        log.info("Path: " + path);
        /* We don't want any division by zero errors. */
        return best > 0 ? best : 1;
    }
    
    public String getTypeProblem() {
        return type;
    }
    
    public String getName() {
        return description;
    }
}
