package util;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author strike
 */
public class SubGraphCheck {

    private static final long HOUR = 1000 * 60 * 60 * 12;

    public static void main(String... args) throws FileNotFoundException, IOException {
        if (args.length < 2) {
            System.err.println("args: file-graphs.es file-subraph.es [seq-start]");
            return;
        }
        UndirectedSparseGraphTO graph = UtilGraph.loadGraphES(new FileInputStream(args[0]));
        UndirectedSparseGraphTO subgraph = UtilGraph.loadGraphES(new FileInputStream(args[1]));
        if (graph.getVertexCount() == 0 || graph.getEdgeCount() == 0 || subgraph.getVertexCount() == 0 || subgraph.getEdgeCount() == 0) {
            System.err.println("exists empty graph");
            return;
        }
        boolean found = false;

        int[] currentPermutation = null;

        if (args != null && args.length > 2) {
            String args2[] = Arrays.copyOfRange(args, 2, args.length);
            currentPermutation = UtilProccess.args2intarr(args2);
        }
        Collection vertices = graph.getVertices();
        if (currentPermutation == null || currentPermutation.length < vertices.size()) {
            currentPermutation = new int[vertices.size()];
            for (int i = 0; i < vertices.size(); i++) {
                currentPermutation[i] = i;
            }
        }
        long lastime = System.currentTimeMillis();

        System.out.print("Checking graph ");
        System.out.print(args[0]);
        System.out.print(" as ");
        System.out.println(args[1]);

        System.out.print("graph: ");
        System.out.println(graph);

        System.out.print("subgraph: ");
        System.out.println(subgraph);

        System.out.print("starting: ");
        UtilProccess.printArray(currentPermutation);

        boolean hasnext = true;

        while (hasnext && !found) {
            found = graph.containStrict(subgraph, currentPermutation);
            if (System.currentTimeMillis() - lastime > HOUR) {
                lastime = System.currentTimeMillis();
                System.out.print("h-");
                UtilProccess.printArray(currentPermutation);
            }
            hasnext = nextPermutation(currentPermutation);
        }
        if (found) {
            System.out.println("Found maped subgraph isomorphic");
            UtilProccess.printArray(currentPermutation);
        }
    }

    //Reference: https://www.nayuki.io/res/next-lexicographical-permutation-algorithm/nextperm.java
    public static boolean nextPermutation(int[] currentPerm) {
        // Find non-increasing suffix
        int i = currentPerm.length - 1;
        while (i > 0 && currentPerm[i - 1] >= currentPerm[i]) {
            i--;
        }
        if (i <= 0) {
            return false;
        }

        // Find successor to pivot
        int j = currentPerm.length - 1;
        while (currentPerm[j] <= currentPerm[i - 1]) {
            j--;
        }
        int temp = currentPerm[i - 1];
        currentPerm[i - 1] = currentPerm[j];
        currentPerm[j] = temp;

        // Reverse suffix
        j = currentPerm.length - 1;
        while (i < j) {
            temp = currentPerm[i];
            currentPerm[i] = currentPerm[j];
            currentPerm[j] = temp;
            i++;
            j--;
        }
        return true;
    }
}
