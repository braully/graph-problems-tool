package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author strike
 */
public class ReorderIncludeSequence {

    @Ignore
    @Test
    public void reorder57() throws FileNotFoundException, IOException {
        GraphHullSetNC subgraph = new GraphHullSetNC();
        UndirectedSparseGraphTO<Integer, Integer> graphES = UtilGraph.loadGraphES(new FileInputStream("./esqueleto-ultimo-grafo-moore.es"));
        String viz = "0, 56, 1, 57, 3191";
        Set<Integer> set = new LinkedHashSet<>();
        String[] split = viz.split(",");
        for (String str : split) {
            int parseInt = Integer.parseInt(str.trim());
            set.add(parseInt);
        }
        graphES.setSet(set);

        OperationConvexityGraphResult hsGraph = subgraph.hsp3(graphES, set);

        Set<Integer> vns = new HashSet<>();
        for (Integer verticePrioritario : new int[]{3247, 3248}) {
            vns.addAll(graphES.getNeighborsUnprotected(verticePrioritario));
            vns.add(verticePrioritario);
        }

        Map<Integer, Set<Integer>> verticesInteresse = new HashMap<>();
        for (Integer v : vns) {
            List<Integer> ret = subgraph.includedSequenceN.get(v);
            for (Integer vi : ret) {
                Set<Integer> va  = verticesInteresse.computeIfAbsent(vi, (t) -> new HashSet<>());
                va.add(v);
            }
        }

        Set<Integer> includeat = new HashSet<>();
        List<Integer> includedSequence = new ArrayList<>(hsGraph.includedSequence);

        for (Integer v : includedSequence) {
            if (includeat.contains(v)) {
                continue;
            }

            boolean addV = addV(subgraph, v, includeat, graphES);

            Set<Integer> verificar = verticesInteresse.get(v);
            if (verificar != null) {
                for (Integer ve : verificar) {
                    if (includeat.contains(ve)) {
                        continue;
                    }

                    List<Integer> all = subgraph.includedSequenceN.get(ve);
                    if (includeat.containsAll(all)) {
                        addV = addV || addV(subgraph, ve, includeat, graphES);
                    }
                }
            }
            if (addV) {
                System.out.println("Fim");
                break;
            }
        }
    }

    @Ignore
    @Test
    public void reorder7() throws FileNotFoundException, IOException {
        GraphHullSetNC subgraph = new GraphHullSetNC();
        UndirectedSparseGraphTO<Integer, Integer> graphES = UtilGraph.loadGraphES(new FileInputStream("./esqueleto-grafo-moore-50.es"));
        String viz = "0, 6, 1, 7, 41";
        Set<Integer> set = new LinkedHashSet<>();
        String[] split = viz.split(",");
        for (String str : split) {
            int parseInt = Integer.parseInt(str.trim());
            set.add(parseInt);
        }
        graphES.setSet(set);

        OperationConvexityGraphResult hsGraph = subgraph.hsp3(graphES, set);

        Set<Integer> vns = new HashSet<>();
        for (Integer verticePrioritario : new int[]{47, 48}) {
            vns.addAll(graphES.getNeighborsUnprotected(verticePrioritario));
            vns.add(verticePrioritario);
        }

        Map<Integer, Set<Integer>> verticesInteresse = new HashMap<>();
        for (Integer v : vns) {
            List<Integer> ret = subgraph.includedSequenceN.get(v);
            for (Integer vi : ret) {
                Set<Integer> va  = verticesInteresse.computeIfAbsent(vi, (t) -> new HashSet<>());
                va.add(v);
            }
        }

        Set<Integer> includeat = new HashSet<>();
        List<Integer> includedSequence = new ArrayList<>(hsGraph.includedSequence);

        for (Integer v : includedSequence) {
            if (includeat.contains(v)) {
                continue;
            }

            boolean addV = addV(subgraph, v, includeat, graphES);

            Set<Integer> verificar = verticesInteresse.get(v);
            if (verificar != null) {
                for (Integer ve : verificar) {
                    if (includeat.contains(ve)) {
                        continue;
                    }

                    List<Integer> all = subgraph.includedSequenceN.get(ve);
                    if (includeat.containsAll(all)) {
                        addV = addV || addV(subgraph, ve, includeat, graphES);
                    }
                }
            }
            if (addV) {
                System.out.println("Fim");
                break;
            }
        }
    }

    protected boolean addV(GraphHullSetNC subgraph, Integer v, Set<Integer> includeat, UndirectedSparseGraphTO<Integer, Integer> graphES) {
        boolean end = false;

        List<Integer> nss = subgraph.includedSequenceN.get(v);
        includeat.add(v);
//        System.out.print(v + ":" + nss + ", ");
        System.out.print(v + ", ");

        Collection<Integer> ns = graphES.getNeighborsUnprotected(v);
        for (Integer vn : ns) {
            Collection<Integer> vnn = graphES.getNeighborsUnprotected(vn);
            if (vnn.size() > 3) {
                if (includeat.containsAll(vnn)) {
                    System.out.println("\n[" + vn + "]");
                    end = true;
                }
            }
        }
        return end;
    }

    @Test
    public void testHsForall() throws FileNotFoundException, IOException {
        GraphHullSetNC subgraph = new GraphHullSetNC();
        UndirectedSparseGraphTO<Integer, Integer> graphES = UtilGraph.loadGraphES(new FileInputStream("./estripado-esqueleto-grafo-moore-50.es"));
        String viz = "0, 6, 49";
        Set<Integer> set = new LinkedHashSet<>();
        String[] split = viz.split(",");
        for (String str : split) {
            int parseInt = Integer.parseInt(str.trim());
            set.add(parseInt);
        }

        for (Integer sa : new Integer[]{
            30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46}) {
            LinkedHashSet<Integer> seta = new LinkedHashSet<>();
            seta.addAll(set);
            seta.add(sa);
            graphES.setSet(seta);
            OperationConvexityGraphResult hsGraph = subgraph.hsp3(graphES, seta);
            if (hsGraph.convexHull.size() < graphES.getVertexCount()) {
                System.out.println(sa + ": não é envoltoria");
//                System.out.println(hsGraph.toMap());
            } else {
                System.out.println(sa + ": ok");
            }
        }

        Set<Integer> vns = new HashSet<>();
        for (Integer verticePrioritario : new int[]{47, 48}) {
            vns.addAll(graphES.getNeighborsUnprotected(verticePrioritario));
            vns.add(verticePrioritario);
        }

        Map<Integer, Set<Integer>> verticesInteresse = new HashMap<>();
        for (Integer v : vns) {
            List<Integer> ret = subgraph.includedSequenceN.get(v);
            for (Integer vi : ret) {
                Set<Integer> va  = verticesInteresse.computeIfAbsent(vi, (t) -> new HashSet<>());
                va.add(v);
            }
        }
    }
}
