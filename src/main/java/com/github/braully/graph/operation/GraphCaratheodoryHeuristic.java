package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class GraphCaratheodoryHeuristic
        extends GraphCheckCaratheodorySet
        implements IGraphOperation {

    /* 
    
    verticesCandidatos <- veticesComPeloMenosDoisVizinhos(G)

melhorConjuntoCarat <- vazio

Para cada v em verticesCandidatos faça
       S    <- vazio
       aux  <- vazio
       auxP <- vazio
       promoviveis <- vazio
       
       Para i de 1 até |V| faça
           aux[i] <- 0
       Fim Para

       nv0 <- melhorVizinho(v, G, aux)       
       adicionaVerticeEmS(nv0, s, G, aux)
       promoviveis <- promoviveis U nv0
     
       nv1 <- melhorVizinho(v, G, aux)       
       adicionaVerticeEmS(nv1, s, G, aux)
       promoviveis <- promoviveis U nv1
       
       Enquanto |promovivies| > 0 faça
            p <- melhorVerticePossivelPromocao(s, v, promovivies, G, aux)
            promovivies <- promovivies - {p}
            
            excludentes <- {p, v}
            auxp <- aux
            removeVerticeDeS(p, s, G, aux)
          
            nvp0 <- melhorVizinho(p, G, aux, excludentes)
            excludentes <- excludentes U nvp0
            nvp1 <- melhorVizinho(p, G, aux, excludentes)
            
            adicionaVerticeEmS(nvp0, s, G, aux)
            adicionaVerticeEmS(nvp1, s, G, aux)
            
            Se permaneceConjuntoCaratheodory(G, s, aux, auxp) Então
               promovivies <- promovivies + {nvp0, nvp1}
            Se Não
               aux <- auxp
               S <- S - {nvp0, nvp1}
               S <- S + {p}
            Fim Se
       Fim enquanto

       Se |S| > |melhorConjuntoCarat| Então
            melhorConjuntoCarat <- S
       Fim Se
fim para
    
     */
    static final String type = "P3-Convexity";
    static final String description = "Nº Caratheodory (Heuristic v1)";

    public static final int INCLUDED = 2;
    public static final int NEIGHBOOR_COUNT_INCLUDED = 1;

    public static boolean verbose = false;
    BFSDistanceLabeler<Integer, Integer> bdl = new BFSDistanceLabeler<>();

//    public static boolean verbose = false;
    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        long totalTimeMillis = -1;

        totalTimeMillis = System.currentTimeMillis();
        Set<Integer> caratheodorySet = buildMaxCaratheodorySet(graphRead);
        totalTimeMillis = System.currentTimeMillis() - totalTimeMillis;

        /* Processar a buscar pelo caratheodoryset e caratheodorynumber */
        Map<String, Object> response = new HashMap<>();
        if (!caratheodorySet.isEmpty()) {
            graphRead.setSet(caratheodorySet);
            response = super.doOperation(graphRead);
        }
        return response;
    }

    @Override
    public String getTypeProblem() {
        return type;
    }

    @Override
    public String getName() {
        return description;
    }

    Set<Integer> buildCaratheodorySetFromPartialElement(UndirectedSparseGraphTO<Integer, Integer> graph,
            Integer v, Set<Integer> s, Set<Integer> hs) {
        int vertexCount = graph.getVertexCount();

        Set<Integer> promotable = new HashSet<>();
        int[] aux = new int[vertexCount];
        int[] auxVp = new int[vertexCount];
        Integer partial = v;

        beforeVerticePromotion(graph, v, v, aux);

        buildInitialCaratheodorySet(v, graph, s, aux);

        promotable.addAll(s);

        if (verbose) {
            printSituation(vertexCount, partial, s, aux);
        }

        while (!promotable.isEmpty()) {
            Integer vp = selectBestPromotableVertice(s, partial,
                    promotable, graph, aux);
            if (vp == null) {
                continue;
            }

//            beforeVerticePromotion(graph, vp, v, aux);
            if (verbose) {
                System.out.println("\n\tPromotable: " + promotable);
                System.out.println("\n\t* Selectd " + vp + " from priority list");
                System.out.print("V(S)        = {");
                for (int i = 0; i < aux.length; i++) {
                    System.out.printf("%2d | ", i);
                }
                System.out.println("}");

                System.out.print(String.format("Aux(%2d)    ", vp));
                printArrayAux(aux);
            }

            copyArray(auxVp, aux);
            promotable.remove(vp);
            removeVertFromS(vp, s, graph, aux);

            Integer nv0 = selectBestNeighbor(vp, graph, aux, partial, auxVp);
            addVertToS(nv0, s, graph, aux);
            Integer nv1 = selectBestNeighbor(vp, graph, aux, partial, auxVp);

            addVertToS(nv1, s, graph, aux);

            boolean checkIfCaratheodory = checkIfCaratheodrySet(auxVp, aux, s, v, vp, nv0, nv1, graph);

            if (verbose) {
                System.out.print("Auxf       ");
                printArrayAux(aux);
                printSatusVS(aux, partial, nv0, nv1, vp, s, graph);
                printDifference(auxVp, aux, graph);
                System.out.println("=========> Check Caratheodory Available: " + (checkIfCaratheodory ? "Ok" : "Erro"));
            }

            if (checkIfCaratheodory) {
                if (nv0 != null) {
                    promotable.add(nv0);
                }
                if (nv1 != null) {
                    promotable.add(nv1);
                }

                if (verbose) {
                    System.out.println("\t-- OK");
                    System.out.println("\t* Adding vertice " + nv0 + " to S");
                    System.out.println("\t* Adding vertice " + nv1 + " to S");
                    printSituation(vertexCount, partial, s, aux);
                }
            } else {
                //roll back
                if (verbose) {
                    System.out.println("\t* Roll back checkIfCaratheodory=false");
                }
                rollback(aux, auxVp, s, promotable, vp, nv0, nv1);
//                copyArray(aux, auxVp);
//                s.add(vp);
//                s.remove(nv0);
//                s.remove(nv1);
            }
        }

        int stmp[] = new int[s.size()];
        Integer cont = 0;
        for (Integer vs : s) {
            stmp[cont++] = vs;
        }
        OperationConvexityGraphResult hsp3 = hsp3aux(graph, stmp);
        Set<Integer> derivatedPartialReal = hsp3.partial;
        int[] auxReal = hsp3.auxProcessor;
        Set<Integer> convexHullReal = hsp3.convexHull;

        if (verbose) {
            if (derivatedPartialReal == null || derivatedPartialReal.isEmpty()) {
                System.out.println("============== ERRO ==================");
            } else {
                System.out.println("-------------- OK --------------------");
            }
            printFinalState(graph, partial, derivatedPartialReal, aux, convexHullReal, s, auxReal);
        }

        beforeReturnSFind(graph, s, aux);
        return s;
    }

    public void buildInitialCaratheodorySet(Integer v,
            UndirectedSparseGraphTO<Integer, Integer> graph,
            Set<Integer> s, int[] aux) {
        int vertexCount = graph.getVertexCount();

        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
        }

        if (verbose) {
            System.out.println("\n\t* Adding vertice " + v + " to parcial");
        }

        Integer nv0 = selectBestNeighbor(v, graph, aux);
        if (verbose) {
            System.out.println("\t* Adding vertice " + nv0 + " to S");
        }

        addVertToS(nv0, s, graph, aux);

        Integer nv1 = selectBestNeighbor(v, graph, aux);
        if (verbose) {
            System.out.println("\t* Adding vertice " + nv1 + " to S");
        }
        addVertToS(nv1, s, graph, aux);
    }

    private void printSatusVS(int[] aux, Integer partial, Integer nv0, Integer nv1,
            Integer vp, Set<Integer> s, UndirectedSparseGraphTO<Integer, Integer> graph) {
        System.out.print("V(S)       ");
        System.out.print(" = {");
        for (int i = 0; i < aux.length; i++) {
            if (partial.equals(i)) {
                System.out.printf(" P | ", i);
            } else if (nv0 != null && nv0.equals(i)) {
                System.out.print(" 0 | ");
            } else if (nv1 != null && nv1.equals(i)) {
                System.out.print(" 1 | ");
            } else if (vp.equals(i)) {
                System.out.print(" V | ");
            } else if (s.contains(i)) {
                System.out.print(" S | ");
            } else {
                System.out.print("   | ");
            }
        }
        System.out.println("}");

        System.out.print("D(V)       ");
        System.out.print(" = {");
        for (int i = 0; i < aux.length; i++) {
            System.out.printf("%2d | ", graph.getNeighborCount(i));
        }
        System.out.println("}");
    }

    void printFinalState(UndirectedSparseGraphTO<Integer, Integer> graph, Integer partial, Set<Integer> derivatedPartialReal, int[] aux, Set<Integer> convexHullReal, Set<Integer> s, int[] auxReal) {
        int vertexCount = graph.getVertexCount();
        System.out.print("∂H(S)       = {");
        for (int i = 0; i < vertexCount; i++) {
            if (partial != null && partial.equals(i)) {
                System.out.printf("%2d | ", i);
            } else {
                System.out.print("   | ");
            }
        }
        System.out.println("}");

        System.out.print("∂®Hs        = {");
        for (int i = 0; i < vertexCount; i++) {
            if (derivatedPartialReal != null && derivatedPartialReal.contains(i)) {
                System.out.printf("%2d | ", i);
            } else {
                System.out.print("   | ");
            }
        }
        System.out.println("}");

        System.out.print("H(S)        = {");
        for (int i = 0; i < vertexCount; i++) {
            if (aux[i] >= 2) {
                System.out.printf("%2d | ", i);
            } else {
                System.out.print("   | ");
            }
        }
        System.out.println("}");

        System.out.print("H®s         = {");
        for (int i = 0; i < vertexCount; i++) {
            if (convexHullReal.contains(i)) {
                System.out.printf("%2d | ", i);
            } else {
                System.out.print("   | ");
            }
        }
        System.out.println("}");

        System.out.print("S           = {");
        for (int i = 0; i < vertexCount; i++) {
            if (s.contains(i)) {
                System.out.printf("%2d | ", i);
            } else {
                System.out.print("   | ");
            }
        }
        System.out.println("}");

        System.out.print("Aux         = {");
        for (int i = 0; i < vertexCount; i++) {
            System.out.printf("%2d | ", aux[i]);
        }
        System.out.println("}");

        System.out.print("Aux®        = {");
        for (int i = 0; i < vertexCount; i++) {
            System.out.printf("%2d | ", auxReal[i]);
        }
        System.out.println("}");
    }

    void printArrayAux(int[] aux) {
        System.out.print(" = {");
        for (int i = 0; i < aux.length; i++) {
            System.out.printf("%2d | ", aux[i]);
        }
        System.out.println("}");
    }

    void copyArray(int[] auxtg, int[] auxsrc) {
        //Backup aux
        for (int i = 0; i < auxtg.length; i++) {
            auxtg[i] = auxsrc[i];
        }
    }

    public void printSituation(int numVertices, Integer partial, Set<Integer> s, int[] aux) {
        System.out.print("\n∂H(S)       = {");
        for (int i = 0; i < numVertices; i++) {
            if (partial != null && partial.equals(i)) {
                System.out.printf("%2d | ", i);
            } else {
                System.out.print("   | ");
            }
        }
        System.out.println("}");

        System.out.print("H(S)        = {");
        for (int i = 0; i < numVertices; i++) {
            if (aux[i] >= 2) {
                System.out.printf("%2d | ", i);
            } else {
                System.out.print("   | ");
            }
        }
        System.out.println("}");

        System.out.print("S           = {");
        for (int i = 0; i < numVertices; i++) {
            if (s.contains(i)) {
                System.out.printf("%2d | ", i);
            } else {
                System.out.print("   | ");
            }
        }
        System.out.println("}");
    }

    public void addVertToS(Integer verti, Set<Integer> s,
            UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux) {

        if (verti == null || aux[verti] >= INCLUDED) {
            return;
        }

        aux[verti] = aux[verti] + INCLUDED;
        if (s != null) {
            s.add(verti);
        }

        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        mustBeIncluded.add(verti);
        while (!mustBeIncluded.isEmpty()) {
            verti = mustBeIncluded.remove();
            Collection<Integer> neighbors = graph.getNeighbors(verti);
            for (int vertn : neighbors) {
                if (vertn == verti) {
                    continue;
                }
                if (vertn != verti && ++aux[vertn] == INCLUDED) {
                    mustBeIncluded.add(vertn);
                }
            }
        }
    }

    public void removeVertFromS(Integer verti, Set<Integer> s,
            UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux) {

        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
        }
        s.remove(verti);
        for (Integer v : s) {
            addVertToS(v, s, graph, aux);
        }
    }

    public Integer selectBestPromotableVertice(Set<Integer> s,
            Integer partial, Set<Integer> promotable,
            UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux) {
        Integer bestVertex = null;
        Integer bestRanking = null;
//        if (promotable != null) {
//            Set<Integer> removable = new HashSet<>();
        for (Integer vtmp : promotable) {
            Collection neighbors = new HashSet(graph.getNeighbors(vtmp));
            neighbors.removeAll(s);
            neighbors.remove(partial);

            for (int i = 0; i < aux.length; i++) {
                if (aux[i] >= 2) {
                    neighbors.remove(i);
                }
            }
            Integer vtmpRanking = neighbors.size();
            if (bestVertex == null || (vtmpRanking >= 2 && vtmpRanking < bestRanking)) {
                bestRanking = vtmpRanking;
                bestVertex = vtmp;
            }
//            }
//            promotable.removeAll(removable);
        }
        return bestVertex;
    }

    public Integer selectBestNeighbor(Integer v, UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux) {
        return selectBestNeighbor(v, graph, aux, v, null);
    }

    public Integer selectBestNeighbor(Integer v, UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux, Integer partial, int[] auxBackup) {
        Integer ret = null;
        Set<Integer> neighbors = new HashSet<>(graph.getNeighbors(v));
        if (partial != null) {
            neighbors.remove(partial);
        }
        neighbors.remove(v);
        Integer ranking = null;
        for (int i = 0; i < aux.length; i++) {
            if (aux[i] >= 2 || (auxBackup != null && auxBackup[i] >= 2)) {
                neighbors.remove(i);
            }
        }

        for (Integer nei : neighbors) {
            int neiRanking = aux[nei] * 100 + graph.degree(nei);
            if (ret == null || neiRanking < ranking) {
                ret = nei;
                ranking = neiRanking;
            }
        }
        return ret;
    }

    private void printDifference(int[] aux, int[] auxNv0, UndirectedSparseGraphTO graph) {
        System.out.print("F-I        ");
        System.out.print(" = {");
        for (int i = 0; i < aux.length; i++) {
            System.out.printf("%2d | ", (auxNv0[i] - aux[i]));
        }
        System.out.println("}");

        System.out.print("F-D        ");
        System.out.print(" = {");
        for (int i = 0; i < aux.length; i++) {
            System.out.printf("%2d | ", (auxNv0[i] - graph.getNeighborCount(i)));
        }
        System.out.println("}");

        System.out.print("D-F-I      ");
        System.out.print(" = {");
        for (int i = 0; i < aux.length; i++) {
            System.out.printf("%2d | ", (graph.getNeighborCount(i) - (auxNv0[i] - aux[i])));
        }
        System.out.println("}");
    }

    public boolean checkIfCaratheodrySet(int[] auxi, int[] auxf, Set<Integer> s,
            Integer v, Integer vp, Integer nv0,
            Integer nv1, UndirectedSparseGraphTO<Integer, Integer> graph) {
        if (auxf[v] < INCLUDED || auxf[vp] < INCLUDED) {
            return false;
        }
        boolean ret = true;
        int vertexCount = graph.getVertexCount();
        int[] auxc = new int[vertexCount];

        for (Integer p : s) {
            for (int j = 0; j < vertexCount; j++) {
                auxc[j] = 0;
            }
            for (Integer i : s) {
                if (!i.equals(p)) {
                    addVertToS(i, s, graph, auxc);
                }
            }
            if (auxc[v] >= INCLUDED) {
                return false;
            }
        }
        return ret;

//        int[] auxbackp = new int[auxf.length];
//
//        Set<Integer> sv = new HashSet<>();
//        Set<Integer> neighbors = new HashSet<>();
//
//        for (int i = 0; i < auxf.length; i++) {
//            int deltav = auxf[i] - auxi[i];
//            if (deltav >= INCLUDED) {
//                neighbors.addAll(graph.getNeighbors(i));
//                neighbors.add(i);
//            }
//        }
//        neighbors.retainAll(s);
//        sv.addAll(neighbors);
//
//        for (Integer i : sv) {
//            Set<Integer> sbackup = new HashSet<>(s);
//            removeVertFromS(i, sbackup, graph, auxbackp);
//            if (auxbackp[i] >= INCLUDED || auxbackp[v] >= INCLUDED) {
//                ret = false;
//                break;
//            }
//        }
//        return ret;
    }

    public Set<Integer> buildMaxCaratheodorySet(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        Set<Integer> caratheodorySet = new HashSet<>();
        Collection<Integer> vertices = graphRead.getVertices();
        for (Integer v : vertices) {
            int neighborCount = graphRead.getNeighborCount(v);
            if (graphRead.isNeighbor(v, v)) {
                neighborCount--;
            }
            if (neighborCount >= 2) {
                Set<Integer> s = new HashSet<>();
                Set<Integer> hs = new HashSet<>();
                Set<Integer> tmp = buildCaratheodorySetFromPartialElement(graphRead, v, s, hs);
                if (tmp != null && tmp.size() > caratheodorySet.size()) {
                    caratheodorySet = tmp;
                }
            }
        }
        return caratheodorySet;
    }

    public void rollback(int[] aux, int[] auxVp, Set<Integer> s, Set<Integer> promotable, Integer vp, Integer nv0, Integer nv1) {
        copyArray(aux, auxVp);
        s.add(vp);
        s.remove(nv0);
        s.remove(nv1);
        promotable.remove(nv0);
        promotable.remove(nv1);

    }

    void beforeVerticePromotion(UndirectedSparseGraphTO<Integer, Integer> graph, Integer vp, Integer v, int[] aux) {
//        bdl.labelDistances(graph, vp);
    }

    public int countSizeHs(Set<Integer> s, int[] aux) {
        int cont = 0;
        if (aux == null) {
            return 0;
        }
        for (int i = 0; i < aux.length; i++) {
            if (aux[i] >= INCLUDED) {
                cont++;
            }
        }
        return cont;
    }

    void beforeReturnSFind(UndirectedSparseGraphTO<Integer, Integer> graph, Set<Integer> s, int[] aux) {

    }
}
