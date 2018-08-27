/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Transport Object of Graph representation.
 *
 * @author braully
 */
public class UndirectedSparseGraphTO<V, E extends Number> extends UndirectedSparseGraph {

    public Collection<Pair<V>> getPairs() {
        Collection values = this.edges.values();
        return (Collection<Pair<V>>) values;
    }

    @JsonIgnore
    @Override
    public Collection getEdges() {
        return super.getEdges();
    }

    public void setEdges(Map edges) {
        this.edges = edges;
    }

    public void setPairs(Collection pairs) {
        if (pairs != null) {
            Number n = 0;
            for (Object edge : pairs) {
                if (edge instanceof List) {
                    List ed = (List) edge;
                    this.addEdge(n, ed.get(0), ed.get(1));
                    n = n.intValue() + 1;
                } else if (edge instanceof Pair) {
                    Pair p = (Pair) edge;
                    this.addEdge(n, p.getFirst(), p.getSecond());
                    n = n.intValue() + 1;
                }
            }
        }
    }

    @JsonIgnore
    @Override
    public int getEdgeCount() {
        return super.getEdgeCount();
    }

    @JsonIgnore
    @Override
    public int getVertexCount() {
        return super.getVertexCount();
    }

    @JsonIgnore
    @Override
    public EdgeType getDefaultEdgeType() {
        return super.getDefaultEdgeType();
    }

    @Override
    public Collection getVertices() {
        Collection vals = super.getVertices();
        List listVals = new ArrayList();
        listVals.addAll(vals);
        Collections.sort(listVals);
        return listVals;
    }

    public Collection<Pair<V>> getNormalizedPairs() {
        Collection values = this.edges.values();
        Collection pairNormalized = new ArrayList();
        List listvals = (List) getVertices();

        Collection<Pair<V>> pairs = this.getPairs();
        for (Pair<V> par : pairs) {
            pairNormalized.add(new Pair<Integer>(listvals.indexOf(par.getFirst()), listvals.indexOf(par.getSecond())));
        }
        return pairNormalized;
    }

    public void setNormalizedPairs(Collection pairs) {

    }

    public Collection getNormalizedVertices() {
        Collection vertexs = this.getVertices();
        List vetsNormalized = new ArrayList();
        for (int i = 0; i < vertexs.size(); i++) {
            vetsNormalized.add(i);
        }
        return vetsNormalized;
    }

    public void setNormalizedVertices(Collection v) {

    }

    public void setVertices(Collection cs) {
        if (this.vertices != null && !this.vertices.isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (cs != null) {
            for (Object o : cs) {
                this.addVertex(o);
            }
        }
    }

    public Collection getDegrees() {
        List degrees = new ArrayList();
        Collection vertices1 = super.getVertices();
        if (vertices1 != null) {
            for (Object v : vertices1) {
                degrees.add(this.degree(v));
            }
        }
        return degrees;
    }

    public void setDegrees(Collection c) {

    }

    private String name;

    private String operation;

    private String inputData;

    private Collection set;

    private Collection labels;

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

    @JsonIgnore
    public int getAdjacency(V i, V k) {
        return this.isNeighbor(i, k) ? 1 : 0;
    }

    @Override
    public UndirectedSparseGraphTO clone() {
        UndirectedSparseGraphTO clone = new UndirectedSparseGraphTO();
        clone.setVertices(this.getVertices());
        clone.setPairs(this.getPairs());
        clone.setName(name);
        return clone;
    }

    public String getEdgeString() {
        StringBuilder sb = new StringBuilder();
        try {
            Collection<Pair<V>> pairs = this.getPairs();
            for (Pair<V> par : pairs) {
                sb.append(par.getFirst()).append("-").append(par.getSecond()).append(",");
            }
        } catch (Exception e) {

        }
        return sb.toString();
    }

    public void setEdgeString(String str) {

    }

    public Object addEdge(int i, int j) {
        Integer edgeCount = this.getEdgeCount();
        return this.addEdge(edgeCount, i, j) ? edgeCount : null;
    }

    public Collection getLabels() {
        return labels;
    }

    public void setLabels(Collection labels) {
        this.labels = labels;
    }

    @JsonIgnore
    private Set<Pair<V>> setPairs = null;

    @JsonIgnore
    private Collection<Pair<V>> getPairsSet() {
        if (setPairs == null) {
            setPairs = Collections.unmodifiableSet(new HashSet<>(this.getPairs()));
        }
        return setPairs;
    }
}
