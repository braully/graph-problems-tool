package com.github.braully.graph.generator;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;

public class GraphGeneratorChordal extends AbstractGraphGenerator {

    static final String STRING_EDGES = "Edge-string";
    static final String[] parameters = {STRING_EDGES};
    static final String description = "Chordal Graph from Any Graph";

    void max_card_search (UndirectedSparseGraphTO<Integer, Integer> graph, int[] ord, int[] vert){
        int nvertices = graph.getVertexCount();
        int i, j, v, w;
        ArrayList <Integer> vertices = new ArrayList <Integer>(graph.getVertices());

        // We have a set for each possible number of numbered neighbors in a vertex
        boolean sets[][] = new boolean [nvertices][nvertices];

        // size[i] is the number of numbered neighbors to vertex i
        int[] size = new int[nvertices];

        // All vertices have 0 numbered neighbors in the beginning
        for (i=0; i < nvertices; i++)
            sets[0][i] = true;

        // j is the biggest with set[j] not empty
        j=0;

        // For each vertex 
        for (i = nvertices-1; i>=0; i--){
            
            // Gets the first vertex with the biggest number of numbered neighbors
            while(true){
                
                for (v = 0; v < nvertices; v++){
                    if (sets[j][v]){
                        sets[j][v] = false;
                        break;
                    }
                }

                if (v < nvertices){
                    break;
                }

                j--;
            }
            
            // Vertex v receives number i
            ord[v] = i;
            vert[i] = v;
            size[v] = -1;
            
            // For each edge (v, w) such that w is unumbered (size[w] is not -1)
            // adds 1 to the number of numbered vertices of w
            // and places it in the next set
            for (w = 0; w < nvertices; w++){
                if ( graph.findEdge(vertices.get(v), vertices.get(w)) != null && size[w] >= 0){
                    sets[size[w]][w] = false;
                    size[w]++;
                    sets[size[w]][w] = true;
                }
            }
            
            // Each time a vertex receives a number, the maximum number
            // of numbered neighbors of a vertex is <= j+1
            j++;
        }        
    }

    // Adds fill in edges to a graph based on its vertex ordenation
    void fill_in(UndirectedSparseGraphTO<Integer, Integer> graph, int[] ord, int[] vert){
        int nvertices = graph.getVertexCount();
        int v, w, x, i;
        ArrayList <Integer> vertices = new ArrayList <Integer>(graph.getVertices());

        // f[v] is the follower of v, i.e. the neighbor of v
        // with the smallest ordering that is bigger than v's
        int[] f = new int[nvertices];
        
        // index[v] is the biggest vertex between v and
        // v's already processed neighbors 
        int[] index = new int[nvertices];
        
        // We begin processing the vertex with the smallest ordering (i)
        for (i = 0; i < nvertices; i++){
            w = vert[i];
            f[w] = w;
            index[w] = i;
            
            // For each neighbor v of w with ordering smaller than w
            // that it's already processed neighbors have ordering
            // smaller than w too: connect w to v and to it's followers
            // that respect the same properties
            for (v = 0; v < nvertices; v++){
                if ( graph.findEdge(vertices.get(v), vertices.get(w)) != null && ord[v] < i){
                    x = v;
                    while(index[x] < i){
                        // w is an already processed neighbor of x with
                        // bigger ordering, so we update index[x]
                        index[x] = i;
                        
                        // Connect x to w in the fill in
                        graph.addEdge(graph.getEdgeCount(), vertices.get(x), vertices.get(w));
                 
                        // We repeat with the follower of x
                        x = f[x];
                    }
                    
                    // If the last follower of v has itself as a follower,
                    // it shall follow w
                    if (f[x] == x)
                        f[x] = w;
                }
            }
        }
        return;
    }

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

        String strEdges = getStringParameter(parameters, STRING_EDGES);

        UndirectedSparseGraphTO<Integer, Integer> graph = new UndirectedSparseGraphTO<>();
        graph.setName("Chordal");

        graph.addEdgesFromString(strEdges);

        int nvertices = graph.getVertexCount();
        
        // order[i] is the ordering of vertex i
        int[] ord = new int[nvertices];
        // vert[i] is the vertex wich has ordering i
        int[]vert = new int[nvertices];

        max_card_search (graph, ord, vert);
        fill_in(graph, ord, vert);

        return graph;
    }

}
