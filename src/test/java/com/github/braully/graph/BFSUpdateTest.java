package com.github.braully.graph;

import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import util.BFSUtil;

/**
 *
 * @author Braully Rocha da Silva
 */
public class BFSUpdateTest {

    String strGraph = "0-6,1-7,2-8,3-9,4-10,5-11,"
            + "0-12,12-42,12-7,6-13,49-42,13-42,13-1,0-14,14-43,14-8,6-15,49-43,15-43,15-2,0-16,16-44,16-9,6-17,49-44,17-44,17-3,0-18,18-45,18-10,6-19,49-45,19-45,19-4,0-20,20-46,20-11,6-21,49-46,21-46,21-5,"
            + "1-22,22-44,22-8,7-23,49-44,23-44,23-2,1-24,24-45,24-9,7-25,49-45,25-45,25-3,1-26,26-46,26-10,7-27,49-46,27-46,27-4,1-28,28-43,28-11,7-29,49-43,29-43,29-5,"
            + "2-30,30-46,30-9,8-31,49-46,31-46,31-3,2-32,32-42,32-10,8-33,49-42,33-42,33-4,2-34,34-45,34-11,8-35,49-45,35-45,35-5,"
            + "3-36,36-43,36-10,9-37,49-43,37-43,37-4,3-38,38-42,38-11,9-39,49-42,39-42,39-5,"
            + "4-40,40-44,40-11,10-41,49-44,41-44,41-5,"
            + "47-49,49-48,"
            + "47-0,48-6,47-1,48-7,47-2,48-8,47-3,48-9,47-4,48-10,47-5,48-11,";

    UndirectedSparseGraphTO graph = new UndirectedSparseGraphTO(strGraph);

    // <17, 27>, <17, 28>, <17, 32>, <17, 35>, 
    // <18, 23>, <18, 28>, <18, 31>, <18, 39>,
    // <19, 22>, <19, 29>, <19, 30>, <19, 38>,
    // <20, 22>, <20, 25>, <20, 32>, <20, 37>,
    // <21, 23>, <21, 24>, <21, 33>, <21, 36>,
    // <22, 36>, <22, 39>,
    // <23, 37>, <23, 38>,
    // <24, 32>, <24, 40>,
    // <25, 33>, <25, 41>,
    // <26, 35>, <26, 38>,
    // <27, 34>, <27, 39>,
    // <28, 30>, <28, 33>,
    // <29, 31>, <29, 32>,
    // <30, 41>, 
    // <31, 40>, 
    // <34, 36>,
    // <35, 37>, 
    Map<Integer, List<Integer>> addEdges
            = Map.of(12, List.of(30, 35, 36, 40), //<12, 30>, <12, 35>, <12, 36>, <12, 40>,
                    13, List.of(31, 34, 37, 41), //<13, 31>, <13, 34>, <13, 37>, <13, 41>,
                    14, List.of(24, 27, 38, 41), //<14, 24>, <14, 27>, <14, 38>, <14, 41>,
                    15, List.of(25, 26, 39, 40), //<15, 25>, <15, 26>, <15, 39>, <15, 40>,
                    16, List.of(26, 29, 33, 34), // <16, 26>, <16, 29>, <16, 33>, <16, 34>, 
                    17, List.of(27, 28, 32, 35),
                    18, List.of(23, 28, 31, 39),
                    19, List.of(22, 29, 30, 38),
                    20, List.of(22, 25, 32, 37),
                    21, List.of(23, 24, 33, 36)
            //                    22, List.of(36, 39),
            //                    23, List.of(37, 38),
            //                    24, List.of(32, 40),
            //                    25, List.of(33, 41),
            //                    26, List.of(35, 38),
            //                    27, List.of(34, 39),
            //                    28, List.of(30, 33),
            //                    29, List.of(31, 32),
            //                    30, List.of(41),
            //                    31, List.of(40),
            //                    34, List.of(36),
            //                    35, List.of(37)
            );

    @Test
    public void testUpdateBfs() {
        String[] edges = null;
        BFSUtil bfs = new BFSUtil(graph.getVertexCount());
        BFSUtil bfsaux = new BFSUtil(graph.getVertexCount());
        BFSUtil bfsinc = new BFSUtil(graph.getVertexCount());

        for (Integer vertsrc : addEdges.keySet()) {
            bfs.labelDistances(graph, vertsrc);
            bfsaux.labelDistances(graph, vertsrc);
            bfsinc.labelDistances(graph, vertsrc);
            for (Integer verttg : addEdges.get(vertsrc)) {
                bfsaux.bfsRanking(graph, vertsrc, verttg);
                graph.addEdge(vertsrc, verttg);
                bfs.labelDistances(graph, vertsrc);
                bfsinc.incBfs(graph, vertsrc, verttg);
                System.out.printf("Adding edge: %d-%d\n", vertsrc, verttg);
                for (int i = 0; i < bfs.bfs.length; i++) {
                    Assert.assertEquals(bfs.bfs[i], bfsaux.bfs[i]);
                    Assert.assertEquals(bfs.bfs[i], bfsinc.bfs[i]);
                }
            }
        }
    }
}
