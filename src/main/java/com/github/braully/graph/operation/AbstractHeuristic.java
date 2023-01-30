/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import static com.github.braully.graph.operation.GraphCaratheodoryCheckSet.NEIGHBOOR_COUNT_INCLUDED;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 *
 * @author strike
 */
public abstract class AbstractHeuristic {

    public Integer K;
    public Integer R;
    public Integer marjority;
    protected int[] kr;
    protected boolean verbose;
    static final String type = "Contamination";

    public String getTypeProblem() {
        return type;
    }

    public void setK(Integer K) {
        this.K = K;
        this.marjority = null;
        this.R = null;
    }

    public void setR(Integer R) {
        this.R = R;
        this.K = null;
        this.marjority = null;
    }

    public void setMarjority(Integer marjority) {
        this.marjority = marjority;
        this.K = null;
        this.R = null;
    }

    public void initKr(UndirectedSparseGraphTO graph) {
        int vertexCount = (Integer) graph.maxVertex() + 1;
        kr = new int[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            if (R != null) {
                kr[i] = Math.min(R, graph.degree(i));
            } else if (K != null) {
                kr[i] = K;
            } else if (marjority != null) {
                kr[i] = graph.degree(i) / marjority;
            }
        }
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public Set<Integer> tryMinimal(UndirectedSparseGraphTO<Integer, Integer> graphRead, Set<Integer> tmp) {
        Set<Integer> s = tmp;
        if (verbose) {
            System.out.println("tentando reduzir: " + s.size());
            System.out.println("s: " + s);
        }
        int cont = 0;
        for (Integer v : tmp) {

//        LinkedList<Integer> tmp2 = new LinkedList<>(tmp);
//        Iterator<Integer> descendingIterator = tmp2.descendingIterator();
//        while (descendingIterator.hasNext()) {
//            Integer v = descendingIterator.next();
            cont++;
            if (graphRead.degree(v) < kr[v]) {
                continue;
            }
            Set<Integer> t = new LinkedHashSet<>(s);
            t.remove(v);
            if (checkIfHullSet(graphRead, t)) {
                s = t;
                if (verbose) {
                    System.out.println("Reduzido removido: " + v);
                    System.out.println("Na posição " + cont + "/" + (tmp.size() - 1));
                }
            }
        }
        return s;
    }

    public Set<Integer> tryMinimal2(UndirectedSparseGraphTO<Integer, Integer> graphRead,
            Set<Integer> tmp) {
        Set<Integer> s = tmp;
        List<Integer> ltmp = new ArrayList<>(tmp);
        if (verbose) {
            System.out.println("tentando reduzir: " + s.size());
            System.out.println("s: " + s);
        }
        Collection<Integer> vertices = graphRead.getVertices();
        int cont = 0;
        for_p:
        for (int h = 0; h < ltmp.size(); h++) {
            Integer x = ltmp.get(h);
            if (graphRead.degree(x) < kr[x] || !s.contains(x)) {
                continue;
            }
            for (int j = h + 1; j < ltmp.size(); j++) {
                Integer y = ltmp.get(j);
                if (graphRead.degree(y) < kr[y] || y.equals(x)
                        || !s.contains(y)) {
                    continue;
                }
                Set<Integer> t = new LinkedHashSet<>(s);
                t.remove(x);
                t.remove(y);

                int contadd = 0;

                int[] aux = new int[(Integer) graphRead.maxVertex() + 1];
                for (int i = 0; i < aux.length; i++) {
                    aux[i] = 0;
                }

                Queue<Integer> mustBeIncluded = new ArrayDeque<>();
                for (Integer iv : t) {
                    Integer v = iv;
                    mustBeIncluded.add(v);
                    aux[v] = kr[v];
                }
                while (!mustBeIncluded.isEmpty()) {
                    Integer verti = mustBeIncluded.remove();
                    contadd++;
                    Collection<Integer> neighbors = graphRead.getNeighborsUnprotected(verti);
                    for (Integer vertn : neighbors) {
                        if (vertn.equals(verti)) {
                            continue;
                        }
                        if (!vertn.equals(verti) && aux[vertn] <= kr[vertn] - 1) {
                            aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                            if (aux[vertn] == kr[vertn]) {
                                mustBeIncluded.add(vertn);
                            }
                        }
                    }
                    aux[verti] += kr[verti];
                }

                for (Integer z : vertices) {
                    if (aux[z] >= kr[z] || z.equals(x) || z.equals(y)) {
                        continue;
                    }
                    int contz = contadd;
                    int[] auxb = (int[]) aux.clone();
                    mustBeIncluded.add(z);
                    auxb[z] = kr[z];
                    while (!mustBeIncluded.isEmpty()) {
                        Integer verti = mustBeIncluded.remove();
                        contz++;
                        Collection<Integer> neighbors = graphRead.getNeighborsUnprotected(verti);
                        for (Integer vertn : neighbors) {
                            if (vertn.equals(verti)) {
                                continue;
                            }
                            if (!vertn.equals(verti) && auxb[vertn] <= kr[vertn] - 1) {
                                auxb[vertn] = auxb[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                                if (auxb[vertn] == kr[vertn]) {
                                    mustBeIncluded.add(vertn);
                                }
                            }
                        }
                        auxb[verti] += kr[verti];
                    }

                    if (contz == vertices.size()) {
                        if (verbose) {
                            System.out.println("Reduzido removido: " + x + " " + y + " adicionado " + z);
                            System.out.println("Na posição " + cont + "/" + (tmp.size() - 1));
                        }
                        t.add(z);
                        s = t;
                        ltmp = new ArrayList<>(s);
//                        h--;
                        h = 0;
                        continue for_p;
                    }
                }

            }
            cont++;

        }
        return s;
    }

    public Set<Integer> tryMinimal2KeepSize(UndirectedSparseGraphTO<Integer, Integer> graphRead,
            Set<Integer> tmp, int sizeKeep) {
        Set<Integer> s = tmp;
        List<Integer> ltmp = new ArrayList<>(tmp);
        if (verbose) {
            System.out.println("tentando reduzir-keep size: " + s.size());
//            System.out.println("s: " + s);
        }
        Collection<Integer> vertices = graphRead.getVertices();
        int cont = -1;
        for_p:
        for (int h = 0; h < ltmp.size(); h++) {
            cont++;
            Integer x = ltmp.get(h);
            if (graphRead.degree(x) < K || !s.contains(x)) {
                continue;
            }
            for (int j = h + 1; j < ltmp.size(); j++) {
                Integer y = ltmp.get(j);
                if (graphRead.degree(y) < K || y.equals(x)
                        || !s.contains(y)) {
                    continue;
                }
                Set<Integer> t = new LinkedHashSet<>(s);
                t.remove(x);
                t.remove(y);

                int contadd = 0;

                int[] aux = new int[(Integer) graphRead.maxVertex() + 1];
                for (int i = 0; i < aux.length; i++) {
                    aux[i] = 0;
                }

                Queue<Integer> mustBeIncluded = new ArrayDeque<>();
                for (Integer iv : t) {
                    Integer v = iv;
                    mustBeIncluded.add(v);
                    aux[v] = K;
                }
                while (!mustBeIncluded.isEmpty()) {
                    Integer verti = mustBeIncluded.remove();
                    contadd++;
                    Collection<Integer> neighbors = graphRead.getNeighborsUnprotected(verti);
                    for (Integer vertn : neighbors) {
                        if (vertn.equals(verti)) {
                            continue;
                        }
                        if (!vertn.equals(verti) && aux[vertn] <= K - 1) {
                            aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                            if (aux[vertn] == K) {
                                mustBeIncluded.add(vertn);
                            }
                        }
                    }
                    aux[verti] += K;
                }

                for (Integer z : vertices) {
                    if (aux[z] >= K
                            || z.equals(x)
                            || z.equals(y)) {
                        continue;
                    }
                    int contz = contadd;
                    int[] auxb = (int[]) aux.clone();
                    mustBeIncluded.add(z);
                    auxb[z] = K;
                    while (!mustBeIncluded.isEmpty()) {
                        Integer verti = mustBeIncluded.remove();
                        contz++;
                        Collection<Integer> neighbors = graphRead.getNeighborsUnprotected(verti);
                        for (Integer vertn : neighbors) {
                            if (vertn.equals(verti)) {
                                continue;
                            }
                            if (!vertn.equals(verti) && auxb[vertn] <= K - 1) {
                                auxb[vertn] = auxb[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                                if (auxb[vertn] == K) {
                                    mustBeIncluded.add(vertn);
                                }
                            }
                        }
                        auxb[verti] += K;
                    }

                    if (contz == sizeKeep) {
                        if (verbose) {
                            System.out.println("Reduzido removido: " + x + " " + y + " adicionado " + z);
                            System.out.println("Na posição " + cont + "/" + (tmp.size() - 1));
                        }
                        t.add(z);
                        s = t;
                        ltmp = new ArrayList<>(s);
//                        h--;
                        h = 0;
                        continue for_p;
                    }
                }

            }

        }
        return s;
    }

    public boolean checkIfHullSet(UndirectedSparseGraphTO<Integer, Integer> graph,
            Iterable<Integer> currentSet) {
        if (currentSet == null) {
            return false;
        }
        Queue<Integer> mustBeIncluded = new ArrayDeque<>();

        Set<Integer> fecho = new HashSet<>();

        int vertexCount = graph.getVertexCount();
        if (kr == null || kr.length < vertexCount) {
            initKr(graph);
        }
//        if (marjority != null) {
//            throw new IllegalStateException("implementar");
//        }

        int[] aux = new int[(Integer) graph.maxVertex() + 1];
        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
            if (kr[i] == 0) {
                mustBeIncluded.add(i);
            }
        }

        for (Integer iv : currentSet) {
            Integer v = iv;
            mustBeIncluded.add(v);
            aux[v] = kr[v];
        }
        while (!mustBeIncluded.isEmpty()) {
            Integer verti = mustBeIncluded.remove();
            Collection<Integer> neighbors = graph.getNeighborsUnprotected(verti);
            for (Integer vertn : neighbors) {
                if (vertn.equals(verti)) {
                    continue;
                }
                if ((++aux[vertn]) == kr[vertn]) {
                    mustBeIncluded.add(vertn);
                }
            }
            fecho.add(verti);
            aux[verti] += kr[verti];
        }
        return fecho.size() == graph.getVertexCount();
    }
}
