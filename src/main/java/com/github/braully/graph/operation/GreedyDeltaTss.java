package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import static javax.swing.text.html.HTML.Attribute.N;
import static org.apache.commons.io.IOUtils.skip;
import util.UtilProccess;

public class GreedyDeltaTss
        extends GenericGreedy implements IGraphOperation {

    static final Logger log = Logger.getLogger(GreedyDeltaTss.class.getSimpleName());
    static final String description = "Greedy-Delta";

    public String getDescription() {
        return description;
    }

    public void selectBestVertice(List<Integer> vertices, int[] aux) {
        bestVertice = -1;
        maxDelta = 0;
        for (Integer w : vertices) {
            //Ignore w if is already contamined OR skip review to next step
            if (aux[w] >= kr[w]) {
                continue;
            }
            if (aux[w] >= countContaminatedVertices) {
                continue;
            }
            int wDelta = 0;

            //Clear and init w contamined count aux variavles
            auxCount.clear();
            auxCount.setVal(w, kr[w]);
            mustBeIncluded.clear();
            mustBeIncluded.add(w);
            //Propagate w contamination
            while (!mustBeIncluded.isEmpty()) {
                Integer verti = mustBeIncluded.remove();
                Collection<Integer> neighbors = N[verti];
                for (Integer vertn : neighbors) {
                    if ((aux[vertn] + auxCount.getCount(vertn)) >= kr[vertn]) {
                        continue;
                    }
                    Integer inc = auxCount.inc(vertn);
                    if ((inc + aux[vertn]) == kr[vertn]) {
                        mustBeIncluded.add(vertn);
                        skip[vertn] = countContaminatedVertices;
                    }
                }
                wDelta++;
            }

            if (bestVertice == -1 || wDelta > maxDelta) {
                bestVertice = w;
                maxDelta = wDelta;
            }
        }
    }

    public static void main(String... args) throws IOException {
        System.out.println("Execution Sample: BlogCatalog database R=2");
        UndirectedSparseGraphTO<Integer, Integer> graph = null;
        GreedyDeltaTss op = new GreedyDeltaTss();

//        URI urinode = URI.create("jar:file:data/big/all-big.zip!/Livemocha/nodes.csv");
//        URI uriedges = URI.create("jar:file:data/big/all-big.zip!/Livemocha/edges.csv");
//        URI urinode = URI.create("jar:file:data/big/all-big.zip!/BlogCatalog/nodes.csv");
//        URI uriedges = URI.create("jar:file:data/big/all-big.zip!/BlogCatalog/edges.csv");
        URI urinode = URI.create("jar:file:data/big/all-big.zip!/Last.fm/nodes.csv");
        URI uriedges = URI.create("jar:file:data/big/all-big.zip!/Last.fm/edges.csv");
        InputStream streamnode = urinode.toURL().openStream();
        InputStream streamedges = uriedges.toURL().openStream();

        graph = UtilGraph.loadBigDataset(streamnode, streamedges);

        op.setVerbose(true);

        op.setPercent(0.5);
        UtilProccess.printStartTime();
        Set<Integer> buildOptimizedHullSet = op.buildTargeSet(graph);
        UtilProccess.printEndTime();

        System.out.println(
                "S[" + buildOptimizedHullSet.size() + "]: "
                + buildOptimizedHullSet
        );
    }

}
