package com.github.braully.graph;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Transport Object of Graph representation.
 *
 * @author braully
 */
public class UndirectedSparseGraphTO<V extends Number, E extends Number> extends UndirectedSparseGraph {

    public UndirectedSparseGraphTO() {
        super();
    }

    public UndirectedSparseGraphTO(String strEdegdsGraph) {
        this();
        this.addEdgesFromString(strEdegdsGraph);
    }

    private List<V> cacheVertices;

    public void addEdgesFromString(String strEdges) {
        String[] edges = null;
        if (strEdges != null && (edges = strEdges.trim().split(",")) != null) {
            try {
                int countEdge = this.getEdgeCount();
                for (String stredge : edges) {
                    String[] vs = stredge.split("-");

                    if (vs != null && vs.length >= 2) {
                        Integer source = Integer.parseInt(vs[0].trim());
                        Integer target = Integer.parseInt(vs[1].trim());
                        this.addEdge(countEdge++, source, target);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

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
        return cacheVertices();
    }

    public <V> V maxVertex() {
        V max = (V) Collections.max(this.cacheVertices());
        return max;
    }

    public List cacheVertices() {
        if (cacheVertices == null) {
            Collection vals = super.getVertices();
            List listVals = new ArrayList();
            listVals.addAll(vals);
            Collections.sort(listVals);
            cacheVertices = listVals;
        }
        return cacheVertices;

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

    @Override
    public boolean removeVertex(Object vertex) {
        clearCachedVertices();
        return super.removeVertex(vertex);
    }

    @Override
    public boolean addVertex(Object vertex) {
        clearCachedVertices();
        return super.addVertex(vertex);
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
        while (this.containsEdge(edgeCount)) {
            edgeCount = edgeCount + 1;
        }
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

    /* Extra methodos, for generate  */
    public Collection<V> getNeighborsUnprotected(V vertex) {
        if (!containsVertex(vertex)) {
            return null;
        }
        return ((Map<V, E>) vertices.get(vertex)).keySet();
    }

    @JsonIgnore
    public boolean containStrict(UndirectedSparseGraphTO subgraph, int[] perm) {
        boolean ret = true;
        List<V> vertices1 = (List<V>) this.getVertices();
        Collection<Pair<V>> pairs = subgraph.getPairs();
        Collection<Pair<V>> thispairs = this.getPairsSet();
        Iterator<Pair<V>> iterator = pairs.iterator();
        Pair<V> pair = null;
        while (iterator.hasNext() && ret) {
            Pair<V> edge = iterator.next();
            V first = edge.getFirst();
            V second = edge.getSecond();
            int indexOf = indexOf(first, perm);
            first = vertices1.get(indexOf);
            indexOf = indexOf(second, perm);
            second = vertices1.get(indexOf);
            pair = new Pair<V>(first, second);
            boolean contains = thispairs.contains(pair);
            if (!contains) {
                pair = new Pair<V>(second, first);
                contains = thispairs.contains(pair);
            }
            ret = ret && contains;
        }
        return ret;
    }

    public static int indexOf(Object nee, int[] haystack) {
        int needle = ((Integer) nee);
        for (int i = 0; i < haystack.length; i++) {
            if (haystack[i] == needle) {
                return i;
            }
        }
        return -1;
    }

    public Integer verticeByIndex(int v) {
        return (Integer) cacheVertices().get(v);
    }

    private void clearCachedVertices() {
        cacheVertices = null;
    }

}
