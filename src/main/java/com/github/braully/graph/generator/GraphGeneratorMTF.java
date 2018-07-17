package com.github.braully.graph.generator;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reference:
 * https://github.com/piyushroshan/GraphTheoryProject/blob/master/src/gtc/MaxTriangleFreeGraph.java
 */
public class GraphGeneratorMTF extends AbstractGraphGenerator {

    static final String N_VERTICES = "N";
    static final String[] parameters = {N_VERTICES};
    static final String description = "Maximal Triangle-Free";
    static final Integer DEFAULT_NVERTICES = 5;

    int count = 0;
    boolean interrupt = false;
    private UndirectedSparseGraphTO lastGraph;

    @Override
    public String[] getParameters() {
        return parameters;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public UndirectedSparseGraphTO<Integer, Integer> generateGraph(Map parameters) {
        Integer nvertices = getIntegerParameter(parameters, N_VERTICES);

        if (nvertices == null) {
            nvertices = DEFAULT_NVERTICES;
        }

        UndirectedSparseGraphTO<Integer, Integer> graph = new UndirectedSparseGraphTO<>();
        graph.setName("MTF" + nvertices);

        List<Integer> vertexElegibles = new ArrayList<>(nvertices);
        Integer[] vertexs = new Integer[nvertices];
        for (int i = 0; i < nvertices; i++) {
            vertexElegibles.add(i);
            vertexs[i] = i;
            graph.addVertex(vertexs[i]);
        }
//        int countEdge = 0;
//        for (int i = 0; i < nvertices; i++) {
//            for (int j = i; j < nvertices - 1; j++) {
//                Integer source = vertexs[i];
//                Integer target = vertexs[j] + 1;
//                graph.addEdge(countEdge++, source, target);
//            }
//        }
        this.count = 0;
        int[] vSet = new int[nvertices];
        graph.addEdge(0, 1);
        vSet[1] = 1;
        vSet[2] = 1;
        addEdge(graph, vSet, 2);
        return graph;
    }

    /**
     * Adds an edge to the graph a.
     *
     * @param graphProcessing the graph under processing
     * @param vSet the set of vertices processed
     * @param cV the count of vertices processed
     */
    void addEdge(UndirectedSparseGraphTO graphProcessing, int[] vSet, int cV) {
        if (interrupt) {
            return;
        }
        int n = graphProcessing.getVertexCount();
        if (cV < n) {
            processAddEdge(graphProcessing, vSet, cV);
        } else if (cV == n) {
            processEndAddEdge(graphProcessing, vSet, cV);
        }
    }

    public void processAddEdge(UndirectedSparseGraphTO graphProcessing, int[] vSet, int cV) {
        int n = graphProcessing.getVertexCount();
        for (int i = 0; i < n && !interrupt; i++) {
            for (int j = 0; j < i && !interrupt; j++) {
                if (i != j && graphProcessing.getAdjacency(i, j) == 0
                        && ((vSet[i] == 0 && vSet[j] == 1) || (vSet[i] == 1 && vSet[j] == 0))) {
                    UndirectedSparseGraphTO graphCopy = (UndirectedSparseGraphTO) graphProcessing.clone();
                    graphCopy.addEdge(i, j);
                    int oldi = vSet[i];
                    int oldj = vSet[j];
                    vSet[i] = 1;
                    vSet[j] = 1;

                    if (isTriangleFree(graphCopy, n)) {
                        int[] vSetCopy = new int[n];
                        System.arraycopy(vSet, 0, vSetCopy, 0, vSetCopy.length);
                        addEdge(graphCopy, vSetCopy, cV + vSet[i] - oldi + vSet[j] - oldj);
                    }
                    vSet[i] = oldi;
                    vSet[j] = oldj;
                }
            }
        }
    }

    public void processEndAddEdge(UndirectedSparseGraphTO graphProcessing, int[] vSet, int cV) {
        int n = graphProcessing.getVertexCount();
        boolean flag = true;
        for (int i = 0; i < n && !interrupt; i++) {
            for (int j = 0; j < i && !interrupt; j++) {
                if (i != j && graphProcessing.getAdjacency(i, j) == 0 && vSet[i] == 1 && vSet[j] == 1) {
                    UndirectedSparseGraphTO grpahCopy = (UndirectedSparseGraphTO) graphProcessing.clone();
                    grpahCopy.addEdge(i, j);
                    if (isTriangleFree(grpahCopy, n)) {
                        flag = false;
                        int[] vSetCopy = new int[n];
                        System.arraycopy(vSet, 0, vSetCopy, 0, vSetCopy.length);
                        addEdge(grpahCopy, vSetCopy, cV);
                    }
                }
            }
        }
        if (flag && !interrupt) {
//            System.out.println("Graph Found: " + count);
//            System.out.println(graphProcessing);
            count++;
            addGraph(graphProcessing);
        }
    }

    /**
     * Checks if a graph is triangle free.
     *
     * @param a the graph to be checked
     * @param n the number of vertices
     * @return true if it is triangle free
     */
    Boolean isTriangleFree(UndirectedSparseGraphTO a, int n) {
        int[][] x = new int[n][n];
        int[][] y = new int[n][n];
        int trace = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int sum = 0;
                for (int k = 0; k < n; k++) {
                    sum = sum + a.getAdjacency(i, k) * a.getAdjacency(k, j);
                }
                x[i][j] = sum;
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int sum = 0;
                for (int k = 0; k < n; k++) {
                    sum = sum + x[i][k] * a.getAdjacency(k, j);
                }
                y[i][j] = sum;
            }
        }

        for (int i = 0; i < n; i++) {
            trace += y[i][i];
        }
        if (trace == 0) {
            return true;
        } else {
            return false;
        }
    }

    public void addNewVertice(UndirectedSparseGraphTO<Integer, Integer> graph) {
        if (graph == null) {
            return;
        }
        this.interrupt = false;
        this.count = 0;
        int nvertices = graph.getVertexCount() + 1;
        graph.addVertex(graph.getVertexCount());
        int[] vSet = new int[nvertices];
        for (int i = 0; i < nvertices - 1; i++) {
            vSet[i] = 1;
        }
        addEdge(graph, vSet, nvertices - 1);
    }

    public void addGraph(UndirectedSparseGraphTO graphProcessing) {
        this.lastGraph = graphProcessing;
        this.observerGraph(graphProcessing);
    }

    public void observerGraph(UndirectedSparseGraphTO graphProcessing) {

    }

    public void interrupt() {
        this.interrupt = true;
    }

    public UndirectedSparseGraphTO getLastGraph() {
        return lastGraph;
    }

    public int getCount() {
        return count;
    }
}
