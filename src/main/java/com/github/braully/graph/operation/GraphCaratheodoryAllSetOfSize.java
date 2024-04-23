package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Braully Rocha da Silva
 */
public class GraphCaratheodoryAllSetOfSize extends GraphCaratheodoryNumberBinary {

    static final Logger log = Logger.getLogger(GraphCaratheodoryAllSetOfSize.class);
    static final String type = "P3-Convexity";
    static final String description = "Caratheodory All Sets of Size";

    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        if (graph == null || graph.getVertexCount() <= 0) {
            return new HashMap<>();
        }

        Integer size = null;

        try {
            String inputData = graph.getInputData();
            size = Integer.parseInt(inputData);
        } catch (Exception e) {

        }
        return checksizeof(size, graph);
    }

    public Map<String, Object> checksizeof(Integer size, UndirectedSparseGraphTO<Integer, Integer> graph) throws IllegalArgumentException {
        Map<String, Object> result = new HashMap<>();

        if (size == null) {
            throw new IllegalArgumentException("Input invalid (not integer): " + graph.getInputData());
        }
        result.put("sets", new ArrayList<>());

        int countNCarat = 0;
        int menorhsize = 0;
        int maiorMaxp = 0;
        Set<Integer> menor = new HashSet<Integer>();

        if (size >= 2) {
            Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(graph.getVertexCount(), size);
            while (combinationsIterator.hasNext()) {
                int[] currentSet = combinationsIterator.next();
                OperationConvexityGraphResult hsp3g = hsp3(graph, currentSet);
                if (hsp3g != null) {
                    int chsize = hsp3g.convexHull.size();
                    if (menorhsize == 0 || chsize < menorhsize) {
                        menorhsize = chsize;
                        menor.clear();
                        for (int i : currentSet) {
                            menor.add(i);
                        }
                    }
                    if (maiorMaxp == 0) {
                        maiorMaxp = this.maxp;
                    }
                    if (maiorMaxp < this.maxp) {
                        maiorMaxp = this.maxp;
                    }
                    TreeSet<Integer> treeSet = new TreeSet<>(hsp3g.caratheodorySet);
                    String key = "Caratheodory Set-" + (countNCarat++) + " |HS|=" + chsize;
                    System.out.println(key + ": " + treeSet);
                    System.out.println("Maior P: " + this.maxp);
                    System.out.println("Partial: " + hsp3g.partial);
                    ((List) result.get("sets")).add(treeSet);
//                    result.put(key, hsp3g.caratheodorySet);

//                    log.info(key + ": " + hsp3g.caratheodorySet);
                    if (verbose) {
                    }
                }
            }
            result.put("Nº Caratheodory Set of Size(" + size + ")", countNCarat);
            result.put("Menor |H(S)", menorhsize);
            result.put("menor", menor);
            result.put("maiorp", maiorMaxp);

//            log.info("Menor |H(S): " + menorhsize);
//            log.info("Nº Caratheodory Set of Size(" + size + "): " + countNCarat);
        }
        return result;
    }

    public String getTypeProblem() {
        return type;
    }

    public String getName() {
        return description;
    }
}
