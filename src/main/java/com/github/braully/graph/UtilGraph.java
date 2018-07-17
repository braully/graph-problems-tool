/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 * Graphs utils
 *
 * @author Braully
 */
public class UtilGraph {

    private static final Logger logWebconsole = Logger.getLogger("WEBCONSOLE");
    private static final Logger log = Logger.getLogger(UtilGraph.class);

    private static String inputFilePath = "/home/strike/tmp/grafos/converter";
    private static String outputFilePath = "/home/strike/tmp/grafos/convertidos";

    public static void main(String... args) throws Exception {
        processDirectory(null, null);
    }

    public static void processDirectory(File file, String[] excludes)
            throws FileNotFoundException, NumberFormatException, IOException {

        File ftmp = new File(inputFilePath);
        if (ftmp.exists() && ftmp.isDirectory()) {
            File[] files = ftmp.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    if (file != null && !file.isDirectory()) {
                        return true;
                    }
                    return false;
                }
            });
            if (files != null) {
                for (File f : files) {
                    try {
                        UndirectedSparseGraphTO<Integer, Integer> undGraph = loadGraphAdjMatrix(new FileInputStream(f));
                        if (undGraph.getVertexCount() > 0) {
                            FileWriter filew = new FileWriter(new File(outputFilePath, f.getName() + ".csr"));
                            writerGraphToCsr(filew, undGraph);
                            filew.flush();
                            filew.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return;
        }
    }

    public static synchronized String saveTmpFileGraphInCsr(UndirectedSparseGraphTO<Integer, Integer> undGraph) {
        String strFile = null;
        if (undGraph != null && undGraph.getVertexCount() > 0) {
            try {
                int vertexCount = undGraph.getVertexCount();
                File file = File.createTempFile("graph-csr-", ".txt");
                file.deleteOnExit();

                strFile = file.getAbsolutePath();
                FileWriter writer = new FileWriter(file);
                writerGraphToCsr(writer, undGraph);
                writer.close();
            } catch (IOException ex) {
                log.error(null, ex);
            }
        }
        log.info("File tmp graph: " + strFile);
        return strFile;
    }

    public static synchronized void writerGraphToCsr(Writer writer, UndirectedSparseGraphTO<Integer, Integer> undGraph) throws IOException {
        if (undGraph == null || writer == null) {
            return;
        }
        List<Integer> vertices = (List<Integer>) undGraph.getVertices();
        int vertexCount = undGraph.getVertexCount();
        writer.write("#Graph |V| = " + vertexCount + "\n");

        List<Integer> csrColIdxs = new ArrayList<>();
        List<Integer> rowOffset = new ArrayList<>();

        int idx = 0;
        for (Integer i : vertices) {
            csrColIdxs.add(idx);
            Collection<Integer> neighbors = undGraph.getNeighbors(i);
            Set<Integer> neighSet = new HashSet<>();
            neighSet.addAll(neighbors);
            for (Integer vn : neighSet) {
                if (!vn.equals(i)) {
                    rowOffset.add(vertices.indexOf(vn));
                    idx++;
                }
            }
        }
        csrColIdxs.add(idx);

        for (Integer i : csrColIdxs) {
            writer.write("" + i);
            writer.write(" ");
        }
        writer.write("\n");
        for (Integer i : rowOffset) {
            writer.write("" + i);
            writer.write(" ");
        }
        writer.write("\n");
    }

    public static synchronized List<Integer> csrColIdxs(UndirectedSparseGraphTO<Integer, Integer> undGraph) {
        if (undGraph == null) {
            return null;
        }
        Collection<Integer> vertices = undGraph.getVertices();
        List<Integer> csrColIdxs = new ArrayList<>();
        List<Integer> rowOffset = new ArrayList<>();

        int idx = 0;
        for (Integer i : vertices) {
            csrColIdxs.add(idx);
            Collection<Integer> neighbors = undGraph.getNeighbors(i);
            Set<Integer> neighSet = new HashSet<>();
            neighSet.addAll(neighbors);
            for (Integer vn : neighSet) {
                if (!vn.equals(i)) {
                    rowOffset.add(vn);
                    idx++;
                }
            }
        }
        csrColIdxs.add(idx);
        return csrColIdxs;
    }

    public static synchronized List<Integer> rowOffset(UndirectedSparseGraphTO<Integer, Integer> undGraph) {
        if (undGraph == null) {
            return null;
        }
        int vertexCount = undGraph.getVertexCount();
        List<Integer> csrColIdxs = new ArrayList<>();
        List<Integer> rowOffset = new ArrayList<>();

        int idx = 0;
        for (Integer i = 0; i < vertexCount; i++) {
            csrColIdxs.add(idx);
            Collection<Integer> neighbors = undGraph.getNeighbors(i);
            Set<Integer> neighSet = new HashSet<>();
            neighSet.addAll(neighbors);
            for (Integer vn : neighSet) {
                if (!vn.equals(i)) {
                    rowOffset.add(vn);
                    idx++;
                }
            }
        }
        csrColIdxs.add(idx);
        return rowOffset;
    }

    static String saveTmpFileGraphInAdjMatrix(UndirectedSparseGraphTO<Integer, Integer> graph) {
        String strFile = null;
        if (graph != null && graph.getVertexCount() > 0) {
            try {
                int vertexCount = graph.getVertexCount();
                File file = File.createTempFile("graph-csr-", ".txt");
                file.deleteOnExit();

                strFile = file.getAbsolutePath();
                FileWriter writer = new FileWriter(file);
                writerGraphToAdjMatrix(writer, graph);
                writer.close();
            } catch (IOException ex) {
                log.error(null, ex);
            }
        }
        log.info("File tmp graph: " + strFile);
        return strFile;
    }

    public static synchronized void writerGraphToAdjMatrix(Writer writer, UndirectedSparseGraphTO<Integer, Integer> undGraph) throws IOException {
        if (undGraph == null || writer == null) {
            return;
        }
        int vertexCount = undGraph.getVertexCount();
//        writer.write("#Graph |V| = " + vertexCount + "\n");

        for (Integer i = 0; i < vertexCount; i++) {
            Collection<Integer> neighbors = undGraph.getNeighbors(i);
            for (Integer j = 0; j < vertexCount; j++) {
                if (neighbors.contains(j)) {
                    writer.write("1");
                } else {
                    writer.write("0");
                }
                if (j < vertexCount - 1) {
                    writer.write(" ");
                }
            }
            if (i < vertexCount - 1) {
                writer.write("\n");
            }
        }
        writer.write("\n");
    }

    static UndirectedSparseGraphTO<Integer, Integer> loadGraphCsr(InputStream uploadedInputStream) throws IOException {
        UndirectedSparseGraphTO<Integer, Integer> ret = null;
        try {
            if (uploadedInputStream != null) {
                BufferedReader r = new BufferedReader(new InputStreamReader(uploadedInputStream));
                String csrColIdxsStr = null;
                String rowOffsetStr = null;

                String readLine = null;
                while ((readLine = r.readLine()) == null || readLine.trim().isEmpty() || readLine.trim().startsWith("#")) {
                }
                csrColIdxsStr = readLine;
                while ((readLine = r.readLine()) == null || readLine.trim().isEmpty() || readLine.trim().startsWith("#")) {
                }
                rowOffsetStr = readLine;
                if (csrColIdxsStr != null && !csrColIdxsStr.trim().isEmpty()
                        && rowOffsetStr != null && !rowOffsetStr.trim().isEmpty()) {
                    String[] csrColIdxsStrSplited = csrColIdxsStr.trim().split(" ");
                    String[] rowOffsetStrSplited = rowOffsetStr.trim().split(" ");
                    ret = new UndirectedSparseGraphTO<>();
                    int vertexCount = csrColIdxsStrSplited.length - 1;
                    int edgeCount = 0;
                    if (csrColIdxsStrSplited != null && csrColIdxsStrSplited.length > 0) {
                        for (int i = 0; i < vertexCount; i++) {
                            ret.addVertex(i);
                        }
                        for (int i = 0; i < vertexCount; i++) {
                            int ini = Integer.parseInt(csrColIdxsStrSplited[i]);
                            int fim = Integer.parseInt(csrColIdxsStrSplited[i + 1]);
                            for (; ini < fim; ini++) {
                                String strFim = rowOffsetStrSplited[ini];
                                ret.addEdge(edgeCount++, i, Integer.parseInt(strFim));
                            }
                        }
                    }
                }
//            System.out.println("CsrColIdxs: " + csrColIdxsStr);
//            System.out.println("RowOffset: " + rowOffsetStr);
            }
        } catch (Exception e) {
            log.error("error", e);
            logWebconsole.info("Error: format invalid --" + e.getLocalizedMessage());
        }
        return ret;
    }

    public static UndirectedSparseGraphTO<Integer, Integer> loadGraphAdjMatrix(InputStream uploadedInputStream) throws IOException {
        UndirectedSparseGraphTO<Integer, Integer> ret = null;
        try {
            if (uploadedInputStream != null) {
                BufferedReader r = new BufferedReader(new InputStreamReader(uploadedInputStream));
                List<String> lines = new ArrayList<>();
                String readLine = null;
                Integer verticeCount = 0;
                ret = new UndirectedSparseGraphTO<>();

                while ((readLine = r.readLine()) != null) {
                    if (!readLine.trim().isEmpty()
                            && !readLine.trim().startsWith("#")
                            && !readLine.trim().matches("\\D+.*")) {
                        lines.add(readLine);
//                        System.out.println(readLine);
                        ret.addVertex(verticeCount);
                        verticeCount = verticeCount + 1;
                    }
                }
                int edgeCount = 0;
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    if (line != null) {
                        String[] split = line.trim().split(" ");
                        if (split != null && split.length > 0 && line.trim().contains(" ")) {
                            for (int j = 0; j < split.length; j++) {
                                if ("1".equals(split[j])) {
                                    ret.addEdge(edgeCount++, i, j);
                                }
                            }
                        } else {
                            char[] charArray = line.trim().toCharArray();
                            if (charArray != null & charArray.length > 0) {
                                for (int j = 0; j < charArray.length; j++) {
                                    if ('1' == charArray[j]) {
                                        ret.addEdge(edgeCount++, i, j);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("error", e);
            logWebconsole.info("Error: format invalid --" + e.getLocalizedMessage());
        }
        return ret;
    }

    /*
     Code from:  https://github.com/bingmann/BispanningGame/blob/master/src/net/panthema/BispanningGame/Graph6.java
     */
    public static UndirectedSparseGraphTO<Integer, Integer> loadGraphG6(String strGraph) throws IOException {
        if (strGraph == null || strGraph.isEmpty()) {
            return null;
        }
        UndirectedSparseGraphTO graph = null;
        if (strGraph.charAt(0) == ':') {
            return loadGraphS6(strGraph.substring(1));
        }

        ByteReader6 br6 = new ByteReader6(strGraph);
        int n = br6.get_number();

        int numEdge = 0;

        graph = new UndirectedSparseGraphTO();

        for (int j = 1; j < n; ++j) {
            for (int i = 0; i < j; ++i) {
                int e = br6.get_bit();
                if (e != 0) {
                    graph.addEdge(numEdge++, i, j);
                }
            }
        }
        return graph;
    }

    public static UndirectedSparseGraphTO<Integer, Integer> loadGraphG6(InputStream uploadedInputStream) throws IOException {
        UndirectedSparseGraphTO ret = null;
        if (uploadedInputStream != null) {
            BufferedReader r = new BufferedReader(new InputStreamReader(uploadedInputStream));
            String readLine = null;
            while ((readLine = r.readLine()) == null || readLine.isEmpty()) {
            }
            ret = loadGraphG6(readLine);
        }
        return ret;
    }

    static class ByteReader6 {

        private byte[] mBytes;
        private int mSize, mPos, mBit;

        public ByteReader6(String s6) {
            mBytes = s6.getBytes();
            mSize = s6.length();
            mPos = mBit = 0;
        }

        // ! whether k bits are available
        boolean have_bits(int k) {
            return (mPos + (mBit + k - 1) / 6) < mSize;
        }

        // ! return the next integer encoded in graph6
        int get_number() {
            assert (mPos < mSize);

            byte c = mBytes[mPos];
            assert (c >= 63);
            c -= 63;
            ++mPos;

            if (c < 126) {
                return c;
            }

            assert (false);
            return 0;
        }

        // ! return the next bit encoded in graph6
        int get_bit() {
            assert (mPos < mSize);

            byte c = mBytes[mPos];
            assert (c >= 63);
            c -= 63;
            c >>= (5 - mBit);

            mBit++;
            if (mBit == 6) {
                mPos++;
                mBit = 0;
            }

            return (c & 0x01);
        }

        // ! return the next bits as an integer
        int get_bits(int k) {
            int v = 0;

            for (int i = 0; i < k; ++i) {
                v *= 2;
                v += get_bit();
            }

            return v;
        }
    }

    static UndirectedSparseGraphTO<Integer, Integer> loadGraphS6(String str) {
        ByteReader6 br6 = new ByteReader6(str);

        int numVertex = br6.get_number();
        int k = (int) Math.ceil(Math.log(numVertex) / Math.log(2));

        UndirectedSparseGraphTO g = new UndirectedSparseGraphTO();

        for (int i = 0; i < numVertex; ++i) {
            g.addVertex(i);
        }

        int v = 0, numEdge = 0;

        while (br6.have_bits(1 + k)) {
            int b = br6.get_bit();
            int x = br6.get_bits(k);

            if (x >= numVertex) {
                break;
            }

            if (b != 0) {
                v = v + 1;
            }
            if (v >= numVertex) {
                break;
            }

            if (x > v) {
                v = x;
            } else {
                g.addEdge(numEdge++, x, v);
            }
        }
        return g;
    }

    public static UndirectedSparseGraphTO<Integer, Integer> loadGraphES(InputStream uploadedInputStream) throws IOException {
        UndirectedSparseGraphTO<Integer, Integer> ret = null;
        if (uploadedInputStream != null) {
            ret = new UndirectedSparseGraphTO<>();
            BufferedReader r = new BufferedReader(new InputStreamReader(uploadedInputStream));
            String readLine = null;
            int countEdge = 0;
            while ((readLine = r.readLine()) != null) {
                String[] edges = null;
                if (readLine != null && !readLine.isEmpty() && (edges = readLine.trim().split(",")) != null) {
                    try {
                        for (String stredge : edges) {
                            String[] vs = stredge.split("-");
                            if (vs != null && vs.length >= 2) {
                                Integer source = Integer.parseInt(vs[0].trim());
                                Integer target = Integer.parseInt(vs[1].trim());
                                ret.addEdge(countEdge++, source, target);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        return ret;
    }
}
