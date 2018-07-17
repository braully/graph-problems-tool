/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.braully.graph.operation.IGraphOperation;
import java.io.BufferedWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;

/**
 * @author braully
 */
public class DatabaseFacade {

    public static final String DATABASE_DIRECTORY = System.getProperty("user.home") + File.separator + "." + "graph-problem";
    public static final String BATCH_DIRECTORY = DATABASE_DIRECTORY + File.separator + "batch";
    public static final String DATABASE_URL = DATABASE_DIRECTORY + File.separator + "graph-problem-results.json";
    public static final String DATABASE_DIRECTORY_GRAPH = DATABASE_DIRECTORY + File.separator + "graph";
    public static final String DATABASE_DIRECTORY_CONSOLE = DATABASE_DIRECTORY + File.separator + "console";

    static {
        try {
            new File(DATABASE_DIRECTORY).mkdirs();
            new File(BATCH_DIRECTORY).mkdirs();
            new File(DATABASE_DIRECTORY_GRAPH).mkdirs();
            new File(DATABASE_DIRECTORY_CONSOLE).mkdirs();
        } catch (Exception e) {

        }
    }

    static List<UndirectedSparseGraphTO> getAllGraphsBatchDiretory() {
        List<UndirectedSparseGraphTO> graphTOs = new ArrayList<>();
        try {
            File dir = new File(BATCH_DIRECTORY);
            File[] filesList = dir.listFiles();
            for (File file : filesList) {
                String name = file.getName();
                if (name.toLowerCase().endsWith(".csr")) {
                    UndirectedSparseGraphTO loadGraphCsr = UtilGraph.loadGraphCsr(new FileInputStream(file));
                    loadGraphCsr.setName(name);
                    graphTOs.add(loadGraphCsr);
                } else if (name.toLowerCase().endsWith(".mat")) {
                    UndirectedSparseGraphTO loadGraphAdjMatrix = UtilGraph.loadGraphAdjMatrix(new FileInputStream(file));
                    loadGraphAdjMatrix.setName(name);
                    graphTOs.add(loadGraphAdjMatrix);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return graphTOs;
    }

    private static void removeBatchDiretoryIfExists(UndirectedSparseGraphTO graph) {
        try {
            String name = graph.getName();
            File dir = new File(BATCH_DIRECTORY);
            File[] filesList = dir.listFiles();
            for (File file : filesList) {
                if (file.getName().equals(name)) {
                    file.delete();
                }
            }
        } catch (Exception e) {

        }
    }

    static class RecordResultGraph implements java.lang.Comparable<RecordResultGraph> {

        String id, status, type, operation, graph, name, vertices, edges, results, date, console;

        public RecordResultGraph() {
        }

        public RecordResultGraph(Map param) {
            if (param != null) {
                try {
                    this.id = param.get("id").toString();
                } catch (Exception e) {
                }
                try {
                    this.status = param.get("status").toString();
                } catch (Exception e) {
                }
                try {
                    this.type = param.get("type").toString();
                } catch (Exception e) {
                }
                try {
                    this.operation = param.get("operation").toString();
                } catch (Exception e) {
                }
                try {
                    this.graph = param.get("graph").toString();
                } catch (Exception e) {
                }
                try {
                    this.name = param.get("name").toString();
                } catch (Exception e) {
                }
                try {
                    this.vertices = param.get("vertices").toString();
                } catch (Exception e) {
                }
                try {
                    this.edges = param.get("edges").toString();
                } catch (Exception e) {
                }
                try {
                    this.results = param.get("results").toString();
                } catch (Exception e) {
                }
                try {
                    this.date = param.get("date").toString();
                } catch (Exception e) {
                }
                try {
                    this.console = param.get("console").toString();
                } catch (Exception e) {
                }
            }
        }

        public RecordResultGraph(String status, String type,
                String operation, String graph, String vertices,
                String edges, String results, String date, String console) {
            this.status = status;
            this.type = type;
            this.operation = operation;
            this.graph = graph;
            this.vertices = vertices;
            this.edges = edges;
            this.results = results;
            this.date = date;
            this.console = console;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public String getGraph() {
            return graph;
        }

        public void setGraph(String graph) {
            this.graph = graph;
        }

        public String getVertices() {
            return vertices;
        }

        public void setVertices(String vertices) {
            this.vertices = vertices;
        }

        public String getEdges() {
            return edges;
        }

        public void setEdges(String edges) {
            this.edges = edges;
        }

        public String getResults() {
            return results;
        }

        public void setResults(String results) {
            this.results = results;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        @Override
        public int compareTo(RecordResultGraph t) {
            if (t != null && id != null) {
                return id.compareToIgnoreCase(t.id);
            }
            return 0;
        }

    }

    public synchronized static List<RecordResultGraph> getAllResults() {
        return getAllResults(DATABASE_URL);
    }

    public synchronized static List<RecordResultGraph> getAllResults(String database) {
        List<RecordResultGraph> results = new ArrayList();
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<Map> tmp = mapper.readValue(new File(database), List.class);
            if (tmp != null) {
                try {
                    Iterator<Map> iterator = tmp.iterator();
                    while (iterator.hasNext()) {
                        RecordResultGraph t = new RecordResultGraph(iterator.next());
                        results.add(t);
                    }
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }
            Collections.reverse(results);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    public static synchronized void saveResult(UndirectedSparseGraphTO graph,
            IGraphOperation graphOperation,
            Map<String, Object> result, List<String> consoleOut) {
        if (result != null && graph != null && graphOperation != null) {
            try {

                List<RecordResultGraph> results = null;
                ObjectMapper mapper = new ObjectMapper();
                try {
                    results = mapper.readValue(new File(DATABASE_URL), List.class);
                } catch (Exception e) {

                }
                if (results == null) {
                    results = new ArrayList<RecordResultGraph>();
                }
                RecordResultGraph record = new RecordResultGraph();
                String fileGraph = saveGraph(graph);
                String fileConsole = saveConsole(consoleOut, fileGraph);
                record.status = "ok";
                record.graph = fileGraph;
                record.name = graph.getName();
                record.edges = "" + graph.getEdgeCount();
                record.vertices = "" + graph.getVertexCount();
                record.operation = graphOperation.getName();
                record.type = graphOperation.getTypeProblem();
                record.date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
                record.results = resultMapToString(result);
                record.id = "" + results.size();
                record.console = fileConsole;
                results.add(record);
                mapper.writeValue(new File(DATABASE_URL), results);
                removeBatchDiretoryIfExists(graph);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String saveGraph(UndirectedSparseGraphTO graph) {
        if (graph == null) {
            return null;
        }
        String fileName = graph.getName();

        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
//        try {
//            MessageDigest md5 = MessageDigest.getInstance("MD5");
//            md5.
//        } catch (Exception e) {
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
//        }
        fileName = fileName + "-" + sb.toString() + ".json";

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File(DATABASE_DIRECTORY_GRAPH + File.separator + fileName), graph);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    private static String saveConsole(List<String> consoleOut, String fileGraphName) {
        if (consoleOut == null || consoleOut.isEmpty()) {
            return null;
        }
        String fileName = fileGraphName;

        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }

        fileName = fileGraphName + ".log";
        File fileOut = new File(DATABASE_DIRECTORY_GRAPH + File.separator + fileName);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileOut));
            for (String l : consoleOut) {
                bw.write(l);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    public static UndirectedSparseGraphTO openGraph(String nameFile) throws IOException {
        if (nameFile == null) {
            return null;
        }
        UndirectedSparseGraphTO graph = null;
        ObjectMapper mapper = new ObjectMapper();
        graph = mapper.readValue(new File(DATABASE_DIRECTORY_GRAPH + File.separator + nameFile), UndirectedSparseGraphTO.class);
        return graph;
    }

    private static String resultMapToString(Map<String, Object> result) {
        StringBuilder strResult = new StringBuilder();
        if (result != null) {
            Set<String> keySet = result.keySet();
            List<String> keyList = new ArrayList<String>(keySet);
            Collections.sort(keyList);
            for (String strKey : keyList) {
                Object obj = result.get(strKey);
                strResult.append(strKey);
                strResult.append(": ");
                strResult.append(objectTosString(obj));
                strResult.append("\n");
            }
        }
        return strResult.toString();
    }

    private static String objectTosString(Object obj) {
        String ret = null;
        if (obj != null) {
            ret = obj.toString();
        }
        return ret;
    }

    public static void allResultsZiped(OutputStream out) throws IOException {
        ZipArchiveOutputStream tOut = null;
        try {
            tOut = new ZipArchiveOutputStream(out);
            addFileToZip(tOut, DATABASE_DIRECTORY, "");
        } finally {
            tOut.flush();
            tOut.close();
        }
    }

    /*
    http://hubpages.com/technology/Zipping-and-Unzipping-Nested-Directories-in-Java-using-Apache-Commons-Compress
     */
    private static void addFileToZip(ZipArchiveOutputStream zOut, String path, String base) throws IOException {
        File f = new File(path);
        String entryName = base + f.getName();
        ZipArchiveEntry zipEntry = new ZipArchiveEntry(f, entryName);

        zOut.putArchiveEntry(zipEntry);

        if (f.isFile()) {
            FileInputStream fInputStream = null;
            try {
                fInputStream = new FileInputStream(f);
                IOUtils.copy(fInputStream, zOut);
                zOut.closeArchiveEntry();
            } finally {
                IOUtils.closeQuietly(fInputStream);
            }

        } else {
            zOut.closeArchiveEntry();
            File[] children = f.listFiles();

            if (children != null) {
                for (File child : children) {
                    addFileToZip(zOut, child.getAbsolutePath(), entryName + "/");
                }
            }
        }
    }
}
