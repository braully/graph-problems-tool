/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph.hn;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import java.io.IOException;
import junit.framework.TestCase;

/**
 *
 * @author Braully Rocha da Silva
 */
public class UndirectedSparseGraphTOTest extends TestCase {

    public UndirectedSparseGraphTOTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testConverterFronJson() throws IOException {
        String jsonRequest = "{\"vertices\":[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24],"
                + "\"edges\":[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,"
                + "30,31,34,35,36,37,40,41,45,46,47,48,49,50,52,53,54,55,56,57,58,60,61,65,66,67,68,69,71,72,73,"
                + "74,75,76,77,78,79,81,82,83,84,85,86,87,89,90,91,92,93,94,97,98,99,100,101],"
                + "\"pairs\":[[0,7],[0,0],[0,10],[1,4],[1,5],[1,23],[1,19],[2,15],[2,23],[2,4],[2,13],[2,11],[3,5],"
                + "[3,1],[3,19],[3,17],[3,10],[3,7],[4,8],[4,18],[5,14],[5,16],[5,13],[6,22],[6,8],[7,23],[7,11],[7,10],"
                + "[8,14],[8,15],[8,9],[8,0],[9,12],[9,1],[9,10],[9,14],[10,10],[10,17],[11,22],[11,0],[11,20],[11,10],[12,7],"
                + "[12,15],[13,22],[13,23],[14,4],[14,22],[14,18],[14,14],[14,17],[15,13],[15,6],[16,2],[16,11],[16,7],[16,10],"
                + "[16,19],[17,13],[17,4],[17,12],[17,0],[18,16],[18,18],[18,9],[19,8],[19,21],[20,24],[20,9],[21,5],[21,18],[21,4],"
                + "[21,16],[22,18],[22,21],[22,9],[22,1],[23,17],[23,9],[23,21],[24,14],[24,21],[24,0],[24,22],[24,12]],"
                + "\"edgeCount\":85,\"vertexCount\":25,\"defaultEdgeType\":\"UNDIRECTED\"}";

        ObjectMapper mapper = new ObjectMapper();
        UndirectedSparseGraphTO readValue = mapper.readValue(jsonRequest, UndirectedSparseGraphTO.class);
        assertNotNull(readValue);
    }
}
