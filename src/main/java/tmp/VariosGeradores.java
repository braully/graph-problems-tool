/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Baseado em:
 * https://github.com/jgrapht/jgrapht/blob/master/jgrapht-core/src/main/java/org/jgrapht/generate/NamedGraphGenerator.java
 *
 * @author strike
 */
public class VariosGeradores {

    private static Map<Integer, Integer> vertexMap = new HashMap<>();

    // -------------Doyle Graph-----------//
    /**
     * Generate the Doyle Graph
     *
     * @see #generateDoyleGraph
     * @return Doyle Graph
     */
    // -------------Generalized Petersen Graph-----------//
    /**
     * @see GeneralizedPetersenGraphGenerator
     * @param n Generalized Petersen graphs $GP(n,k)$
     * @param k Generalized Petersen graphs $GP(n,k)$
     * @return Generalized Petersen Graph
     */
    public static UndirectedSparseGraphTO generalizedPetersenGraph(int n, int k) {
        UndirectedSparseGraphTO g = new UndirectedSparseGraphTO();
        generateGeneralizedPetersenGraph(g, n, k);
        return g;
    }

    private static void generateGeneralizedPetersenGraph(UndirectedSparseGraphTO targetGraph, int n, int k) {
        GeneralizedPetersenGraphGenerator gpgg
                = new GeneralizedPetersenGraphGenerator(n, k);
        gpgg.generateGraph(targetGraph);
    }

    // -------------Petersen Graph-----------//
    /**
     * @see #generatePetersenGraph
     * @return Petersen Graph
     */
    public static UndirectedSparseGraphTO petersenGraph() {
        return generalizedPetersenGraph(5, 2);
    }

    /**
     * Generates a
     * <a href="http://mathworld.wolfram.com/PetersenGraph.html">Petersen
     * Graph</a>. The Petersen Graph is a named graph that consists of 10
     * vertices and 15 edges, usually drawn as a five-point star embedded in a
     * pentagon. It is the generalized Petersen graph $GP(5,2)$
     *
     * @param targetGraph receives the generated edges and vertices; if this is
     * non-empty on entry, the result will be a disconnected graph since
     * generated elements will not be connected to existing elements
     */
    public void generatePetersenGraph(UndirectedSparseGraphTO targetGraph) {
        generateGeneralizedPetersenGraph(targetGraph, 5, 2);
    }

    // -------------Dürer Graph-----------//
    /**
     * Generates a <a href="http://mathworld.wolfram.com/DuererGraph.html">Dürer
     * Graph</a>. The Dürer graph is the skeleton of Dürer's solid, which is the
     * generalized Petersen graph $GP(6,2)$.
     *
     * @return the Dürer Graph
     */
    public static UndirectedSparseGraphTO dürerGraph() {
        return generalizedPetersenGraph(6, 2);
    }

    /**
     * Generates a <a href="http://mathworld.wolfram.com/DuererGraph.html">Dürer
     * Graph</a>. The Dürer graph is the skeleton of Dürer's solid, which is the
     * generalized Petersen graph $GP(6,2)$.
     *
     * @param targetGraph receives the generated edges and vertices; if this is
     * non-empty on entry, the result will be a disconnected graph since
     * generated elements will not be connected to existing elements
     */
    public void generateDürerGraph(UndirectedSparseGraphTO targetGraph) {
        generateGeneralizedPetersenGraph(targetGraph, 6, 2);
    }

    // -------------Dodecahedron Graph-----------//
    /**
     * @see #generateDodecahedronGraph
     * @return Dodecahedron Graph
     */
    public static UndirectedSparseGraphTO dodecahedronGraph() {
        return generalizedPetersenGraph(10, 2);
    }

    /**
     * Generates a
     * <a href="http://mathworld.wolfram.com/DodecahedralGraph.html">Dodecahedron
     * Graph</a>. The skeleton of the dodecahedron (the vertices and edges) form
     * a graph. It is one of 5 Platonic graphs, each a skeleton of its Platonic
     * solid. It is the generalized Petersen graph $GP(10,2)$
     *
     * @param targetGraph receives the generated edges and vertices; if this is
     * non-empty on entry, the result will be a disconnected graph since
     * generated elements will not be connected to existing elements
     */
    public void generateDodecahedronGraph(UndirectedSparseGraphTO targetGraph) {
        generateGeneralizedPetersenGraph(targetGraph, 10, 2);
    }

    // -------------Desargues Graph-----------//
    /**
     * @see #generateDesarguesGraph
     * @return Desargues Graph
     */
    public static UndirectedSparseGraphTO desarguesGraph() {
        return generalizedPetersenGraph(10, 3);
    }

    /**
     * Generates a
     * <a href="http://mathworld.wolfram.com/DesarguesGraph.html">Desargues
     * Graph</a>. The Desargues graph is a cubic symmetric graph
     * distance-regular graph on 20 vertices and 30 edges. It is the generalized
     * Petersen graph $GP(10,3)$
     *
     * @param targetGraph receives the generated edges and vertices; if this is
     * non-empty on entry, the result will be a disconnected graph since
     * generated elements will not be connected to existing elements
     */
    public void generateDesarguesGraph(UndirectedSparseGraphTO targetGraph) {
        generateGeneralizedPetersenGraph(targetGraph, 10, 3);
    }

    // -------------Nauru Graph-----------//
    /**
     * @see #generateNauruGraph
     * @return Nauru Graph
     */
    public static UndirectedSparseGraphTO nauruGraph() {
        return generalizedPetersenGraph(12, 5);
    }

    /**
     * Generates a <a href="http://mathworld.wolfram.com/NauruGraph.html">Nauru
     * Graph</a>. The Nauru graph is a symmetric bipartite cubic graph with 24
     * vertices and 36 edges. It is the generalized Petersen graph $GP(12,5)$
     *
     * @param targetGraph receives the generated edges and vertices; if this is
     * non-empty on entry, the result will be a disconnected graph since
     * generated elements will not be connected to existing elements
     */
    public void generateNauruGraph(UndirectedSparseGraphTO targetGraph) {
        generateGeneralizedPetersenGraph(targetGraph, 12, 5);
    }

    // -------------Möbius-Kantor Graph-----------//
    /**
     * Generates a
     * <a href="http://mathworld.wolfram.com/Moebius-KantorGraph.html">Möbius-Kantor
     * Graph</a>. The unique cubic symmetric graph on 16 nodes. It is the
     * generalized Petersen graph $GP(8,3)$
     *
     * @return the Möbius-Kantor Graph
     */
    public static UndirectedSparseGraphTO möbiusKantorGraph() {
        return generalizedPetersenGraph(8, 3);
    }

    /**
     * Generates a
     * <a href="http://mathworld.wolfram.com/Moebius-KantorGraph.html">Möbius-Kantor
     * Graph</a>. The unique cubic symmetric graph on 16 nodes. It is the
     * generalized Petersen graph $GP(8,3)$
     *
     * @param targetGraph receives the generated edges and vertices; if this is
     * non-empty on entry, the result will be a disconnected graph since
     * generated elements will not be connected to existing elements
     */
    public void generateMöbiusKantorGraph(UndirectedSparseGraphTO targetGraph) {
        generateGeneralizedPetersenGraph(targetGraph, 8, 3);
    }

    // -------------Bull Graph-----------//
    /**
     * @see #generateBullGraph
     * @return Bull Graph
     */
    public static UndirectedSparseGraphTO bullGraph() {
        UndirectedSparseGraphTO g = new UndirectedSparseGraphTO();
        generateBullGraph(g);
        return g;
    }

    /**
     * Generates a <a href="http://mathworld.wolfram.com/BullGraph.html">Bull
     * Graph</a>. The bull graph is a simple graph on 5 nodes and 5 edges whose
     * name derives from its resemblance to a schematic illustration of a bull
     * or ram
     *
     * @param targetGraph receives the generated edges and vertices; if this is
     * non-empty on entry, the result will be a disconnected graph since
     * generated elements will not be connected to existing elements
     */
    public static void generateBullGraph(UndirectedSparseGraphTO targetGraph) {
        vertexMap.clear();
        addEdge(targetGraph, 0, 1);
        addEdge(targetGraph, 1, 2);
        addEdge(targetGraph, 2, 3);
        addEdge(targetGraph, 1, 3);
        addEdge(targetGraph, 3, 4);
    }

    // -------------Butterfly Graph-----------//
    /**
     * @see #generateButterflyGraph
     * @return Butterfly Graph
     */
    public static UndirectedSparseGraphTO butterflyGraph() {
        UndirectedSparseGraphTO g = new UndirectedSparseGraphTO();
        generateButterflyGraph(g);
        return g;
    }

    /**
     * Generates a
     * <a href="http://mathworld.wolfram.com/ButterflyGraph.html">Butterfly
     * Graph</a>. This graph is also known as the "bowtie graph" (West 2000, p.
     * 12). It is isomorphic to the friendship graph $F_2$.
     *
     * @param targetGraph receives the generated edges and vertices; if this is
     * non-empty on entry, the result will be a disconnected graph since
     * generated elements will not be connected to existing elements
     */
    public static void generateButterflyGraph(UndirectedSparseGraphTO targetGraph) {
        new WindmillGraphsGenerator(Mode.DUTCHWINDMILL, 2, 3).generateGraph(targetGraph);
    }

    // -------------Claw Graph-----------//
    /**
     * @see #generateClawGraph
     * @return Claw Graph
     */
    // --------------Helper methods-----------------/
    private static Integer addVertex(UndirectedSparseGraphTO targetGraph, int i) {
        targetGraph.addVertex(i);
        vertexMap.put(i, i);
        return i;
    }

    private static void addEdge(UndirectedSparseGraphTO targetGraph, int i, int j) {
        addVertex(targetGraph, i);
        addVertex(targetGraph, j);
        targetGraph.addEdge(i, j);
    }

    private static void addCycle(UndirectedSparseGraphTO targetGraph, int array[]) {
        for (int i = 0; i < array.length; i++) {
            addEdge(targetGraph, array[i], array[(i + 1) % array.length]);
        }
    }

    public static enum Mode {
        WINDMILL,
        DUTCHWINDMILL
    }

    static class WindmillGraphsGenerator {

        /**
         * WINDMILL and DUTCHWINDMILL Modes for the Constructor
         */
        private final Mode mode;
        private final int m;
        private final int n;

        /**
         * Constructs a GeneralizedPetersenGraphGenerator used to generate a
         * Generalized Petersen graphs $GP(n,k)$.
         *
         * @param mode indicate whether the generator should generate Windmill
         * graphs or Dutch Windmill graphs
         * @param m number of copies of $C_n$ (Dutch Windmill graph) or $K_n$
         * (Windmill graph)
         * @param n size of $C_n$ (Dutch Windmill graph) or $K_n$ (Windmill
         * graph). To generate friendship graphs, set $n=3$ (the mode is
         * irrelevant).
         */
        public WindmillGraphsGenerator(Mode mode, int m, int n) {
            if (m < 2) {
                throw new IllegalArgumentException("m must be larger or equal than 2");
            }
            if (n < 3) {
                throw new IllegalArgumentException("n must be larger or equal than 3");
            }

            this.mode = mode;
            this.m = m;
            this.n = n;
        }

        public void generateGraph(UndirectedSparseGraphTO target) {
            Integer center = target.addVertex();
            List<Integer> sub = new ArrayList<>(n);

            if (mode == Mode.DUTCHWINDMILL) { // Generate Dutch windmill graph
                for (int i = 0; i < m; i++) { // m copies of cycle graph Cn
                    sub.clear();
                    sub.add(center);
                    for (int j = 1; j < n; j++) {
                        sub.add(target.addVertex());
                    }

                    for (int r = 0; r < sub.size(); r++) {
                        target.addEdge(sub.get(r), sub.get((r + 1) % n));
                    }
                }
            } else { // Generate windmill graph
                for (int i = 0; i < m; i++) { // m copies of complete graph Kn
                    sub.clear();
                    sub.add(center);
                    for (int j = 1; j < n; j++) {
                        sub.add(target.addVertex());
                    }

                    for (int r = 0; r < sub.size() - 1; r++) {
                        for (int s = r + 1; s < sub.size(); s++) {
                            target.addEdge(sub.get(r), sub.get(s));
                        }
                    }
                }
            }
        }
    }

    static class GeneralizedPetersenGraphGenerator {

        private final int n;
        private final int k;

        /**
         * Key used to access the star polygon vertices in the resultMap
         */
        public static final String STAR = "star";
        /**
         * Key used to access the regular polygon vertices in the resultMap
         */
        public static final String REGULAR = "regular";

        /**
         * Constructs a GeneralizedPetersenGraphGenerator used to generate a
         * Generalized Petersen graphs $GP(n,k)$.
         *
         * @param n size of the regular polygon (cycle graph $C_n$)
         * @param k size of the star polygon ${n,k}$
         */
        public GeneralizedPetersenGraphGenerator(int n, int k) {
            if (n < 3) {
                throw new IllegalArgumentException("n must be larger or equal than 3");
            }
            if (k < 1 || k > Math.floor((n - 1) / 2.0)) {
                throw new IllegalArgumentException("k must be in the range [1, floor((n-1)/2.0)]");
            }

            this.n = n;
            this.k = k;
        }

        /**
         * Generates the Generalized Petersen Graph
         *
         * @param target receives the generated edges and vertices; if this is
         * non-empty on entry, the result will be a disconnected graph since
         * generated elements will not be connected to existing elements
         * @param resultMap if non-null, the resultMap contains a mapping from
         * the key "star" to a list of vertices constituting the star polygon,
         * as well as a key "regular" which maps to a list of vertices
         * constituting the regular polygon.
         */
        public void generateGraph(UndirectedSparseGraphTO target) {
            List<Integer> verticesU = new ArrayList<>(n); // Regular polygon vertices
            List<Integer> verticesV = new ArrayList<>(n); // Star polygon vertices
            for (int i = 0; i < n; i++) {
                verticesU.add(target.addVertex());
                verticesV.add(target.addVertex());
            }

            for (int i = 0; i < n; i++) {
                target.addEdge(verticesU.get(i), verticesU.get((i + 1) % n));
                target.addEdge(verticesU.get(i), verticesV.get(i));
                target.addEdge(verticesV.get(i), verticesV.get((i + k) % n));
            }
        }
    }
}
