/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import static com.github.braully.graph.operation.GraphCaratheodoryCheckSet.NEIGHBOOR_COUNT_INCLUDED;
import static com.github.braully.graph.operation.GraphHNVOptm.getDescription;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import util.BFSUtil;
import util.MapCountOpt;

/**
 *
 * @author strike
 */
public abstract class AbstractHeuristicOptm extends AbstractHeuristic {

    public static final String pdeltaHsixdificuldadeTotal = "deltaHXdifTotal";

    public static final String pdeltaHsi = "deltaHsi";
    public static final String pdeltaParcial = "deltaParcial";
    public static final String pbonusTotal = "bonusTotal";
    public static final String pbonusParcial = "bonusParcial";
    public static final String pdificuldadeTotal = "dificuldadeTotal";
    public static final String pdificuldadeParcial = "dificuldadeParcial";
    public static final String pbonusTotalNormalizado = "bonusTotalNormalizado";
    public static final String pbonusParcialNormalizado = "bonusParcialNormalizado";
    public static final String pprofundidadeS = "profundidadeS";
    public static final String pgrau = "grau";
    public static final String paux = "aux";

    public static final List<String> allParameters = List.of(pdeltaHsi, pbonusTotal,
            pbonusParcial, pdificuldadeTotal, pdificuldadeParcial,
            pbonusTotalNormalizado, pbonusParcialNormalizado,
            pprofundidadeS, pgrau, paux, pdeltaParcial);

    public boolean realizarPoda = false;
    protected boolean pularAvaliacaoOffset = false;
    protected int[] pularAvaliacao = null;
    protected int[] degree = null;

    protected Set<Integer> verticesTrabalho;
    protected Set<Integer> podados;
    protected int[] graup;
    protected int[] krp;
    protected BFSUtil bdlhs;
    protected BFSUtil bdls;
    Set<Integer> graphCenter;

//    @Override
    public void resetParameters() {
        this.parameters.clear();
    }

    public void setParameter(String p, boolean b) {
        this.parameters.put(p, b);
    }

    public void setPularAvaliacaoOffset(boolean b) {
        this.pularAvaliacaoOffset = true;
    }

    public boolean sortByDegree = false;

    public void setSortByDegree(boolean sortByDegree) {
        this.sortByDegree = sortByDegree;
    }

    @Override
    public String getName() {
        StringBuilder sb = new StringBuilder(getDescription());
        for (String par : parameters.keySet()) {
            Boolean get = parameters.get(par);
            if (get != null) {
                if (get) {
                    sb.append("+");
                } else {
                    sb.append("-");
                }
                sb.append(par);
            }
        }
        if (sortByDegree) {
            sb.append(":sort");
        }
        if (realizarPoda) {
            sb.append(":poda");
        }
        if (pularAvaliacaoOffset) {
            sb.append(":pularAva");
        }
        if (tryMiminal()) {
            sb.append(":tryMinimal");
        }
        if (tryMiminal2()) {
            sb.append(":tryMinimal2");
        }
        return sb.toString();
    }

    public int addVertToAux(Integer verti,
            UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux) {
        int countIncluded = 0;
        if (verti == null) {
            return countIncluded;
        }
        if (kr[verti] > 0 && aux[verti] >= kr[verti]) {
            return countIncluded;
        }

        aux[verti] = aux[verti] + kr[verti];
        mustBeIncluded.clear();
        mustBeIncluded.add(verti);
        while (!mustBeIncluded.isEmpty()) {
            verti = mustBeIncluded.remove();
            Collection<Integer> neighbors = graph.getNeighborsUnprotected(verti);
            for (Integer vertn : neighbors) {
                if ((++aux[vertn]) == kr[vertn]) {
                    mustBeIncluded.add(vertn);
                }
            }
            countIncluded++;
        }

        return countIncluded;
    }

    public int addVertToS(Integer verti, Set<Integer> s,
            UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux) {
        int countIncluded = 0;
        if (verti == null) {
            return countIncluded;
        }
        if (kr[verti] > 0 && aux[verti] >= kr[verti]) {
            return countIncluded;
        }

        aux[verti] = aux[verti] + kr[verti];
        if (s != null) {
            s.add(verti);
            Collection<Integer> neighbors = graph.getNeighborsUnprotected(verti);
            for (Integer vertn : neighbors) {
                if ((++scount[vertn]) == kr[vertn] && s.contains(vertn)) {
                    if (verbose) {
                        System.out.println("Scount > kr: " + vertn + " removendo de S ");
                    }
                    s.remove(vertn);
                    Collection<Integer> nn = getNeighborsNaoPodados(graph, vertn);
                    for (Integer vnn : nn) {
                        scount[vnn]--;
                    }
                }
            }
        }
        mustBeIncluded.clear();
        mustBeIncluded.add(verti);
        while (!mustBeIncluded.isEmpty()) {
            verti = mustBeIncluded.remove();
            Collection<Integer> neighbors = graph.getNeighborsUnprotected(verti);
            for (Integer vertn : neighbors) {
                if ((++aux[vertn]) == kr[vertn]) {
                    mustBeIncluded.add(vertn);
                }
            }
            countIncluded++;
        }

        return countIncluded;
    }

    public Set<Integer> tipDecomp(UndirectedSparseGraphTO graph) {
        Set<Integer> S = new LinkedHashSet<>(graph.getVertices());
//        initKr(graph);
        int n = (Integer) graph.maxVertex() + 1;

        int[] delta = new int[n];
        int[] k = new int[n];
        int[] dist = new int[n];

        Set<Integer>[] N = new Set[n];

        for (Integer v : S) {
            delta[v] = graph.degree(v);
            k[v] = kr[v];
            N[v] = new LinkedHashSet<>(graph.getNeighborsUnprotected(v));
            dist[v] = delta[v] - kr[v];
        }

        boolean flag = true;

        while (flag) {
            Integer v = null;
            for (var vi : S) {
                if (v == null) {
                    v = vi;
                } else if (dist[v] > dist[vi]) {
                    v = vi;
                }
            }
            if (dist[v] == Integer.MAX_VALUE) {
                flag = false;
            } else {
                S.remove(v);
                for (Integer u : N[v]) {
                    if (dist[u] > 0) {
                        dist[u]--;
                    } else {
                        dist[u] = Integer.MAX_VALUE;
                    }
                    N[u].remove(v);
                }
            }
        }
//        S = tryMinimal(graph, S);
        return S;
    }

//    @Override
    public Set<Integer> podaBasica(UndirectedSparseGraphTO graph) {
        podados = new LinkedHashSet<>();
        Set<Integer> verticesTrabalhados = new LinkedHashSet<>(verticesTrabalho);
        //        initKr(graph);
        int n = (Integer) graph.maxVertex() + 1;

        int[] dist = new int[n];

        Set<Integer>[] N = new Set[n];

        for (Integer v : verticesTrabalhados) {
            N[v] = new LinkedHashSet<>(getNeighborsNaoPodados(graph, v));
            dist[v] = graup[v] - krp[v];
        }

        boolean flag = true;

        while (!verticesTrabalhados.isEmpty() && flag) {
            Integer v = null;
            Double maiorRanking = null;
            for (var vi : verticesTrabalhados) {
                int entropia = graup[vi] - krp[vi];
                if (entropia == 0 && degree[vi] == graup[vi]) {
                    //                double rankvi = (graup[vi] - krp[vi]) * (bdlhs.getDistance(graph, vi) + 1);
                    Integer distance = bdlhs.getDistance(graph, vi);
//                double rankvi = (distance + 1) / Math.max(1, entropia);
                    double rankvi = distance;
                    if (v == null) {
                        v = vi;
                        maiorRanking = rankvi;
                    } else if (rankvi > maiorRanking) {
                        v = vi;
                        maiorRanking = rankvi;
                    }
                }
            }
            if (v == null) {
                flag = false;
            } else {
                if (verbose) {
                    System.out.println("Removendo vertice de baixa qualidade: " + v);
                }
                for (Integer u : N[v]) {
                    graup[u]--;
                    N[u].remove(v);
                }
                verticesTrabalho.remove(v);
                podados.add(v);
            }
            verticesTrabalhados.remove(v);
        }
//        S = tryMinimal(graph, S);
        return verticesTrabalho;
    }

    public Set<Integer> podaProfunda(UndirectedSparseGraphTO graph) {
        podados = new LinkedHashSet<>();
        Set<Integer> verticesTrabalhados = new LinkedHashSet<>(verticesTrabalho);
        //        initKr(graph);
        int n = (Integer) graph.maxVertex() + 1;

        int[] dist = new int[n];

        Set<Integer>[] N = new Set[n];

        for (Integer v : verticesTrabalhados) {
            N[v] = new LinkedHashSet<>(getNeighborsNaoPodados(graph, v));
            dist[v] = graup[v] - krp[v];
        }

        boolean flag = true;
        int nivelPoda = 0;
        while (!verticesTrabalhados.isEmpty() && flag) {
            Integer v = null;
            Double maiorRanking = null;
            for (var vi : verticesTrabalhados) {
                int podas = degree[vi] - graup[vi];
                int entropia = graup[vi] - krp[vi];
                if (entropia == 0 && podas <= nivelPoda) {
                    //                double rankvi = (graup[vi] - krp[vi]) * (bdlhs.getDistance(graph, vi) + 1);
                    Integer distance = bdlhs.getDistance(graph, vi);
//                double rankvi = (distance + 1) / Math.max(1, entropia);
//                    double rankvi = distance;
                    double rankvi = -graup[vi];
                    if (v == null) {
                        v = vi;
                        maiorRanking = rankvi;
                    } else if (rankvi > maiorRanking) {
                        v = vi;
                        maiorRanking = rankvi;
                    }
                }
            }
            if (v == null) {
                if (nivelPoda == 0) {
                    nivelPoda = 100;
                    continue;
                }
                flag = false;
            } else {
                if (verbose) {
                    System.out.println("Removendo vertice de baixa qualidade: " + v);
                }
                for (Integer u : N[v]) {
                    graup[u]--;
                    N[u].remove(v);
                }
                verticesTrabalho.remove(v);
                podados.add(v);
            }
            verticesTrabalhados.remove(v);
        }
//        S = tryMinimal(graph, S);
        return verticesTrabalho;
    }

    protected Collection<Integer> getNeighborsNaoPodados(UndirectedSparseGraphTO<Integer, Integer> graph, Integer v) {
        Collection<Integer> neighborsUnprotected = graph.getNeighborsUnprotected(v);
        if (!realizarPoda) {
            return neighborsUnprotected;
        }
        Collection<Integer> nvs = new LinkedHashSet<>(neighborsUnprotected);
        if (podados != null) {
            nvs.removeAll(podados);
        }
        return nvs;
    }

    public void setRealizarPoda(boolean realizarPoda) {
        this.realizarPoda = realizarPoda;
    }

    protected void findCenterOfGraph(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Collection<Integer> vertices = graph.getVertices();
        int vertexCount = (Integer) graph.maxVertex() + 1;
        bdlhs = BFSUtil.newBfsUtilSimple(vertexCount + 1);
        bdls = BFSUtil.newBfsUtilSimple(vertexCount + 1);
        findCenterOfGraph(vertices, graph);
    }

    protected void findCenterOfGraph(Collection<Integer> vertices, UndirectedSparseGraphTO<Integer, Integer> graph) {

        while (bdlhs.discored < vertices.size()) {
            Integer maior = null;
            Integer menor = null;
            for (Integer v : vertices) {
                if (bdlhs.getDistance(graph, v) == null) {
                    if (maior == null) {
                        maior = v;
                        menor = v;
                    } else {
                        int dv = graph.degree(v);
                        if (dv > graph.degree(maior)) {
                            maior = v;
                        }
                        if (dv < graph.degree(menor)) {
                            menor = v;
                        }
                    }
                }
            }
            bdlhs.incBfs(graph, maior);
//            bdls.incBfs(graph, menor);
//            Integer maiorDistanciaDoMenor = 0;
//            Integer vMaiorDistanciaDoMenor = null;
//            Integer maiorDistanciaDoMaior = 0;
//            Integer vMaiorDistanciaDoMaior = null;
//            for (Integer v : vertices) {
//                if (vMaiorDistanciaDoMenor == null) {
//                    vMaiorDistanciaDoMenor = v;
//                    maiorDistanciaDoMenor = bdls.getDistance(graph, v);
//                    vMaiorDistanciaDoMaior = v;
//                    maiorDistanciaDoMaior = bdlhs.getDistance(graph, v);
//                } else {
//                    Integer distV = bdls.getDistance(graph, v);
//                    if (distV > maiorDistanciaDoMenor) {
//                        maiorDistanciaDoMenor = distV;
//                        vMaiorDistanciaDoMenor = v;
//                    }
//                    Integer distV1 = bdlhs.getDistance(graph, v);
//                    if (distV1 > vMaiorDistanciaDoMaior) {
//                        maiorDistanciaDoMaior = distV1;
//                        vMaiorDistanciaDoMaior = v;
//                    }
//                }
//            }
//
//            System.out.println("Maior distancia de " + menor + ": " + maiorDistanciaDoMenor);
//            System.out.println("Maior distancia de " + maior + ": " + maiorDistanciaDoMaior);
        }
    }

    protected double maiorIndiceCentralidade = 0;
    protected boolean esgotado = false;
    protected int bestVertice = -1;
    protected int maiorGrau = 0;
    protected int maiorGrauContaminacao = 0;
    protected int maiorDeltaHs = 0;
    protected int maiorContaminadoParcialmente = 0;
    protected double maiorBonusParcialNormalizado = 0.0;
    protected double maiorDificuldadeTotal = 0;
    protected double maiorBonusTotal = 0;
    protected double maiorBonusTotalNormalizado = 0;
    protected double maiorBonusParcial = 0;
    protected double maiorDificuldadeParcial = 0;
    protected int maiorProfundidadeS = 0;
    protected int maiorProfundidadeHS = 0;
    protected int maiorAux = 0;
    protected double maiorDouble = 0;
    protected Queue<Integer> mustBeIncluded = new ArrayDeque<>();
    protected MapCountOpt mapCount;
    protected List<Integer> melhores = new ArrayList<Integer>();

    public Set<Integer> tryMinimal2Lite(UndirectedSparseGraphTO<Integer, Integer> graphRead,
            Set<Integer> tmp) {
        int contVizinhoComum = 0;
        int contSemVizinhoComum = 0;
        Set<Integer> s = tmp;
        List<Integer> ltmp = new ArrayList<>(tmp);
        if (verbose) {
            System.out.println("tentando reduzir-2-lite: " + s.size());
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
            Collection<Integer> nsY = graphRead.getNeighborsUnprotected(x);
            for (int j = h + 1; j < ltmp.size(); j++) {
                Integer y = ltmp.get(j);
                Collection<Integer> nsX = graphRead.getNeighborsUnprotected(y);
                boolean xydisjoint = Collections.disjoint(nsX, nsY);
                if (graphRead.degree(y) < kr[y]
                        || y.equals(x)
                        || !s.contains(y)
                        || xydisjoint) {
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

                mustBeIncluded.clear();
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
//                Set<Integer> verticesTest = new LinkedHashSet<>(nsX);
//                verticesTest.retainAll(nsY);
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
                        if (cont > (tmp.size() / 2) && grafoconexo) {
                            System.out.println("Poda dupla removido:  " + x + "," + y + " realizada depois de 50% " + cont + "/" + (tmp.size() - 1));

                        }
                        t.add(z);
                        s = t;
                        ltmp = new ArrayList<>(s);
//                        h--;
                        h = 0;
//                        h = h / 2;
                        if (Collections.disjoint(graphRead.getNeighborsUnprotected(x), graphRead.getNeighborsUnprotected(y))) {
                            contSemVizinhoComum++;
                        } else {
                            contVizinhoComum++;
                        }
                        continue for_p;
                    }
                }

            }
            cont++;

        }
        if (contVizinhoComum != 0 || contSemVizinhoComum != 0) {
            System.out.println("Minimal: sem vizinhos comum " + contSemVizinhoComum + " com vizinhos comuns " + contVizinhoComum);
        }
        return s;
    }

    public Set<Integer> tryMinimal2Medium(UndirectedSparseGraphTO<Integer, Integer> graphRead,
            Set<Integer> tmp) {
        int contVizinhoComum = 0;
        int contSemVizinhoComum = 0;
        Set<Integer> s = tmp;
        List<Integer> ltmp = new ArrayList<>(tmp);
        if (verbose) {
            System.out.println("tentando reduzir-2-lite: " + s.size());
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
            Collection<Integer> nsY = graphRead.getNeighborsUnprotected(x);
            for (int j = h + 1; j < ltmp.size(); j++) {
                Integer y = ltmp.get(j);
                Collection<Integer> nsX = graphRead.getNeighborsUnprotected(y);
                boolean xydisjoint = Collections.disjoint(nsX, nsY);
                if (graphRead.degree(y) < kr[y]
                        || y.equals(x)
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

                mustBeIncluded.clear();
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
//                Set<Integer> verticesTest = new LinkedHashSet<>(nsX);
//                verticesTest.retainAll(nsY);
                for (Integer z : vertices) {
                    if (aux[z] >= kr[z] || z.equals(x) || z.equals(y)) {
                        continue;
                    }

                    if (xydisjoint) {
                        Collection<Integer> nsZ = graphRead.getNeighborsUnprotected(z);
                        boolean zyzdisjoint = Collections.disjoint(nsZ, nsY) && Collections.disjoint(nsZ, nsX);
                        //Baixa probabilidade de otimização;
                        if (zyzdisjoint) {
                            continue;
                        }
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
                        if (cont > (tmp.size() / 2) && grafoconexo) {
                            System.out.println("Poda dupla removido:  " + x + "," + y + " realizada depois de 50% " + cont + "/" + (tmp.size() - 1));

                        }
                        t.add(z);
                        s = t;
                        ltmp = new ArrayList<>(s);
//                        h--;
                        h = 0;
//                        h = h / 2;
                        if (Collections.disjoint(graphRead.getNeighborsUnprotected(x), graphRead.getNeighborsUnprotected(y))) {
                            contSemVizinhoComum++;
                        } else {
                            contVizinhoComum++;
                        }
                        continue for_p;
                    }
                }

            }
            cont++;

        }
        if (contVizinhoComum != 0 || contSemVizinhoComum != 0) {
            System.out.println("Minimal: sem vizinhos comum " + contSemVizinhoComum + " com vizinhos comuns " + contVizinhoComum);
        }
        if (s.size() != tmp.size()) {
            System.out.println("minimal lite: " + tmp.size() + "/" + s.size());
        }
        return s;
    }

    protected boolean isGreaterSimple(int... compare) {
        boolean ret = false;
        int i = 0;
        while (i < compare.length - 2
                && compare[i] == compare[i + 1]) {
            i += 2;
        }
        if (i <= compare.length - 2 && compare[i] > compare[i + 1]) {
            ret = true;
        }
        return ret;
    }

    public Boolean isGreater(double... compare) {
        Boolean ret = false;
        int i = 0;
        while (i < compare.length - 2 && compare[i] == compare[i + 1]) {
            i += 2;
        }
        if (i <= compare.length - 2) {
            if (compare[i] > compare[i + 1]) {
                ret = true;
            } else if (compare[i] == compare[i + 1]) {
                ret = null;
            }
        }
        return ret;
    }

    public List<Integer> getVertices(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        List<Integer> vertices = new ArrayList<>((List<Integer>) graphRead.getVertices());
        if (sortByDegree) {
            vertices.sort(Comparator
                    .comparingInt((Integer v) -> -graphRead.degree(v))
            //                .thenComparing(v -> -v)
            );
        }
        return vertices;
    }

}
