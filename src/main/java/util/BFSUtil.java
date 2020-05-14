package util;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public class BFSUtil {

    Integer[] bfs = null;
    private Queue<Integer> queue = null;
    int[] depthcount = new int[5];

    BFSUtil(int size) {
        bfs = new Integer[size];
        queue = new LinkedList<Integer>();
    }

    void labelDistances(UndirectedSparseGraphTO graphTemplate, Integer v) {
        bfs(graphTemplate, v);
    }

    Integer getDistance(UndirectedSparseGraphTO graphTemplate, Integer u) {
        return bfs[u];
    }

    void bfsRanking(UndirectedSparseGraphTO<Integer, Integer> subgraph, Integer v, Integer... fakeNeighbor) {
        for (int i = 0; i < bfs.length; i++) {
            bfs[i] = null;
        }
        clearRanking();
        queue.clear();
        queue.add(v);
        bfs[v] = 0;
        if (fakeNeighbor != null) {
            for (Integer fkn : fakeNeighbor) {
                bfs[fkn] = 1;
                queue.add(fkn);
                depthcount[1]++;
            }
        }
        visitVertexRanking(v, bfs, subgraph);
    }

    public void clearRanking() {
        for (int i = 0; i < depthcount.length; i++) {
            depthcount[i] = 0;
        }
    }

    void visitVertexRanking(Integer v, Integer[] bfs,
            UndirectedSparseGraphTO<Integer, Integer> subgraph1) {
        while (!queue.isEmpty()) {
            Integer poll = queue.poll();
            int depth = bfs[poll] + 1;
            Collection<Integer> ns = (Collection<Integer>) subgraph1.
                    getNeighborsUnprotected(poll);
            for (Integer nv : ns) {
                if (bfs[nv] == null) {
                    bfs[nv] = depth;
                    queue.add(nv);
                    depthcount[depth]++;
                }
            }
        }
    }

    void bfs(UndirectedSparseGraphTO<Integer, Integer> subgraph, Integer v) {
        bfsRanking(subgraph, v);
    }

    void visitVertex(Integer v, Integer[] bfs, UndirectedSparseGraphTO<Integer, Integer> subgraph1) {

    }

    void incDepthcount(int[] depthcount) {
        for (int i = 0; i < depthcount.length; i++) {
            this.depthcount[i] = this.depthcount[i] + depthcount[i];
        }
    }
}
