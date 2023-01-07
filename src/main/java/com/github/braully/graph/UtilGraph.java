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

    private static String inputFilePath = com.github.braully.graph.DatabaseFacade.DATABASE_DIRECTORY + "tmp/grafos/converter";
    private static String outputFilePath = com.github.braully.graph.DatabaseFacade.DATABASE_DIRECTORY + "tmp/grafos/convertidos";

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

    public static UndirectedSparseGraphTO<Integer, Integer> loadBigDataset(InputStream streamNodes, InputStream edgesStream) throws IOException {
        UndirectedSparseGraphTO<Integer, Integer> ret = null;
        if (streamNodes != null && edgesStream != null) {
            try {
                ret = new UndirectedSparseGraphTO<Integer, Integer>();
                BufferedReader rnodes = new BufferedReader(new InputStreamReader(streamNodes));
                String readLine = null;
                while ((readLine = rnodes.readLine()) != null
                        && !(readLine = readLine.trim()).startsWith("#")) {
                    Integer v = Integer.parseInt(readLine);
                    ret.addVertex(v - 1);
                }
                BufferedReader redges = new BufferedReader(new InputStreamReader(edgesStream));
                readLine = null;
                while ((readLine = redges.readLine()) != null
                        && !(readLine = readLine.trim()).isEmpty()
                        && !readLine.startsWith("#")) {
                    String[] split = readLine.split(",");
                    if (split.length >= 2) {
                        Integer v = Integer.parseInt(split[0].trim()) - 1;
                        Integer t = Integer.parseInt(split[1].trim()) - 1;
                        ret.addEdge(v, t);
                    }
                }
            } catch (Exception e) {

            }
        }
        return ret;
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

    public static UndirectedSparseGraphTO<Integer, Integer> loadGraphAdjList(InputStream uploadedInputStream) throws IOException {
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
                        readLine = readLine.replaceAll("\\s\\s+", " ");
                        lines.add(readLine);
//                        System.out.println(readLine);
                        String[] split = readLine.trim().split(" ");
                        if (split != null && split.length > 0) {
                            ret.addVertex(Integer.parseInt(split[0].trim()));
                            verticeCount = verticeCount + 1;
                        }
                    }
                }
                int edgeCount = 0;
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
//                    System.out.println("Line: " + line);
                    if (line != null) {
                        String[] split = line.trim().split(" ");
//                        System.out.print("s= " + split[0]);
                        if (split != null && split.length > 1) {
                            Integer s = Integer.parseInt(split[0].trim());
//                            System.out.print("t= ");
                            for (int j = 1; j < split.length; j++) {
//                                System.out.print(split[j]);
//                                System.out.print(", ");
                                Integer t = Integer.parseInt(split[j].trim());
                                ret.addEdge(edgeCount++, s, t);
                            }
                        }
//                        System.out.println();
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
//        int n = br6.get_number();
        int n = br6.getGraphSize();
//        System.out.println("n=" + n);
//        System.out.println("n2=" + br6.getGraphSize());

        int numEdge = 0;

        graph = new UndirectedSparseGraphTO(n);

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
//http://users.cecs.anu.edu.au/~bdm/data/formats.html

        static final int BIAS6 = 63;
        static final int MAXBYTE = 126;
        static final int SMALLN = 62;
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

        public int getGraphSize() {
            return graphsize(mBytes);
        }

        public int graphsize(byte[] s) {
            int ip = 0;
            int n;

            if (s[0] == ':' || s[0] == '&') {
                ip++;
            }

            n = s[ip++] - BIAS6;

            if (n > SMALLN) {
                n = s[ip++] - BIAS6;
                if (n > SMALLN) {
                    n = s[ip++] - BIAS6;
                    n = (n << 6) | (s[ip++] - BIAS6);
                    n = (n << 6) | (s[ip++] - BIAS6);
                    n = (n << 6) | (s[ip++] - BIAS6);
                    n = (n << 6) | (s[ip++] - BIAS6);
                    n = (n << 6) | (s[ip++] - BIAS6);
                } else {
                    n = (n << 6) | (s[ip++] - BIAS6);
                    n = (n << 6) | (s[ip++] - BIAS6);
                }
            }
            mPos = ip;
            return n;
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

    public static UndirectedSparseGraphTO loadGraph(File file) {
        UndirectedSparseGraphTO<Integer, Integer> ret = null;
        try {
            String fileName = file.getName();
            InputStream uploadedInputStream = new FileInputStream(file);
            if (fileName != null && !fileName.trim().isEmpty()) {
                String tmpFileName = fileName.trim().toLowerCase();
                if (tmpFileName.endsWith("csr")) {
                    ret = UtilGraph.loadGraphCsr(uploadedInputStream);
                } else if (tmpFileName.endsWith("mat")) {
                    ret = UtilGraph.loadGraphAdjMatrix(uploadedInputStream);
                } else if (tmpFileName.endsWith("g6")) {
                    ret = UtilGraph.loadGraphG6(uploadedInputStream);
                } else if (tmpFileName.endsWith("es")) {
                    ret = UtilGraph.loadGraphES(uploadedInputStream);
                } else if (tmpFileName.endsWith("adj")) {
                    ret = UtilGraph.loadGraphAdjList(uploadedInputStream);
                }
                if (ret != null) {
                    ret.setName(fileName);
                }
            }
        } catch (Exception e) {
            log.error("fail on load graph", e);
        }
        return ret;
    }
}
