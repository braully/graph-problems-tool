/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import edu.uci.ics.jung.graph.AbstractTypedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.GraphDecorator;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.Collection;

/**
 * Transport Object of Graph representation.
 *
 * @author braully
 */
public class GraphTO<Integer, Double> extends GraphDecorator<Integer, Double> {

    private String name;

    private String operation;

    private String inputData;

    private Collection set;

    public GraphTO() {
        super(new UndirectedSparseGraph<Integer, Double>());
    }

    public GraphTO(EdgeType edgeType) {
        super(edgeType == EdgeType.DIRECTED ? new DirectedSparseGraph<Integer, Double>() : new UndirectedSparseGraph<Integer, Double>());
    }

    public Collection getSet() {
        return set;
    }

    public void setSet(Collection setStr) {
        this.set = setStr;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getInputData() {
        return inputData;
    }

    public void setInputData(String inputData) {
        this.inputData = inputData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public EdgeType getDefaultEdgeType() {
        if (delegate instanceof UndirectedSparseGraph) {
            return EdgeType.UNDIRECTED;
        } else if (delegate instanceof DirectedSparseGraph) {
            return EdgeType.DIRECTED;
        }
        return null;
    }

    public void setDefaultEdgeType(EdgeType edgeType) {
        if (delegate != null) {
            if (edgeType == EdgeType.DIRECTED && delegate instanceof UndirectedSparseGraph) {
                UndirectedSparseGraph<Integer, Double> graphTmp = new UndirectedSparseGraph<Integer, Double>();
                DirectedSparseGraph<Integer, Double> tmp = new DirectedSparseGraph<>();
//                tmp.a
            } else if (EdgeType.UNDIRECTED == edgeType && delegate instanceof DirectedSparseGraph) {

            }
        }
    }
}
