package util;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BFSUtil {

    public Integer[] bfs = null;
    private Queue<Integer> queue = null;
    public int[] depthcount = new int[5];
    public Integer[] compactMatrix;
    Integer[] bfsBkp = null;
    int[] depthcountBkp = null;
    Integer vertexCount;

    public void backup() {
        if (bfsBkp == null) {
            Integer[] bfsBkp = new Integer[bfs.length];
            int[] depthcountBkp = new int[5];
        }
        for (int i = 0; i < bfs.length; i++) {
            bfsBkp[i] = bfs[i];
        }
        for (int i = 0; i < depthcount.length; i++) {
            depthcountBkp[i] = depthcount[i];
        }
    }

    public void load() {
        if (bfsBkp != null) {
            for (int i = 0; i < bfs.length; i++) {
                bfs[i] = bfsBkp[i];
            }
            for (int i = 0; i < depthcount.length; i++) {
                depthcount[i] = depthcountBkp[i];
            }
        }
    }

    public static BFSUtil newBfsUtilSimple(int size) {
        return new BFSUtil(size);
    }

    public static BFSUtil newBfsUtilCompactMatrix(int vertexCount) {
        BFSUtil bfsUtil = new BFSUtil(vertexCount);
        bfsUtil.compactMatrix = new Integer[((1 + vertexCount) * vertexCount) / 2];
        return bfsUtil;
    }

    public int discored = 0;

    private BFSUtil(int size) {
        bfs = new Integer[size];
        vertexCount = size;
        queue = new LinkedList<Integer>();
    }

    public void labelDistances(UndirectedSparseGraphTO graphTemplate, Integer v) {
        bfs(graphTemplate, v);
    }

    public Integer getDistance(UndirectedSparseGraphTO graphTemplate, Integer u) {
        return bfs[u];
    }

    public Integer getDistanceSafe(UndirectedSparseGraphTO graphTemplate, Integer u) {
        if (bfs[u] == null) {
            return -1;
        }
        return bfs[u];
    }

    public void labelDistances(UndirectedSparseGraphTO graphTemplate, Collection<Integer> vs) {
        bfsRanking(graphTemplate, vs);
    }

    public void bfsRanking(UndirectedSparseGraphTO<Integer, Integer> subgraph, Collection<Integer> vs) {
        for (int i = 0; i < bfs.length; i++) {
            bfs[i] = null;
        }
        queue.clear();
        for (Integer v : vs) {
            queue.add(v);
            bfs[v] = 0;
            revisitVertex(v, bfs, subgraph);
        }
    }

    public void bfsRanking(UndirectedSparseGraphTO<Integer, Integer> subgraph, Integer v, Integer... fakeNeighbor) {
        for (int i = 0; i < bfs.length; i++) {
            bfs[i] = null;
        }
        discored = 1;

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

    public void visitVertexRanking(Integer v, Integer[] bfs,
            UndirectedSparseGraphTO<Integer, Integer> subgraph1) {
        while (!queue.isEmpty()) {
            Integer poll = queue.poll();
            int depth = bfs[poll] + 1;
            Collection<Integer> ns = (Collection<Integer>) subgraph1.
                    getNeighborsUnprotected(poll);
            for (Integer nv : ns) {
                if (bfs[nv] == null) {
                    discored++;
                    bfs[nv] = depth;
                    queue.add(nv);
                }
            }
        }
    }

    public void revisitVertexRanking(Integer v, Integer[] bfs,
            UndirectedSparseGraphTO<Integer, Integer> subgraph1) {
        while (!queue.isEmpty()) {
            Integer poll = queue.poll();
            int depth = bfs[poll] + 1;
            Collection<Integer> ns = (Collection<Integer>) subgraph1.
                    getNeighborsUnprotected(poll);
            for (Integer nv : ns) {
                if (bfs[nv] > depth) {
                    discored++;
                    depthcount[bfs[nv]]--;
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
        while (!queue.isEmpty()) {
            Integer poll = queue.poll();
            int depth = bfs[poll] + 1;
            Collection<Integer> ns = (Collection<Integer>) subgraph1.
                    getNeighborsUnprotected(poll);
            for (Integer nv : ns) {
                if (bfs[nv] == null) {
                    bfs[nv] = depth;
                    queue.add(nv);
                }
            }
        }
    }

    public void revisitVertex(UndirectedSparseGraphTO<Integer, Integer> subgraph1,
            Integer v) {
        while (!queue.isEmpty()) {
            Integer poll = queue.poll();
            int depth = bfs[poll] + 1;
            Collection<Integer> ns = (Collection<Integer>) subgraph1.
                    getNeighborsUnprotected(poll);
            for (Integer nv : ns) {
                if (bfs[nv] == null) {
                    discored++;
                    bfs[nv] = depth;
                    queue.add(nv);
                } else if (bfs[nv] > depth) {
                    bfs[nv] = depth;
                    queue.add(nv);
                }
            }
        }
    }

    public void revisitVertex(Integer v, Integer[] bfs,
            UndirectedSparseGraphTO<Integer, Integer> subgraph1) {
        while (!queue.isEmpty()) {
            Integer poll = queue.poll();
            int depth = bfs[poll] + 1;
            Collection<Integer> ns = (Collection<Integer>) subgraph1.
                    getNeighborsUnprotected(poll);
            for (Integer nv : ns) {
                if (bfs[nv] == null || bfs[nv] > depth) {
                    bfs[nv] = depth;
                    queue.add(nv);
                }
            }
        }
    }

    public void incDepthcount(int[] depthcount) {
        for (int i = 0; i < depthcount.length; i++) {
            this.depthcount[i] = this.depthcount[i] + depthcount[i];
        }
    }

    public void incBfs(UndirectedSparseGraphTO graph, Integer vroot, Integer newvert) {
        if (newvert != null) {
            depthcount[bfs[newvert]]--;
            bfs[newvert] = 1;
            queue.add(newvert);
            depthcount[1]++;
            revisitVertexRanking(vroot, bfs, graph);

        }
    }

    public void incBfs(UndirectedSparseGraphTO graph, Integer newroowt) {
        if (bfs[newroowt] == null) {
            discored++;
        }
        bfs[newroowt] = 0;
        queue.add(newroowt);
        revisitVertex(graph, newroowt);
    }

    public void labelDistancesCompactMatrix(UndirectedSparseGraphTO graph) {
        for (Integer vertsrc : (List<Integer>) graph.getVertices()) {
            labelDistances(graph, vertsrc);
            for (int j = 0; j < bfs.length; j++) {
                if (vertsrc != null) {

                    Integer va=  bfs[j];
                    if (va  != null) {
                        set(vertsrc, j, va);
                    }
                }
            }
        }
    }

    /*
       012345 |0
        06789 |1
         0abf |2
          0cd |3
           0e |4
            0 |5
       k=6
       i=0,j=0 -> p[0]=0
       i=0,j=1 -> p[1]=1
       i=0,j=2 -> p[2]=2
       ...
       i=0,j=5 -> p[5]=5
    
       i=1,j=0 -> p[1]=1
       i=1,j=1 -> p[6]=0
       i=1,j=2 -> p[7]=6
       i=1,j=3 -> p[8]=7
       ...
       i=1,j=5 -> p[10]=9
    
       i=2,j=0 -> p[ 2]=2
       i=2,j=1 -> p[ 7]=6
       i=2,j=2 -> p[11]=0
       i=2,j=3 -> p[12]=a
       ...
       i=2,j=5 -> p[14]=f
    
       i=3,j=0 -> p[3]=3
       i=3,j=1 -> p[8]=7
       i=3,j=2 -> p[12]=a
       i=3,j=3 -> p[15]=0
       i=3,j=4 -> p[16]=c
       i=3,j=5 -> p[17]=d
    
       i=4,j=4 -> p[18]=0
       i=4,j=5 -> p[19]=e
       
      
       i=5,j=0 p[ 5]=5
       i=5,j=1 p[10]=9
       i=5,j=2 p[14]=f 
       i=5,j=3 p[17]=d
       i=5,j=4 p[19]=e
       i=5,j=5 p[20]=0
       
       012345678901234567890
       012345067890abf0cd0e0

       012345 |0 
       106789 |1
       260abf |2
       37a0cd |3
       48bc0e |4
       59fde0 |5
     */
    public Integer get(int i, int j) {
        int pos = calcPositionCompactMatrix(i, j);
        return compactMatrix[pos];
    }

    public int calcPositionCompactMatrix(int i, int j) {
        //se a posição está na parte da matriz espelho, inverter

        int a_i = 0;
        if (j < i) {
            a_i = i;
            i = j;
            j = a_i;
        }

        //termo de uma PA: a_n = a_1 + r*(n-1)
        a_i = vertexCount - 1 * (i - 1);
        //sn = ((a_1 + a_n)*n)/2
        a_i = ((vertexCount + a_i) * i) / 2;
        return a_i + j - i;
    }

    public void set(int i, int j, int value) {
        //System.out.printf("set(%d,%d)=%d\n", i, j, value);
        int pos = calcPositionCompactMatrix(i, j);
        compactMatrix[pos] = value;
    }

    public void clearBfs() {
        for (int i = 0; i < bfs.length; i++) {
            bfs[i] = null;
        }
    }
}
