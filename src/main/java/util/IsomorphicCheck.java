package util;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import edu.uci.ics.jung.graph.util.Pair;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import static java.util.Objects.nonNull;

/**
 *
 * @author strike
 */
public class IsomorphicCheck {

    private static final long HOUR = 1000 * 60 * 60 * 12;

    public static List<Character> abindex = List.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
            'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'x', 'y', 'z');

    public static void main(String... args) throws FileNotFoundException, IOException {
//        if (args.length < 2) {
//            System.err.println("args: file-graphs.es file-subraph.es [seq-start]");
//            return;
//        }
        String strfile = "/home/strike/grafos/exercicio-salvador.txt";

        String grafo[] = new String[]{"", ""};
        int index = 0;

        BufferedReader buffer = new BufferedReader(new FileReader(strfile));

        String line = null;
        while (nonNull(line = buffer.readLine())) {
            if (line.trim().isEmpty()) {
                index++;
                continue;
            }
            grafo[index] += line + "\n";
        }

//        UndirectedSparseGraphTO graph = UtilGraph.loadGraphES(new FileInputStream(args[0]));
//        UndirectedSparseGraphTO subgraph = UtilGraph.loadGraphES(new FileInputStream(args[1]));
        UndirectedSparseGraphTO graph = UtilGraph.loadGraphAdjMatrix(new ByteArrayInputStream(grafo[0].getBytes()));
        UndirectedSparseGraphTO subgraph = UtilGraph.loadGraphAdjMatrix(new ByteArrayInputStream(grafo[1].getBytes()));
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
//        System.out.print(args[0]);
        System.out.print(" as ");
//        System.out.println(args[1]);

        System.out.print("graph: ");
        System.out.println(graph);

        System.out.print("subgraph: ");
        System.out.println(subgraph);

        System.out.print("starting: ");
        UtilProccess.printArray(currentPermutation);

        boolean hasnext = true;
        System.out.println("G:");
        Collection verts = graph.getVertices();
        System.out.print("V(G)=");
        System.out.println(verts);
        
        System.out.println("|E(G)|=" + graph.getEdgeCount());
        System.out.print("E(G)=");
        System.out.println(graph.getEdgeString());

        System.out.println("H:");
        List verts2 = (List) subgraph.getVertices();
        System.out.print("V(H)=[");
//            System.out.println(verts2);
        for (int i = 0; i < verts2.size(); i++) {
            System.out.print(abindex.get(i));
            System.out.print(", ");
        }
        System.out.println("]");

        System.out.println("|E(H)|=" + subgraph.getEdgeCount());
        System.out.print("E(H)=");
        System.out.println(getEdgeStringFormated(subgraph, abindex));
        System.out.println();

        while (hasnext && !found) {
            found = graph.containStrict(subgraph, currentPermutation);
            if (found) {
                found = subgraph.containStrict(graph, currentPermutation);
            }
            if (System.currentTimeMillis() - lastime > HOUR) {
                lastime = System.currentTimeMillis();
                System.out.print("h-");
                UtilProccess.printArray(currentPermutation);
            }
            hasnext = nextPermutation(currentPermutation);
        }

        System.out.println("G:");
        verts = graph.getVertices();
        System.out.print("V(G)=");
        System.out.println(verts);
        System.out.print("E(G)=");
        System.out.println(graph.getEdgeString());

        System.out.println("H:");
        verts2 = (List) subgraph.getVertices();
        System.out.print("V(H)=[");
//            System.out.println(verts2);
        for (int i = 0; i < verts2.size(); i++) {
            System.out.print(abindex.get(i));
            System.out.print(", ");
        }
        System.out.println("]");

        System.out.print("E(H)=");
        System.out.println(getEdgeStringFormated(subgraph, abindex));
        System.out.println();

        if (found) {

//            System.out.println("Found maped subgraph isomorphic");
//            UtilProccess.printArray(currentPermutation);
            System.out.println("Isomorfismo:");
            for (int i = 0; i < currentPermutation.length; i++) {
                System.out.printf("f(%d)=%s\n", i, abindex.get(currentPermutation[i]));
            }
        } else {
            System.out.println("Sem isomorfismo");
        }
    }

    public static String getEdgeStringFormated(UndirectedSparseGraphTO graph, List verts) {
        StringBuilder sb = new StringBuilder();
        try {
            Collection<Pair<Integer>> pairs = graph.getPairs();
            for (Pair<Integer> par : pairs) {
                sb.append(verts.get(par.getFirst()))
                        .append("-").append(
                        verts.get(par.getSecond()))
                        .append(",");
            }
            for (Integer v : (Collection<Integer>) graph.getVertices()) {
                if (graph.degree(v) == 0) {
                    sb.append(verts.get(v)).append(",");
                }
            }
        } catch (Exception e) {

        }
        return sb.toString();
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
