package com.github.braully.graph.generator;

import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;

public class GraphGeneratorMoore extends AbstractGraphGenerator {

    static final String K_REGULAR = "K";
    static final String[] parameters = {K_REGULAR};
    static final String description = "Moore";
    static final Integer DEFAULT_NVERTICES = 2;

    static final List<Integer> ks = Arrays.asList(new Integer[]{2, 3, 7, 57});

    Queue<Integer> queue = new LinkedList<Integer>();

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
        Integer k = getIntegerParameter(parameters, K_REGULAR);

        if (k == null) {
            k = DEFAULT_NVERTICES;
        }
        UndirectedSparseGraphTO<Integer, Integer> graph = new UndirectedSparseGraphTO<>();

        if (ks.contains(k)) {
            graph.setName("K" + K_REGULAR);
            for (Integer i = 0; i < k * k + 1; i++) {
                graph.addVertex(i);
            }
            int ko = k - 2;
            int maxvert = k * k;
            for (int j = 0; j < k - 1; j++) {
                graph.addEdge(j, j + k - 1);
            }
            System.out.println();

            int offset = 2 * (k - 1);
            int join = k * (k - 1);

            int[] comb = combination(k);
            int idx = 0;

            for (int j = 0; j < k - 1; j++) {
                int u = j;
                int v = j + k - 1;
                int tu = u;
                int tv = v;
                for (int i = 0; i < k - (2 + j); i++) {
                    graph.addEdge(u, offset);
                    graph.addEdge(offset, join + comb[idx]);
                    graph.addEdge(offset++, ++tv);
                    graph.addEdge(v, offset);
                    graph.addEdge(maxvert, join + comb[idx]);
                    graph.addEdge(offset, join + comb[idx++]);
                    graph.addEdge(offset++, ++tu);
                }
            }
            join = join + ko;
            graph.addEdge(join, maxvert);
            graph.addEdge(maxvert, join + 1);
            for (int j = 0; j < k - 1; j++) {
                int u = j;
                int v = j + k - 1;
                graph.addEdge(join, u);
                graph.addEdge(join + 1, v);
            }
            completeEdges(graph, k);
        } else {
            log.log(Level.SEVERE, "Impossible graph");
        }
        return graph;
    }

    private int[] combination(Integer k) {
        //cache for result k = 57
        if (k == 57) {
            return new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 2, 53, 4, 51, 6, 49, 8, 47, 10, 45, 12, 43, 14, 41, 16, 39, 18, 37, 20, 35, 22, 33, 24, 31, 26, 29, 28, 27, 30, 25, 32, 23, 34, 21, 36, 19, 38, 17, 40, 15, 42, 13, 44, 11, 46, 9, 48, 7, 50, 5, 52, 3, 54, 1, 7, 5, 30, 0, 40, 6, 29, 11, 24, 54, 45, 9, 23, 12, 18, 19, 33, 52, 15, 20, 17, 25, 27, 51, 22, 26, 16, 31, 21, 48, 28, 32, 14, 37, 46, 47, 34, 38, 10, 43, 8, 41, 42, 44, 13, 49, 4, 36, 39, 50, 35, 3, 53, 16, 39, 14, 13, 15, 19, 42, 17, 36, 28, 27, 33, 52, 20, 6, 0, 43, 25, 38, 3, 41, 22, 5, 18, 45, 30, 37, 10, 49, 31, 29, 4, 51, 26, 32, 54, 11, 34, 50, 40, 48, 12, 23, 24, 21, 8, 1, 9, 35, 47, 46, 44, 34, 17, 22, 9, 48, 2, 35, 47, 36, 1, 27, 10, 7, 11, 24, 44, 12, 13, 6, 14, 23, 33, 28, 29, 54, 0, 39, 45, 21, 30, 46, 31, 8, 40, 25, 41, 32, 52, 49, 37, 38, 42, 20, 43, 19, 26, 15, 53, 51, 50, 18, 21, 25, 28, 24, 52, 50, 2, 6, 3, 29, 18, 36, 8, 40, 31, 33, 37, 0, 42, 49, 46, 1, 48, 32, 44, 5, 26, 54, 15, 10, 16, 43, 19, 22, 17, 11, 45, 12, 9, 27, 35, 7, 13, 53, 23, 20, 41, 14, 38, 47, 39, 31, 43, 3, 53, 37, 54, 44, 4, 38, 34, 20, 30, 33, 22, 45, 51, 27, 28, 18, 35, 52, 47, 9, 19, 10, 1, 26, 8, 48, 32, 24, 15, 16, 50, 25, 23, 36, 40, 49, 2, 7, 12, 11, 41, 46, 13, 29, 42, 36, 33, 7, 29, 28, 31, 2, 54, 44, 1, 21, 32, 46, 5, 11, 15, 34, 19, 41, 37, 20, 9, 51, 26, 24, 10, 16, 17, 3, 23, 12, 43, 47, 4, 14, 38, 30, 52, 53, 35, 0, 18, 48, 8, 45, 42, 27, 50, 49, 35, 12, 32, 29, 40, 42, 11, 50, 5, 43, 34, 10, 14, 2, 4, 47, 1, 52, 17, 3, 53, 48, 21, 45, 18, 51, 19, 44, 16, 41, 23, 20, 46, 37, 33, 26, 24, 0, 25, 30, 13, 27, 54, 22, 39, 38, 23, 40, 3, 32, 0, 36, 45, 4, 35, 6, 5, 53, 28, 37, 54, 39, 17, 27, 7, 2, 41, 42, 25, 34, 14, 11, 15, 22, 46, 44, 31, 13, 18, 21, 51, 1, 9, 10, 50, 16, 52, 26, 38, 12, 20, 30, 4, 39, 30, 49, 46, 22, 31, 0, 19, 21, 38, 44, 43, 50, 48, 53, 34, 33, 24, 45, 41, 18, 13, 47, 5, 32, 14, 51, 28, 36, 37, 40, 1, 6, 54, 17, 29, 8, 25, 16, 12, 20, 27, 26, 15, 44, 8, 38, 32, 7, 42, 33, 11, 18, 3, 39, 31, 1, 37, 47, 30, 5, 15, 22, 14, 54, 19, 41, 2, 13, 27, 48, 46, 34, 26, 6, 25, 49, 36, 52, 16, 28, 21, 0, 51, 43, 20, 23, 9, 50, 20, 17, 53, 48, 41, 22, 42, 7, 40, 18, 19, 45, 35, 25, 23, 33, 52, 24, 9, 16, 8, 29, 30, 4, 0, 5, 43, 1, 49, 31, 27, 6, 34, 21, 15, 46, 51, 38, 10, 26, 14, 13, 17, 22, 0, 3, 51, 34, 1, 21, 26, 39, 49, 25, 27, 10, 47, 20, 13, 15, 5, 35, 7, 40, 18, 42, 11, 14, 48, 44, 33, 2, 53, 46, 16, 38, 24, 9, 41, 52, 19, 23, 4, 37, 45, 21, 8, 29, 31, 32, 41, 50, 25, 10, 26, 23, 39, 51, 37, 28, 11, 6, 7, 36, 54, 53, 5, 43, 19, 30, 18, 4, 47, 52, 35, 12, 22, 46, 24, 15, 34, 42, 48, 16, 33, 39, 0, 10, 8, 28, 49, 12, 21, 6, 2, 34, 48, 31, 50, 43, 18, 51, 9, 38, 15, 25, 52, 20, 7, 13, 47, 16, 24, 11, 37, 26, 53, 40, 35, 30, 1, 3, 44, 5, 19, 46, 30, 49, 48, 8, 17, 42, 33, 3, 32, 50, 9, 41, 47, 20, 27, 51, 19, 31, 6, 25, 1, 40, 24, 5, 36, 35, 29, 2, 4, 54, 23, 37, 34, 43, 26, 28, 13, 14, 40, 10, 2, 19, 35, 29, 5, 53, 49, 11, 37, 45, 23, 43, 22, 25, 44, 33, 14, 51, 52, 24, 9, 21, 26, 15, 47, 32, 30, 28, 41, 13, 6, 54, 27, 38, 17, 12, 7, 36, 13, 32, 14, 26, 34, 12, 53, 3, 22, 50, 46, 15, 43, 39, 9, 4, 16, 54, 47, 1, 27, 37, 28, 24, 31, 38, 52, 42, 49, 44, 48, 23, 25, 45, 2, 51, 44, 41, 36, 16, 52, 15, 38, 46, 1, 39, 13, 35, 29, 21, 42, 27, 28, 53, 9, 45, 25, 12, 14, 17, 48, 54, 47, 26, 50, 20, 23, 5, 4, 2, 3, 47, 10, 9, 11, 8, 29, 16, 15, 49, 7, 35, 30, 3, 0, 26, 12, 40, 23, 13, 4, 54, 39, 17, 50, 41, 27, 25, 38, 45, 24, 14, 22, 53, 37, 6, 52, 24, 2, 16, 43, 45, 30, 4, 54, 32, 50, 46, 48, 36, 23, 37, 39, 0, 6, 9, 29, 34, 18, 28, 40, 51, 17, 42, 27, 11, 14, 1, 31, 26, 53, 0, 46, 4, 3, 43, 19, 34, 2, 7, 8, 23, 1, 9, 49, 33, 16, 15, 29, 27, 18, 25, 47, 48, 6, 30, 31, 5, 42, 24, 54, 51, 36, 30, 5, 45, 47, 44, 40, 46, 23, 20, 41, 27, 19, 28, 12, 50, 26, 35, 38, 8, 54, 7, 13, 1, 4, 16, 11, 10, 49, 32, 34, 52, 48, 38, 40, 20, 12, 18, 21, 52, 28, 37, 3, 13, 43, 31, 17, 8, 46, 48, 15, 45, 22, 53, 51, 32, 44, 47, 39, 36, 29, 9, 7, 35, 9, 21, 13, 51, 18, 50, 36, 44, 6, 43, 42, 33, 30, 11, 32, 35, 1, 7, 14, 0, 20, 15, 10, 41, 54, 29, 17, 40, 12, 4, 0, 42, 7, 19, 54, 16, 52, 10, 37, 24, 6, 31, 36, 3, 30, 2, 11, 39, 22, 28, 50, 20, 38, 14, 44, 13, 21, 48, 8, 36, 46, 33, 6, 19, 14, 51, 24, 44, 7, 2, 49, 42, 41, 23, 32, 13, 15, 5, 31, 4, 54, 12, 40, 8, 43, 9, 17, 39, 49, 16, 34, 4, 54, 35, 38, 50, 21, 32, 10, 14, 11, 6, 8, 24, 25, 1, 2, 22, 53, 18, 0, 19, 40, 41, 38, 8, 23, 48, 42, 52, 26, 21, 35, 10, 29, 12, 5, 53, 34, 14, 36, 17, 6, 43, 31, 13, 44, 0, 11, 25, 40, 42, 36, 11, 6, 8, 24, 14, 35, 27, 17, 48, 20, 26, 3, 2, 12, 5, 15, 32, 25, 4, 16, 1, 10, 38, 12, 4, 22, 7, 34, 44, 27, 28, 31, 51, 36, 45, 9, 47, 33, 53, 0, 3, 37, 1, 17, 49, 29, 11, 53, 12, 0, 17, 13, 29, 8, 52, 47, 43, 2, 4, 14, 41, 1, 44, 46, 3, 37, 39, 33, 40, 2, 20, 17, 47, 42, 6, 26, 24, 38, 0, 40, 30, 50, 5, 33, 39, 22, 53, 49, 15, 18, 27, 45, 1, 13, 9, 12, 52, 46, 28, 50, 20, 49, 37, 40, 22, 5, 17, 31, 25, 24, 35, 43, 49, 53, 28, 50, 25, 3, 44, 48, 16, 7, 32, 39, 18, 27, 38, 47, 30, 41, 0, 23, 11, 22, 20, 50, 33, 10, 39, 54, 29, 41, 34, 52, 2, 40, 45, 21, 5, 47, 46, 45, 1, 54, 0, 35, 41, 38, 18, 29, 3, 9, 20, 2, 10, 48, 30, 15, 39, 3, 5, 36, 34, 26, 10, 8, 15, 49, 27, 6, 25, 4, 18, 29, 41, 7, 18, 53, 21, 51, 23, 39, 31, 48, 45, 33, 4, 30, 2, 37, 42, 52, 51, 53, 44, 0, 20, 7, 37, 19, 14, 33, 2, 12, 49, 21, 22, 7, 19, 28, 45, 22, 8, 39, 23, 42, 16, 6, 2, 43, 49, 22, 3, 17, 13, 30, 54, 32, 19, 0, 9, 31, 24, 20, 4, 10, 3, 27, 29, 52, 9, 46, 16, 33, 30, 5, 5, 21, 42, 12, 1, 35, 32, 15, 46, 19, 31, 19, 23, 34, 51, 43, 21, 33, 50, 25, 16, 43, 11, 10, 18, 33, 39, 8, 6, 0, 14, 26, 45, 19, 36, 18, 44, 11, 3, 37, 35, 31, 32, 36, 51, 29, 17, 40, 36, 28, 34, 28, 47, 7, 8, 21, 7, 6, 22, 24, 11, 34, 28, 10, 45, 32};
        }
        int ko = k - 2;
        int len = ((ko + 1) * ko) / 2;
        int arr[] = new int[len];
        int arrup[] = new int[len];
        int arrdown[] = new int[len];
        int[] countpos = new int[len];
        int[] countval = new int[ko];

        int max_val_count = 0;
        if (ko != 0) {
            max_val_count = len / ko;
        }

        Map<Integer, List<Integer>> possibilidades = new HashMap<>(len);
        Integer[] targetv = new Integer[ko];

        for (int j = 0; j < ko; j++) {
            targetv[j] = j;
            countval[j]++;
        }

        int offsetup = ko - 1;
        int up = 0;
        int down = 1;
        for (int i = 0; i < len; i++) {
            arr[i] = -1;
            arrup[i] = up;
            arrdown[i] = down++;
            if (i == offsetup) {
                up++;
                offsetup += (ko - up);
            }
            if (down == ko + 1) {
                down = up + 1;
            }
        }

        for (int i = 0; i < ko; i++) {
            countpos[i] = 0;
            arr[i] = i;
            List<Integer> listaPossiveis = new ArrayList<>(len);
            listaPossiveis.addAll(Arrays.asList(targetv));
            possibilidades.put(i, listaPossiveis);
        }

        for (int i = ko; i < len; i++) {
            List<Integer> listaPossiveis = new ArrayList<>(len);
            listaPossiveis.addAll(Arrays.asList(targetv));
            possibilidades.put(i, listaPossiveis);
        }

        int pos = ko;

        while (pos < len && pos >= ko) {
            List<Integer> list = possibilidades.get(pos);
            if (countpos[pos] >= ko) {
                for (int i = pos; i < len; i++) {
                    countpos[i] = 0;
                    int val = arr[i];
                    if (val >= 0) {
                        countval[val]--;
                        arr[i] = -1;
                    }
                }
                pos--;
                countval[arr[pos]]--;
                continue;
            }
            int lsize = list.size();
            int val = -1;
            boolean skip = true;
            boolean excluded = true;
            boolean overflow = true;
            while (skip && countpos[pos] < lsize) {
                val = list.get(countpos[pos]++);
                overflow = countval[val] >= max_val_count;
                boolean ret = false;
                for (int i = 0; i < pos && !ret; i++) {
                    if (arrdown[i] == up || arrdown[i] == down || arrup[i] == up) {
                        ret = ret || arr[i] == val;
                    }
                }
                excluded = ret;
                skip = overflow || excluded;
            }
            if (!skip) {
                arr[pos] = val;
                countval[val]++;
                pos++;
            }
        }
        if (pos < len) {
            throw new IllegalStateException("Combination impossible");
        }
        return arr;
    }

    private void completeEdges(UndirectedSparseGraphTO<Integer, Integer> graph, Integer k) {
        Collection<Integer> vertices = graph.getVertices();
        int numvert = vertices.size();
        List<Integer> incompletVertices = new ArrayList<>();
        int numArestasIniciais = graph.getEdgeCount();
        int numArestasFinais = ((k * k + 1) * k) / 2;
        int len = numArestasFinais - numArestasIniciais;
        for (Integer v : vertices) {
            if (graph.degree(v) < k) {
                incompletVertices.add(v);
            }
        }

        vertices.stream().filter((v) -> (graph.degree(v) < k)).forEachOrdered((v) -> {
            incompletVertices.add(v);
        });

        int[] pos = new int[len];
        for (int i = 0; i < len; i++) {
            pos[i] = 0;
        }

        UndirectedSparseGraphTO lastgraph = graph.clone();
        List<Integer> poss = new ArrayList<>();
        List<Integer> bestVals = new ArrayList<>();
        Integer[] bfsTmp = new Integer[numvert];
        Deque<Integer> stack = new LinkedList<>();
        Integer[] bfsWork = new Integer[numvert];
        Integer[][] bfsBackup = new Integer[k][numvert];

        while (!incompletVertices.isEmpty() && lastgraph.getEdgeCount() < numArestasFinais) {
            Integer v = incompletVertices.get(0);
            sincronizarListaPossibilidades(bfsWork, lastgraph, k, poss, v);
            int offset = stack.size();

            while (lastgraph.degree(v) < k && stack.size() >= offset) {
                poss.clear();
                for (Integer i = 0; i < bfsWork.length; i++) {
                    if (bfsWork[i] > 3 && lastgraph.degree(i) < k) {
                        poss.add(i);
                    }
                }

                int dv = lastgraph.degree(v);
                int posssize = poss.size();
                int idx = pos[stack.size()];

                if (posssize == 0 || posssize < k - dv || idx >= posssize) {
                    rollback(pos, stack, lastgraph);
                    arrayCopy(bfsBackup[stack.size() - offset], bfsWork);
                    vertices.stream().filter((vi) -> (lastgraph.degree(vi) < k)).forEachOrdered((vi) -> {
                        incompletVertices.add(vi);
                    });
                    continue;
                }

                Integer peso = null;
                Integer bestVal = null;

                for (Integer p : poss) {
                    arrayCopy(bfsWork, bfsTmp);
                    Integer tmpEdge = (Integer) lastgraph.addEdge(v, p);
                    revisitVertex(v, bfsTmp, lastgraph);
                    int pesoLocal = 0;
                    for (int z = 0; z < bfsTmp.length; z++) {
                        if (bfsTmp[z] > 3) {
                            pesoLocal++;
                        }
                    }
                    if (peso == null || pesoLocal > peso) {
                        bestVal = p;
                        peso = pesoLocal;
                        bestVals.clear();
                        bestVals.add(p);
                    } else if (peso == pesoLocal) {
                        bestVals.add(p);
                    }
                    lastgraph.removeEdge(tmpEdge);
                }

                if (idx >= bestVals.size()) {//roolback
                    rollback(pos, stack, lastgraph);
                    arrayCopy(bfsBackup[stack.size() - offset], bfsWork);
                    vertices.stream().filter((vi) -> (lastgraph.degree(vi) < k)).forEachOrdered((vi) -> {
                        incompletVertices.add(vi);
                    });
                    continue;
                }

                //Backup bfs
                arrayCopy(bfsWork, bfsBackup[stack.size() - offset]);
                bestVal = bestVals.get(idx);
                Integer ed = (Integer) lastgraph.addEdge(v, bestVal);
                pos[stack.size()]++;
                stack.push(ed);
                revisitVertex(v, bfsWork, lastgraph);
            }
            vertices.stream().filter((vi) -> (lastgraph.degree(vi) < k)).forEachOrdered((vi) -> {
                incompletVertices.add(vi);
            });
        }

        try {
            System.out.print("Added-Edges: ");
            List<Integer> stackList = (List<Integer>) stack;
            for (int i = stackList.size() - 1; i >= 0; i--) {
                Pair endpoints = lastgraph.getEndpoints(stackList.get(i));
                System.out.print(endpoints);
                System.out.print(", ");
            }
        } catch (Exception e) {
        } finally {
            System.out.println();
        }

        System.out.println("Final Graph: ");
        String edgeString = lastgraph.getEdgeString();
        System.out.println(edgeString);
    }

    public void sincronizarListaPossibilidades(Integer[] bfs, UndirectedSparseGraphTO lastgraph, Integer k, List<Integer> poss, Integer v) {
        poss.clear();
        bfs(lastgraph, bfs, v);
        for (Integer i = 0; i < bfs.length; i++) {
            if (bfs[i] > 3 && lastgraph.degree(i) < k) {
                poss.add(i);
            }
        }
    }

    public void bfs(UndirectedSparseGraphTO<Integer, Integer> subgraph, Integer[] bfs, Integer v) {
        for (int i = 0; i < bfs.length; i++) {
            bfs[i] = null;
        }
        bfs[v] = 0;
        visitVertex(v, bfs, subgraph);
    }

    public void visitVertex(Integer v, Integer[] bfs, UndirectedSparseGraphTO<Integer, Integer> subgraph1) {
        queue.clear();
        queue.add(v);
        while (!queue.isEmpty()) {
            Integer poll = queue.poll();
            int depth = bfs[poll] + 1;
            Collection<Integer> ns = (Collection<Integer>) subgraph1.getNeighbors(poll);
            for (Integer nv : ns) {
                if (bfs[nv] == null) {
                    bfs[nv] = depth;
                    queue.add(nv);
                } else if (depth < bfs[nv]) {//revisit
                    bfs[nv] = depth;
                    queue.add(nv);
                }
            }
        }
    }

    void revisitVertex(Integer hold, Integer[] bfs3, UndirectedSparseGraphTO<Integer, Integer> subgraph) {
        if (hold == null || bfs3[hold] != 0) {
            throw new IllegalStateException("BFS From another root");
        }
        visitVertex(hold, bfs3, subgraph);
    }

    void arrayCopy(Integer[] bfs, Integer[] bfsBackup) {
        for (int i = 0; i < bfs.length; i++) {
            bfsBackup[i] = bfs[i];
        }
    }

    void rollback(int[] pos, Deque<Integer> stack, UndirectedSparseGraphTO graph) {
        for (int i = stack.size(); i < pos.length; i++) {
            pos[i] = 0;
        }
        graph.removeEdge(stack.pop());
    }
}
