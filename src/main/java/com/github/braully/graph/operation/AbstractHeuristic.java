package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import static java.lang.Math.abs;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author Braully Rocha da Silva
 */
public abstract class AbstractHeuristic implements IGraphOperation {

    public static final String type = "Target set selection";
    public static final String PARAM_NAME_HULL_NUMBER = "number";
    public static final String PARAM_NAME_HULL_SET = "set";
    protected static Random randomUtil = new Random();

    //
    public Integer kTreshold;
    public Integer rTreshold;
    public Double percentTreshold;
    public Boolean randomTreshold;
    //
    protected int[] kr;
    protected boolean verbose;

    public static final String MINIMAL = "minimal";
    public Map<String, Boolean> parameters = new LinkedHashMap<>();
    protected int[] scount = null;

    public boolean tentarMinamilzar = false;
    public boolean tentarMinamilzar2 = false;
    protected boolean grafoconexo = true;

    protected int[] degree = null;
    protected Set<Integer>[] N = null;

    protected Queue<Integer> mustBeIncluded = new ArrayDeque<>();
    protected int[] auxb = null;

    protected int[] skip = null;

    public String getTypeProblem() {
        return type;
    }

    public void setK(Integer K) {
        this.kTreshold = K;
        this.percentTreshold = null;
        this.rTreshold = null;
        this.randomTreshold = null;
    }

    public void setR(Integer R) {
        this.rTreshold = R;
        this.kTreshold = null;
        this.percentTreshold = null;
        this.randomTreshold = null;
    }

    public void setPercent(Double marjority) {
        this.percentTreshold = marjority;
        this.kTreshold = null;
        this.rTreshold = null;
        this.randomTreshold = null;
    }

    public void setKr(int[] kr) {
        this.kr = kr;
    }

    public int[] getKr() {
        return kr;
    }

    public void setRandomKr(UndirectedSparseGraphTO<Integer, Integer> graph) {
        int vertexCount = (Integer) graph.maxVertex() + 1;
        kr = new int[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            int degree = graph.degree(i);
            if (degree > 0) {
                int random = random(degree);
                kr[i] = random;
            } else {
                kr[i] = degree;
            }
        }
    }

    public void initKr(UndirectedSparseGraphTO graph) {
        if (rTreshold != null || kTreshold != null || percentTreshold != null) {
            int vertexCount = (Integer) graph.maxVertex() + 1;
            kr = new int[vertexCount];
            for (int i = 0; i < vertexCount; i++) {
                int degree = graph.degree(i);
                if (rTreshold != null) {
                    kr[i] = Math.min(rTreshold, graph.degree(i));
                } else if (kTreshold != null) {
                    kr[i] = kTreshold;
                } else if (percentTreshold != null) {
                    //                kr[i] = roundUp(degree, majority
//                    double ddgree = degree;
//                    double ki = percentTreshold * ddgree;
                    double ki = Math.ceil(percentTreshold * degree);
                    int kii = (int) ki;
                    kr[i] = kii;
                } else if (randomTreshold != null) {
                    if (degree > 0) {
                        int random = random(degree);
                        kr[i] = random;
                    } else {
                        kr[i] = degree;
                    }
                }
            }
        }
    }

    public abstract String getDescription();

    protected boolean refine = false;
    protected boolean refine2 = false;

    public void setRefine(boolean refine) {
        this.refine = refine;
    }

    public void setRefine2(boolean refine2) {
        this.refine2 = refine2;
    }

    public String getName() {
        StringBuilder sb = new StringBuilder(getDescription());
        if (refine) {
            sb.append("-rf1");
        }
        if (refine2) {
            sb.append("-rf2");
        }
        return sb.toString();
    }

    public Set<Integer> refineResult(UndirectedSparseGraphTO<Integer, Integer> graph, Set<Integer> s, int targetSize) {
        if (refine) {
            s = refineResultStep1(graph, s, targetSize);
        }
        if (refine2) {
            s = refineResultStep2(graph, s, targetSize);
        }
        return s;
    }

    public Set<Integer> refineResultStep1(UndirectedSparseGraphTO<Integer, Integer> graphRead,
            Set<Integer> tmp, int tamanhoAlvo) {
        Set<Integer> s = new LinkedHashSet<>(tmp);

        for (Integer v : tmp) {
            Collection<Integer> nvs = N[v];
            int scont = 0;
            for (Integer nv : nvs) {
                if (s.contains(nv)) {
                    scont++;
                }
            }
            if (scont >= kr[v]) {
                s.remove(v);
            }
        }
        return s;
    }

    public Set<Integer> refineResultStep2(UndirectedSparseGraphTO<Integer, Integer> graphRead,
            Set<Integer> tmp, int tamanhoAlvo) {
        Set<Integer> s = tmp;

        if (s.size() <= 1) {
            return s;
        }

        if (verbose) {
            System.out.println("tentando reduzir: " + s.size());
//            System.out.println("s: " + s);
        }
        int cont = 0;
        for (Integer v : tmp) {
            cont++;
            if (graphRead.degree(v) < kr[v]) {
                continue;
            }
            Set<Integer> t = new LinkedHashSet<>(s);
            t.remove(v);

            int contadd = 0;
            int[] aux = auxb;

            for (int i = 0; i < aux.length; i++) {
                aux[i] = 0;
            }

            mustBeIncluded.clear();
            for (Integer iv : t) {
                mustBeIncluded.add(iv);
                aux[iv] = kr[iv];
            }
            while (!mustBeIncluded.isEmpty()) {
                Integer verti = mustBeIncluded.remove();
                contadd++;
                Collection<Integer> neighbors = N[verti];
                for (Integer vertn : neighbors) {
                    if (aux[vertn] <= kr[vertn] - 1) {
                        aux[vertn] = aux[vertn] + 1;
                        if (aux[vertn] == kr[vertn]) {
                            mustBeIncluded.add(vertn);
                        }
                    }
                }
                aux[verti] += kr[verti];
            }

            if (contadd >= tamanhoAlvo) {
                if (verbose) {
                    System.out.println(" - removido: " + v + " na pos " + cont + "/" + s.size() + " det " + v + ": " + degree[v]
                            + "/" + kr[v] + " " + ((float) kr[v] * 100 / (float) degree[v]));

                }
                s = t;
            }
        }
        if (verbose) {
            int delt = tmp.size() - s.size();
            if (delt > 0) {
                System.out.println(tmp.size() + "/" + s.size() + " removido " + delt + " vertices");
            }
        }
        return s;
    }

    public static int random(int num) {
        //Probability ignored, for future use, , Integer probability
        return randomUtil.nextInt(num);
    }

    public static int roundUp(int num, int divisor) {
        int sign = (num > 0 ? 1 : -1) * (divisor > 0 ? 1 : -1);
        return sign * (abs(num) + abs(divisor) - 1) / abs(divisor);
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean checkIfHullSet(UndirectedSparseGraphTO<Integer, Integer> graph,
            Iterable<Integer> currentSet) {
        if (currentSet == null) {
            return false;
        }
        Queue<Integer> mustBeIncluded = new ArrayDeque<>();

        Set<Integer> fecho = new HashSet<>();

        Collection<Integer> vertices = graph.getVertices();

        int vertexCount = graph.getVertexCount();
        int maxVertex = graph.maxVertex();
//        if (kr == null || kr.length < maxVertex + 1) {
        initKr(graph);
//        }
        int[] aux = new int[maxVertex + 1];
        for (Integer i : vertices) {
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
        return fecho.size() == vertexCount;
    }

    public Set<Integer> tryMinimal(UndirectedSparseGraphTO<Integer, Integer> graphRead,
            Set<Integer> tmp) {
        Set<Integer> s = tmp;
        if (verbose) {
            System.out.println("tentando reduzir: " + s.size());
//            System.out.println("s: " + s);
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
//                if (verbose) {
//                System.out.println("Reduzido removido: " + v + " na posição " + cont + "/" + (tmp.size() - 1));
//                }
                if (cont > (tmp.size() / 2) && grafoconexo) {
//                    System.out.println("Poda de v:  " + v + " realizada depois de 50% em grafo conexo " + cont + "/" + (tmp.size() - 1));
//                    System.out.println(" - Detalhes de v: "
//                            + v + " degree: " + graphRead.degree(v) + " scount: "
//                            + scount[v] + " kr:" + kr[v]);
                }
            }
        }
        if (verbose) {
            System.out.println("reduzido para: " + s.size());
//            System.out.println("s: " + s);
        }
        return s;
    }

    public Set<Integer> tryMinimal2(UndirectedSparseGraphTO<Integer, Integer> graphRead,
            Set<Integer> tmp) {
        Set<Integer> s = tmp;
        List<Integer> ltmp = new ArrayList<>(tmp);
        if (verbose) {
            System.out.println("tentando reduzir-2: " + s.size());
//            System.out.println("s: " + s);
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
                            aux[vertn] = aux[vertn] + 1;
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
                                auxb[vertn] = auxb[vertn] + 1;
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
                            if (scount != null) {
                                Collection<Integer> nsX = graphRead.getNeighborsUnprotected(y);
                                Collection<Integer> nsY = graphRead.getNeighborsUnprotected(x);
                                Collection<Integer> nsZ = graphRead.getNeighborsUnprotected(z);
                                System.out.print("Count s: " + x + ": "
                                        + scount[x] + " " + y + ": " + scount[y]
                                        + " adicionado " + z + ": " + scount[z]);
                                if (Collections.disjoint(nsX, nsY)) {
                                    System.out.print(" ... é disjunto");
                                } else {
                                    System.out.print(" ... tem vizinhos comuns");
                                }
                                if (Collections.disjoint(nsZ, nsY) && Collections.disjoint(nsZ, nsX)) {
                                    System.out.println(" ... z é independente");
                                } else {
                                    System.out.println(" ... z intercept x ou y");
                                }
                            }
                            System.out.println("Na posição " + cont + "/" + (tmp.size() - 1));
                        }
                        if (cont > (tmp.size() / 2) && grafoconexo) {
                            System.out.println("Poda dupla em grafo conexo removido:  " + x + "," + y + " realizada depois de 50% " + cont + "/" + (tmp.size() - 1));
                            System.out.println(" - Detalhes de v: "
                                    + x + " degree: " + graphRead.degree(x) + " scount: "
                                    + scount[x] + " kr:" + kr[x]);
                            System.out.println(" - Detalhes de v: "
                                    + y + " degree: " + graphRead.degree(y) + " scount: "
                                    + scount[y] + " kr:" + kr[y]);
                        }
                        t.add(z);
                        s = t;
                        ltmp = new ArrayList<>(s);
//                        h--;
                        h = 0;
//                        h = h / 2;
                        continue for_p;
                    }
                }

            }
            cont++;

        }
        return s;
    }

    public void setTryMinimal() {
        tentarMinamilzar = true;
    }

    public void setTryMinimal2() {
        tentarMinamilzar2 = true;
    }

    public void setTryMinimal(boolean v) {
        tentarMinamilzar = v;
    }

    protected boolean tryMiminal() {
        return tentarMinamilzar;
    }

    protected boolean tryMiminal2() {
        return tentarMinamilzar2;
    }
}
