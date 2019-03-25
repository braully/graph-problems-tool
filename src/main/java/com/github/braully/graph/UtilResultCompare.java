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
public class UtilResultCompare {

    public static void main(String... args) throws Exception {
        Options options = new Options();

        Option input = new Option("i", "input", true, "input file path");
        input.setRequired(false);
        options.addOption(input);

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
            inputFilePath = com.github.braully.graph.DatabaseFacade.DATABASE_DIRECTORY + "Dropbox/documentos/mestrado/resultado-processamento-grafos/resultado-quartic-ht.txt";

//            inputFilePath = com.github.braully.graph.DatabaseFacade.DATABASE_DIRECTORY + "Dropbox/documentos/mestrado/resultado-processamento-grafos/resultado-mft-parcial-ht.txt";
//            inputFilePath = com.github.braully.graph.DatabaseFacade.DATABASE_DIRECTORY + "Dropbox/documentos/mestrado/resultado-processamento-grafos/resultado-highlyirregular-ht.txt";
//            inputFilePath = com.github.braully.graph.DatabaseFacade.DATABASE_DIRECTORY + "Documentos/grafos-processados/mtf/resultado-ht.txt";
//            inputFilePath = com.github.braully.graph.DatabaseFacade.DATABASE_DIRECTORY + "Dropbox/documentos/mestrado/resultado-processamento-grafos/resultado-hypo-parcial-ht.txt";
//            inputFilePath = com.github.braully.graph.DatabaseFacade.DATABASE_DIRECTORY + "Dropbox/documentos/mestrado/resultado-processamento-grafos/resultado-eul-ht.txt";
//            inputFilePath = com.github.braully.graph.DatabaseFacade.DATABASE_DIRECTORY + "Dropbox/documentos/mestrado/resultado-processamento-grafos/resultado-almhypo-ht.txt";
//            inputFilePath = com.github.braully.graph.DatabaseFacade.DATABASE_DIRECTORY + "Dropbox/documentos/mestrado/resultado-processamento-grafos/resultado-Almost_hypohamiltonian_graphs_cubic-parcial-ht.txt";
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
            String[] parts1 = readLine.split("\t");
            String[] parts2 = null;
            if ((readLine = r.readLine()) != null) {
                parts2 = readLine.split("\t");
            }
            try {
                if (parts1 != null && parts1.length >= 6
                        && parts2 != null && parts2.length >= 6) {
                    String grupo1 = parts1[0];
                    String idgrafo1 = parts1[1];
                    String nverticestr1 = parts1[2];
                    String operacao1 = parts1[3];
                    String resultao1 = parts1[4];
                    String tempo1 = parts1[5];
                    double tdouble1 = Double.parseDouble(tempo1);

                    String grupo2 = parts2[0];
                    String idgrafo2 = parts2[1];
                    String nverticestr2 = parts2[2];
                    String operacao2 = parts2[3];
                    String resultao2 = parts2[4];
                    String tempo2 = parts2[5];
                    double tdouble2 = Double.parseDouble(tempo2);

                    if (grupo1.equals(grupo2) && idgrafo1.equals(idgrafo2)
                            && operacao1.equals("Nº Caratheodory (Heuristic v1)")
                            && operacao2.equals("Nº Caratheodory (Binary Java)")) {
                        Integer resultado1 = null;
                        Integer resultado2 = null;
                        try {
                            resultado2 = Integer.parseInt(resultao2);
                            resultado1 = Integer.parseInt(resultao1);
                        } catch (Exception e) {
                        }

                        addResult(grupo1, idgrafo1, Integer.parseInt(nverticestr1),
                                resultado1, resultado2, tdouble1, tdouble2);
//                        addResult(nverticestr1 + "_-order", idgrafo1, Integer.parseInt(nverticestr1),
//                                resultado1, resultado2, tdouble1, tdouble2);
                    }

//                    addResult(, Integer.parseInt(nverticestr), parts[2], tdouble);
//                    addResult(parts[1] + "_-order", Integer.parseInt(parts[1]), parts[2], Integer.parseInt(parts[3]));
                }
            } catch (Exception e) {
            }
        }
        printResultadoConsolidado();
    }

    private static void addResult(String grafo, String id, int nvertices, Integer ncarat,
            Integer resultado2, double tdouble1, double tdouble2) {
        String key = grafo.trim() + "-" + nvertices;
        ResultadoLinha r = resultados.get(key);
        if (r == null) {
            r = new ResultadoLinha();
            r.nome = grafo;
            r.numvertices = nvertices;
            resultados.put(key, r);
        }
        if (ncarat == null) {
            ncarat = 0;
        } else {
            r.addDiference(ncarat, resultado2);
        }
        r.addResultado1(ncarat, tdouble1);
        r.addResultado2(resultado2, tdouble2);
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
        System.out.print("T Normal(s)");
        System.out.print("\t");
        System.out.print("T Heuristic(s)");
        System.out.print("\t");
        System.out.print("Pior");
        System.out.print("\t");
        System.out.print("Media");
        System.out.print("\t");
        System.out.print("Melhor");
        System.out.print("\t");
        System.out.print("Min");
        System.out.print("\t");
        System.out.print("Max");
        System.out.print("\t");
        System.out.print("Erro");
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
//                            addResult("planar_conn", r.id, Integer.parseInt(r.vertices), Integer.parseInt(strNumCarat), resultado2, tdouble1, tdouble2);
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
        long erros;
        int max1;
        int min1;
        int max2;
        int min2;
        double totalTime1;
        double totalTime2;
        long diffAc;
        long diff;
        long worst;
        long best;
        Map<Integer, Integer> totalPorNum = new HashMap<>();

        public void printResultado() {
            double media = ((double) diffAc / (double) diff);
            System.out.print(nome);
            System.out.print("\t");
            System.out.print(numvertices);
            System.out.print("\t");
            System.out.print(cont);
            System.out.print("\t");
            System.out.print(String.format("%.2f", totalTime2));
            System.out.print("\t");
            System.out.print(String.format("%.2f", totalTime1));
            System.out.print("\t");
            System.out.print(worst);
            System.out.print("\t");
            System.out.print(String.format("%.2f", media));
            System.out.print("\t");
            System.out.print(best);
            System.out.print("\t");
            System.out.print(min2 - min1);
            System.out.print("\t");
            System.out.print(max2 - max1);
            System.out.print("\t");

            Integer tmp = totalPorNum.get(0);
            if (tmp != null) {
                System.out.print(tmp);
            } else {
                System.out.print("0");
            }
            System.out.print("\t");

            for (int i = 2; i <= maxCarat; i++) {
                tmp = totalPorNum.get(i);
                if (tmp != null) {
                    System.out.print(tmp);
                } else {
                    System.out.print("0");
                }
                System.out.print("\t");
            }
            System.out.println("");
        }

        public void addResultado1(int ncarat, double t1) {
            totalTime1 += t1;
            if (ncarat == 0) {
                erros++;
            } else {
                if (ncarat > max1) {
                    max1 = ncarat;
                }
                if (min1 == 0 || ncarat < min1) {
                    min1 = ncarat;
                }
            }
            Integer tparcial = totalPorNum.get(ncarat);
            if (tparcial == null) {
                tparcial = 0;
            }
            if (ncarat > maxCarat) {
                maxCarat = ncarat;
            }
            if (ncarat > 0) {
                totalPorNum.put(ncarat, (tparcial - 1));
            } else {
                totalPorNum.put(ncarat, (tparcial + 1));
            }
        }

        public void addResultado2(int ncarat, double t2) {
            totalTime2 += t2;
            cont++;
            if (ncarat > max2) {
                max2 = ncarat;
            }
            if (min2 == 0 || ncarat < min2) {
                min2 = ncarat;
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

        public void addDiference(int r1, int r2) {
            long tmpdiff = (r2 - r1);
            if (tmpdiff > worst) {
                worst = tmpdiff;
            }
            if (best == 0 || tmpdiff < best) {
                best = tmpdiff;
            }
            diffAc += tmpdiff;
            diff++;
        }
    };
}
