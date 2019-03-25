/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.cli.*;

/**
 *
 * @author Braully Rocha da Silva
 */
public class UtilResult {

    public static boolean verbose = false;

    public static void main(String... args) throws Exception {
        Options options = new Options();

        Option input = new Option("i", "input", true, "input file path");
        input.setRequired(false);
        options.addOption(input);

        Option verb = new Option("v", "verbose", false, "verbose process");
        input.setRequired(false);
        options.addOption(verb);

        Option output = new Option("o", "output", true, "output file");
        output.setRequired(false);
        options.addOption(output);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("UtilResult", options);

            System.exit(1);
            return;
        }

        String inputFilePath = cmd.getOptionValue("input");
        if (inputFilePath == null) {
            inputFilePath = com.github.braully.graph.DatabaseFacade.DATABASE_DIRECTORY + "grafos-para-processar/mft2/resultado.txt";
        }
        if (inputFilePath != null) {
            if (inputFilePath.toLowerCase().endsWith(".txt")) {
                processFileTxt(inputFilePath);
            } else if (inputFilePath.toLowerCase().endsWith(".json")) {
                processFileJson(inputFilePath);
            }
        }
    }

    private static void processFileTxt(String inputFilePath) throws FileNotFoundException, IOException {
        File file = null;
        if (inputFilePath == null || !(file = new File(inputFilePath)).isFile()) {
            System.err.println("File is invalid: " + inputFilePath);
            return;
        }
        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String readLine = null;
        while ((readLine = r.readLine()) != null) {
            String[] parts = readLine.split("\t");
            try {
                if (parts != null && parts.length > 3) {
                    addResult(parts[0], Integer.parseInt(parts[1]), parts[2], Integer.parseInt(parts[3]));
//                    addResult(parts[1] + "_-order", Integer.parseInt(parts[1]), parts[2], Integer.parseInt(parts[3]));
                }
            } catch (Exception e) {

            }
        }
        printResultadoConsolidado();
    }

    private static void addResult(String grafo, int nvertices, String id, int ncarat) {
        String key = grafo.trim() + "-" + nvertices;
        ResultadoLinha r = resultados.get(key);
        if (r == null) {
            r = new ResultadoLinha();
            r.nome = grafo;
            r.numvertices = nvertices;
            resultados.put(key, r);
        }
        r.addResultado(ncarat);
    }

    public static Map<String, ResultadoLinha> resultados = new HashMap<String, ResultadoLinha>();
    static int maxCarat = 0;

    private static void printResultadoConsolidado() {
        Set<String> keys = resultados.keySet();
        List<String> listKeys = new ArrayList<>(keys);
        Collections.sort(listKeys);

        System.out.print("Grafo");
        System.out.print("\t");
        System.out.print("Nº Vert");
        System.out.print("\t");
        System.out.print("Quantidade");
        System.out.print("\t");
        System.out.print("Min");
        System.out.print("\t");
        System.out.print("Max");
        System.out.print("\t");
        for (int i = 2; i <= maxCarat; i++) {
            System.out.print("QNC" + i);
            System.out.print("\t");
        }
        System.out.println("");

        for (String key : listKeys) {
            resultados.get(key).printResultado();
        }
    }

    private static void processFileJson(String inputFilePath) {

        Pattern pattern = Pattern.compile("^Caratheodroy number.*?: (\\d+)");
        System.out.println("Opening Results");
        List<DatabaseFacade.RecordResultGraph> allResults = DatabaseFacade.getAllResults(inputFilePath);
        System.out.println("Results open");
        System.out.println("Total results: " + allResults.size());
        if (allResults != null) {
            for (DatabaseFacade.RecordResultGraph r : allResults) {
                try {
                    if (r.operation.equals("Nº Caratheodory (Binary Java)") && r.graph.startsWith("planar_conn")) {

                        Matcher matcher = pattern.matcher(r.results);
                        if (matcher.find()) {
                            String strNumCarat = matcher.group(1);
//                            System.out.print(strNumCarat);
//                            System.out.print(" from result: ");
//                            System.out.print(r.results);
                            addResult("planar_conn", Integer.parseInt(r.vertices), r.id, Integer.parseInt(strNumCarat));
                        } else {
//                            System.out.println("Not found");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        printResultadoConsolidado();
    }

    static class ResultadoLinha {

        String nome;
        int numvertices;
        long cont;
        int max;
        int min;
        Map<Integer, Integer> totalPorNum = new HashMap<>();

        public void printResultado() {
            System.out.print(nome);
            System.out.print("\t");
            System.out.print(numvertices);
            System.out.print("\t");
            System.out.print(cont);
            System.out.print("\t");
            System.out.print(min);
            System.out.print("\t");
            System.out.print(max);
            System.out.print("\t");
            for (int i = 2; i <= maxCarat; i++) {
                Integer tmp = totalPorNum.get(i);
                if (tmp != null) {
                    System.out.print(tmp);
                } else {
                    System.out.print("0");
                }
                System.out.print("\t");
            }
            System.out.println("");
        }

        public void addResultado(int ncarat) {
            cont++;
            if (ncarat > max) {
                max = ncarat;
            }
            if (min == 0 || ncarat < min) {
                min = ncarat;
            }
            Integer tparcial = totalPorNum.get(ncarat);
            if (tparcial == null) {
                tparcial = 0;
            }
            if (ncarat > maxCarat) {
                maxCarat = ncarat;
            }
            totalPorNum.put(ncarat, (tparcial + 1));
        }
    };
}
