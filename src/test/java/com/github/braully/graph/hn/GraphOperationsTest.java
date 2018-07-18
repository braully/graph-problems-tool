/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph.hn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.operation.GraphCaratheodoryHeuristicHybrid;
import java.util.HashSet;
import java.util.Set;
import junit.framework.TestCase;

/**
 *
 * @author Braully Rocha da Silva
 */
public class GraphOperationsTest extends TestCase {

    String jsonRequest = "{\"vertices\": [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14], \"defaultEdgeType\": \"UNDIRECTED\","
            + "	\"edges\": [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 21, 22, 23, 24, 25, 26, 27],"
            + "	\"pairs\": [ [ 14, 5 ], [ 14, 8 ], [ 13, 4 ], [ 13, 1 ], [ 12, 5 ], [ 12, 12 ], [ 11, 5 ], [ 11, 3 ], [ 10, 1 ], [ 10, 2 ], [ 9, 3 ], [ 9, 2 ], [ 8, 1 ], [ 8, 11 ], [ 7, 7 ], [ 7, 13 ], [ 6, 0 ], [ 6, 4 ], [ 5, 9 ], [ 5, 10 ], [ 4, 2 ], [ 3, 3 ], [ 3, 0 ], [ 2, 6 ], [ 2, 0 ], [ 1, 12 ], [ 1, 4 ]],"
            + "	\"degrees\": [ 3, 5, 5, 4, 4, 5, 3, 2, 3, 3, 3, 3, 3, 3, 2], \"edgeCount\": 27,	\"vertexCount\": 15}";

    UndirectedSparseGraphTO graph = null;

    @Override
    protected void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        graph = mapper.readValue(jsonRequest, UndirectedSparseGraphTO.class);
        super.setUp();
    }

    public void testConverterFronJson() {
        GraphCaratheodoryHeuristicHybrid heuristic = new GraphCaratheodoryHeuristicHybrid();

        Set<Integer> s = new HashSet<>();
        Set<Integer> hs = new HashSet<>();
        Set<Integer> promotable = new HashSet<>();
        int[] aux = new int[graph.getVertexCount()];

        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
        }

        Integer v = 6;
        Integer partial = v;
        hs.add(v);

        System.out.println("Adding vertice " + v + " to parcial");

        Integer nv0 = heuristic.selectBestNeighbor(v, graph, aux, partial, aux);

        System.out.println("Adding vertice " + nv0 + " to S");

        heuristic.addVertToS(nv0, s, graph, aux);
        promotable.add(nv0);

        Integer nv1 = heuristic.selectBestNeighbor(v, graph, aux, partial, aux);

        System.out.print("Aux = {");
        for (int i = 0; i < graph.getVertexCount(); i++) {
            System.out.printf("%2d | ", aux[i]);
        }
        System.out.println("}");

        System.out.println("Adding vertice " + nv1 + " to S");

        heuristic.addVertToS(nv1, s, graph, aux);
        promotable.add(nv1);

        System.out.print("Aux = {");
        for (int i = 0; i < graph.getVertexCount(); i++) {
            System.out.printf("%2d | ", aux[i]);
        }
        System.out.println("}");

        Integer vp = 4;
        nv0 = 13;
        nv1 = 1;

        System.out.println("Remove " + vp);
        heuristic.removeVertFromS(vp, s, graph, aux);
        System.out.print("Aux = {");
        for (int i = 0; i < graph.getVertexCount(); i++) {
            System.out.printf("%2d | ", aux[i]);
        }
        System.out.println("}");

        System.out.println("Add " + vp);
        heuristic.addVertToS(vp, s, graph, aux);
        System.out.print("Aux = {");
        for (int i = 0; i < graph.getVertexCount(); i++) {
            System.out.printf("%2d | ", aux[i]);
        }
        System.out.println("}");
    }
}
