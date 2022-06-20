package com.github.braully.graph.generator;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import com.github.braully.graph.operation.GraphHullSetNC;
import com.github.braully.graph.operation.GraphSubgraph;
import com.github.braully.graph.operation.OperationConvexityGraphResult;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GraphGeneratorStudyCase extends AbstractGraphGenerator {

    static final String description = "Study case";

    static final String CYCLE = "Cyle";
    static final String INNER = "Inner";

    static final String[] parameters = {CYCLE, INNER};

    static String DEFAULT_CYCLE = "0, 56, 3249";
    static String DEFAULT_INNER = "222,";

    UndirectedSparseGraphTO<Integer, Integer> graphES = null;

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

        String cycle = getStringParameter(parameters, CYCLE);
        String inner = getStringParameter(parameters, INNER);

        if (cycle == null) {
            cycle = DEFAULT_CYCLE;
        }

        if (inner == null) {
            inner = DEFAULT_INNER;
        }

        try {
            String grafoFile = "esqueleto-ultimo-grafo-moore.es";
            InputStream openStream = Thread.currentThread().getContextClassLoader().getResource(grafoFile).openStream();
            graphES = UtilGraph.loadGraphES(openStream);
        } catch (Exception ex) {
            Logger.getLogger(GraphGeneratorStudyCase.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
        GraphHullSetNC hullOp = new GraphHullSetNC();
        GraphSubgraph subGrapOp = new GraphSubgraph();

        Set<Integer> set = new LinkedHashSet<>();
        Set<Integer> subSet = new LinkedHashSet<>();
        Set<Integer> setInner = new LinkedHashSet<>();

        String[] split = cycle.split(",");
        for (String str : split) {
            int parseInt = Integer.parseInt(str.trim());
            set.add(parseInt);
        }
        split = inner.split(",");
        for (String str : split) {
            int parseInt = Integer.parseInt(str.trim());
            setInner.add(parseInt);
        }
        List<Integer> na = new ArrayList<>();
        List<Integer> nss = new ArrayList<>();
        OperationConvexityGraphResult hsGraph = hullOp.hsp3(graphES, set);

        subSet.addAll(hsGraph.convexHull);

        for (Integer v : (Collection<Integer>) graphES.getVertices()) {
            if (hsGraph.convexHull.contains(v)) {
                continue;
            }
            Collection<Integer> ns = graphES.getNeighborsUnprotected(v);
            if (Collections.disjoint(hsGraph.convexHull, ns)) {
                na.add(v);
            } else {
                nss.add(v);
            }
        }
        Collections.sort(na);
        Collections.sort(nss);

        subSet.addAll(setInner);

        UndirectedSparseGraphTO<Integer, Integer> graph = subGrapOp.subGraphInduced(graphES, subSet);
        return graph;
    }
}
