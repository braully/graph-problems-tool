package util;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import edu.uci.ics.jung.graph.util.Pair;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author strike
 */
public class Processamento {

    static long OFFSET_ANOS = 360 * 24 * 60 * 60 * 1000;
    static long OFFSET_MESES = 30 * 24 * 60 * 60 * 1000;
    static long OFFSET_HORAS = 24 * 60 * 60 * 1000;
    static long OFFSET_DIAS = 60 * 60 * 1000;
    static long OFFSET_MINUTOS = 60 * 1000;


    /* Parametos */
    boolean verbose = false;
    boolean vebosePossibilidadesIniciais = false;
    boolean veboseFimEtapa = false;
    boolean verboseRankingOption = false;

    boolean rankearOpcoes = true;
    int rankearOpcoesProfundidade = 3;
    boolean rankearSegundaOpcoes = false;

    boolean anteciparVazio = true;
    boolean descartarOpcoesNaoOptimais = true;

    boolean falhaInRollBack = false;
    int falhaRollbackCount = 0;

    boolean falhaInCommitCount = false;
    int falhaCommitCount = 0;
    boolean failInviable = true;

    boolean compressPossiblidades = true;

    final boolean ordenarTrabalhoPorFazerPorPrimeiraOpcao = true;
    final boolean dumpResultadoPeriodicamente = true;

    /* Acompamnehto */
    long lastime = System.currentTimeMillis();
    int lastresult = 0;
    long lastime2 = System.currentTimeMillis();
    long[] rbcount = new long[4];

    /* Estado */
    UndirectedSparseGraphTO insumo;
    Collection<Integer> vertices;
    LinkedList<Integer> trabalhoPorFazer;
    LinkedList<Integer> trabalhoPorFazerOrigianl;
    Map<Integer, List<Integer>> caminhosPossiveis;
    Map<Integer, List<Integer>> caminhosPossiveisOriginal;
    TreeMap<Integer, Collection<Integer>> caminhoPercorrido = new TreeMap<>();
    Map<Integer, Map<Integer, List<Integer>>> historicoRanking = new TreeMap<>();
//    LinkedList<Integer> edegesAdded = new LinkedList<>();
    Map<Integer, TreeMap<Integer, Collection<Integer>>> pendencia = new HashMap<>();
    Map<Integer, BFSUtil> rankingTmp = new HashMap<>();

    int numArestasIniciais;
    int numVertices;
    int numAretasFinais;
    int len;
    int k;
    Integer trabalhoAtual;
    int marcoInicial;

    BFSUtil bfsalg;
    BFSUtil bfsRanking;
    BFSUtil bfsRankingSegundaOpcao;
    long longestresult = 12214;
    Integer melhorOpcaoLocal;


    /* */
    public String getEstrategiaString() {
        return (rankearSegundaOpcoes ? "rsot" : "rsof") + "-" + (rankearOpcoes ? "rt0t" : "rt0f")
                + rankearOpcoesProfundidade + "-" + (ordenarTrabalhoPorFazerPorPrimeiraOpcao ? "opft" : "otpff")
                + "-" + (descartarOpcoesNaoOptimais ? "dnot" : "dnof") + "-" + (anteciparVazio ? "avt" : "avf");
    }

    void loadGraph(String inputFilePath) {
        UndirectedSparseGraphTO graph = UtilGraph.loadGraph(new File(inputFilePath));
        loadGraph(graph);
    }

    private void loadGraph(UndirectedSparseGraphTO graph) {
        this.insumo = graph;
        this.vertices = (Collection<Integer>) graph.getVertices();
        this.trabalhoPorFazer = new LinkedList<>();
        this.caminhosPossiveis = new HashMap<>();
        this.caminhosPossiveisOriginal = new HashMap<>();
        this.caminhoPercorrido = new TreeMap<>();
        this.historicoRanking = new TreeMap<>();

        k = 0;
        for (Integer v : vertices) {
            int dg = graph.degree(v);
            if (dg > k) {
                k = dg;
            }
        }
        this.numVertices = vertices.size();
        this.numAretasFinais = ((k * k + 1) * k) / 2;
        this.numArestasIniciais = this.insumo.getEdgeCount();

        bfsalg = new BFSUtil(numVertices);
        bfsRanking = new BFSUtil(numVertices);
        bfsRankingSegundaOpcao = new BFSUtil(numVertices);
    }

    void loadLastCaminho() {
        loadCaminho(UtilProccess.getLastComb());
    }

    void loadCaminho(String loadProcess) {
        try {
            BufferedReader bf = new BufferedReader(new FileReader(loadProcess));
            String line = null;
            int count = insumo.getEdgeCount();
            while ((line = bf.readLine()) != null) {
                if (line != null && line.length() > 0) {
                    String[] args = line.split(" ");

                    String strpattern = "\\{(\\d+)\\}\\((\\d+),(\\d+)\\)(\\[[0-9,]+\\])?";
                    Pattern pattern = Pattern.compile(strpattern);
                    Arrays.sort(args, new Comparator<String>() {
                        Pattern ptn = Pattern.compile("\\{(\\d+)\\}");

                        public int compare(String t, String t1) {
                            Matcher mt = ptn.matcher(t);
                            Matcher mt1 = ptn.matcher(t1);
                            if (mt.find() && mt1.find()) {
                                String st = mt.group(1);
                                String st1 = mt1.group(1);
                                int tint = Integer.parseInt(st);
                                int tint1 = Integer.parseInt(st1);
                                return Integer.compare(tint, tint1);
                            }
                            throw new IllegalStateException("Incomparable stats");
                        }
                    });
                    for (String str : args) {
                        Matcher matcher = pattern.matcher(str);
                        if (verbose) {
                            System.out.print(str);
                            System.out.print("->");
                        }
                        if (matcher.matches()) {
                            Integer numEdge = Integer.parseInt(matcher.group(1));
                            Integer e1 = Integer.parseInt(matcher.group(2));
                            Integer e2 = Integer.parseInt(matcher.group(3));
                            List<Integer> caminho = UtilProccess.strToList(matcher.group(4));
                            Integer aresta = addEdge(e1, e2);
                            if (!numEdge.equals(aresta)) {
                                throw new IllegalStateException(String.format("Incorrect load info edge %d expected %d for: %s ", aresta, numEdge, str));
                            }
                            if (aresta != null) {
                                caminhoPercorrido.put(aresta, caminho);
                                if (verbose) {
                                    System.out.printf("e1=%d,e2=%d,e=%d:", e1, e2, aresta);
                                    System.out.print(caminho);
                                    System.out.print("  ");
                                }
                                if (insumo.degree(e1) == k) {
                                    trabalhoPorFazer.remove(e1);
                                }
                                if (insumo.degree(e2) == k) {
                                    trabalhoPorFazer.remove(e2);
                                }
                                Map<Integer, List<Integer>> rankingAtual = historicoRanking.getOrDefault(aresta, new HashMap<>());
                                historicoRanking.putIfAbsent(aresta, rankingAtual);
                            } else {
                                System.out.println("Ignored: " + str);
                            }
                        }
                    }
                }
            }
            System.out.print("Loaded " + (insumo.getEdgeCount() - count) + " edges added. ");
            printGraphCount();
        } catch (FileNotFoundException ex) {
            System.out.println("Não existe processamento anterior: " + loadProcess);
            System.out.println("Começando do zero");
        } catch (IOException ex) {
            Logger.getLogger(PipeGraph.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void prepareStart() {

        if (caminhosPossiveis == null || caminhosPossiveis.isEmpty()) {
            initialLoad();
        } else {
            System.out.println("Pre-started");
        }

        if (ordenarTrabalhoPorFazerPorPrimeiraOpcao) {
            Collections.sort(trabalhoPorFazer);
        }

        System.out.printf("Trabalho por fazer... nº de vertices incompletos %d: \n", trabalhoPorFazer.size());
        for (Integer e : trabalhoPorFazer) {
            if (verbose) {
                System.out.printf("%d (%d), ", e, insumo.degree(e));
            }
        }
        printGraphCount();

        if (vebosePossibilidadesIniciais) {
            System.out.print("Caminhos possiveis: \n");
            List<Integer> ant = caminhosPossiveis.get(trabalhoPorFazer.get(0));
            for (Integer e : trabalhoPorFazer) {
                List<Integer> at = caminhosPossiveis.get(e);
                if (!at.equals(ant)) {
                    System.out.println("----------------------------------------------------------------------------------------------");
                }
                System.out.printf("%d|%d|=%s\n", e, at.size(), at.toString());
                ant = at;
                int dv = k - insumo.degree(e);
                if (dv > at.size()) {
//                throw new IllegalStateException("Grafo inviavel: vetrice " + e + " dv=" + dv + " possi(" + at.size() + ")=" + at);
                }
            }
        }
        System.out.println();
    }

    public void initialLoad() {
        System.out.println("Calculando trabalho a fazer");
        trabalhoPorFazer.clear();
        caminhosPossiveis.clear();
        caminhosPossiveisOriginal.clear();

        for (Integer v : vertices) {
            int remain = k - insumo.degree(v);
            if (remain > 0) {
                trabalhoPorFazer.add(v);
                caminhosPossiveis.put(v, new ArrayList<>());
            }
        }

        if (compressPossiblidades) {
            System.out.print("Compressed...");
        }

        System.out.print("Calculando possibilidades de caminho...");

        for (int i = 0; i < trabalhoPorFazer.size(); i++) {
            Integer v = trabalhoPorFazer.get(i);
            bfsalg.labelDistances(insumo, v);
            caminhosPossiveis.put(v, new ArrayList<>());
            caminhosPossiveisOriginal.put(v, new ArrayList<>());
            int countp = 0;
            int dv = k - insumo.degree(v);
            for (int j = 0; j < trabalhoPorFazer.size(); j++) {
                Integer u = trabalhoPorFazer.get(j);
                if (bfsalg.getDistance(insumo, u) == 4) {
                    countp++;
                    if (j >= i || !compressPossiblidades) {
                        caminhosPossiveis.get(v).add(u);
                    }
                    caminhosPossiveisOriginal.get(v).add(u);
                }
            }
            if (countp < dv) {
                String sterr = "Grafo inviavel: vetrice " + v + " dv=" + dv + " possi(" + countp + ")=" + caminhosPossiveis.get(v);
                if (failInviable) {
                    throw new IllegalStateException(sterr);
                } else {
                    System.err.println(sterr);
                }
            }
        }

        ordenarTrabalhoPorCaminhosPossiveis();
        this.trabalhoPorFazerOrigianl = new LinkedList<>(trabalhoPorFazer);
        System.out.println("Grafo viavel");
    }

    void loadStartFromCache() {
        try {
            trabalhoPorFazer = (LinkedList<Integer>) UtilProccess.loadFromCache("trabalho-por-fazer-partial.dat");
        } catch (Exception e) {
            if (e.getCause() instanceof FileNotFoundException) {
                System.out.println("Cache not found, generating");
                this.prepareStart();
                UtilProccess.saveToCache(trabalhoPorFazer, "trabalho-por-fazer-partial.dat");
            }
        }

        try {
            caminhosPossiveis = (Map<Integer, List<Integer>>) UtilProccess.loadFromCache("caminhos-possiveis.dat");
        } catch (Exception e) {
            if (e.getCause() instanceof FileNotFoundException) {
                System.out.println("Cache not found, generating");
                this.prepareStart();
                UtilProccess.saveToCache(caminhosPossiveis, "caminhos-possiveis.dat");
            }
        }
    }

    void recheckPossibilities() {
        recheckPossibilities(insumo);
    }

    void recheckPossibilities(UndirectedSparseGraphTO insumo) {
        System.out.println("Checking graph");
        boolean inviavel = false;
        Collection<Integer> vertices = insumo.getVertices();
        for (Integer v : vertices) {
            int remain = k - insumo.degree(v);
            if (remain > 0) {
                int countp = 0;
                bfsalg.labelDistances(insumo, v);
                for (Integer u : vertices) {
                    if (bfsalg.getDistance(insumo, u) == 4 && insumo.degree(u) < k) {
                        countp++;
                    }
                }
                if (countp < remain) {
                    String sterr = "Grafo inviavel: vetrice " + v + " dv=" + remain + " possi(" + countp + ")";
                    System.err.println(sterr);
                    inviavel = true;
                }
            }
        }
        if (inviavel) {
            throw new IllegalStateException("Grafo inviavel vetrice ");
        }
        System.out.println("Graph viability... Ok");
    }

    public void sanitizeGraphPossibility() {
        System.out.println("Sanitizando grafo");
        TreeSet<Integer> verticeSanitizar = new TreeSet<>();
        for (Integer v : vertices) {
            int remain = k - insumo.degree(v);
            if (remain > 0) {
                int countp = 0;
                bfsalg.labelDistances(insumo, v);
                for (Integer u : vertices) {
                    if (bfsalg.getDistance(insumo, u) == 4 && insumo.degree(u) < k) {
                        countp++;
                    }
                }
                if (countp < remain) {
//                    throw new IllegalStateException("Grafo inviavel: vetrice " + v + " dv=" + remain + " possi(" + countp + ")");
                    verticeSanitizar.add(v);
                }
            }
        }
        UndirectedSparseGraphTO insumoTmp = sanitizarVertices(verticeSanitizar);
        System.out.println("grafo sanitizado");
        System.out.println("reecheck");
        recheckPossibilities(insumoTmp);
        System.out.println("redesing graph");
        this.insumo = insumoTmp;
        resincTrabalhosPorFazer();
        this.printGraphCount();
    }

    void stripIncompleteVertices() {
        System.out.println("strip grafo");
        TreeSet<Integer> verticeSanitizar = new TreeSet<>();
        for (Integer v : vertices) {
            if (!verticeComplete(v)) {
                verticeSanitizar.add(v);
            }
        }
        UndirectedSparseGraphTO insumoTmp = sanitizarVertices(verticeSanitizar);
        System.out.println("grafo stripe");
        System.out.println("reecheck");
        recheckPossibilities(insumoTmp);
        System.out.println("redesing graph");
        this.insumo = insumoTmp;
        resincTrabalhosPorFazer();
        this.printGraphCount();
    }

    public UndirectedSparseGraphTO sanitizarVertices(TreeSet<Integer> verticeSanitizar) throws IllegalStateException {
        UndirectedSparseGraphTO insumoTmp = insumo.clone();
        for (Integer v : verticeSanitizar) {
            System.out.println("Sanitizando vertice " + v);
            int degree = insumoTmp.degree(v);
            Integer posicaoAtualAbsoluta = getPosicaoAtualAbsoluta(v);
            for (int i = degree; i >= 0; i--) {
                Integer indiceAresta = posicaoAtualAbsoluta - degree + i;
                Pair endpoints = insumoTmp.getEndpoints(indiceAresta);
                if (endpoints != null) {
                    Collection<Integer> percorrido = caminhoPercorrido.get(indiceAresta);
                    if (!endpoints.getFirst().equals(v)) {
                        throw new IllegalStateException("Vertices em sequencias incorrestas: " + v + " " + endpoints + " " + posicaoAtualAbsoluta);
                    }
                    addPendencia(v, endpoints, new ArrayList<>(percorrido));
                    percorrido.clear();
                    insumoTmp.removeEdge(indiceAresta);
                    caminhoPercorrido.remove(indiceAresta);
                    System.out.printf("-{%d}(%d,%d) ", indiceAresta, endpoints.getFirst(), endpoints.getSecond());
                } else {
                    System.out.printf("**{%d}(%d,*) ", indiceAresta, v);
                }
            }
        }
        return insumoTmp;
    }

    void printGraphCount() {
        System.out.println(toStringGraphCount());
    }

    String toStringGraphCount() {
        int edgeCount = insumo.getEdgeCount();
        String grafoCount = "verts : " + (numVertices - trabalhoPorFazer.size())
                + "/" + numVertices + " edges: "
                + edgeCount + "/" + numAretasFinais + " ("
                + Math.round(((double) edgeCount / (double) numAretasFinais) * 100) + "%)";
        return grafoCount;
    }

    String toStringEstimatedTime(int trabalhoCount, long time) {
        int deltaUltimoTrabalho = trabalhoCount - lastresult;
        String tempoEstimado = "infinito";
        if (deltaUltimoTrabalho > 0) {
            long deltaTempoUltimoTrabalho = time - lastime;
            int trabalhoRestante = numAretasFinais - trabalhoCount;
            long temposEstimado = (trabalhoRestante * deltaTempoUltimoTrabalho) / deltaUltimoTrabalho;
            //
            long anos = temposEstimado / OFFSET_ANOS;
            temposEstimado = temposEstimado - OFFSET_ANOS * anos;
            long meses = temposEstimado / OFFSET_MESES;
            temposEstimado = temposEstimado - OFFSET_MESES * meses;
            long dias = temposEstimado / OFFSET_DIAS;
            temposEstimado = temposEstimado - OFFSET_DIAS * dias;
            long horas = temposEstimado / OFFSET_HORAS;
            temposEstimado = temposEstimado - OFFSET_HORAS * horas;
            long minutos = temposEstimado / OFFSET_MINUTOS;
            tempoEstimado = String.format("%da:%dm:%dd:%dh:%dm", anos, meses, dias, horas, minutos);
        }
        return tempoEstimado;
    }

    boolean verticeComplete(Integer i) {
        return insumo.degree(i) == k;
    }

    void resincTrabalhosPorFazer() {
        trabalhoPorFazer.clear();
        for (Integer e : trabalhoPorFazerOrigianl) {
            if (!verticeComplete(e)) {
                trabalhoPorFazer.add(e);
            }
        }
    }

    synchronized Processamento fork() {
        Processamento sub = new Processamento();
//        this.insumo = graph;
        sub.vertices = this.vertices;
        sub.caminhosPossiveis = caminhosPossiveis;
        sub.caminhosPossiveisOriginal = caminhosPossiveisOriginal;
        sub.k = k;
        sub.numVertices = numVertices;
        sub.numAretasFinais = numAretasFinais;
        sub.numArestasIniciais = numArestasIniciais;

        sub.insumo = insumo.clone();
        sub.trabalhoPorFazer = new LinkedList<>(trabalhoPorFazer);
        sub.caminhoPercorrido = UtilProccess.cloneMap(caminhoPercorrido);
//        sub.caminhosPossiveis = UtilTmp.cloneMap(caminhosPossiveis);
        sub.historicoRanking = new TreeMap<>();
        sub.bfsalg = new BFSUtil(numVertices);
        sub.bfsRanking = new BFSUtil(numVertices);
        sub.bfsRankingSegundaOpcao = new BFSUtil(numVertices);

        /* verboses */
        sub.verbose = this.verbose;
        sub.verboseRankingOption = this.verboseRankingOption;
        sub.veboseFimEtapa = this.veboseFimEtapa;
        sub.vebosePossibilidadesIniciais = this.vebosePossibilidadesIniciais;

        /* ranking */
        sub.rankearOpcoes = this.rankearOpcoes;
        sub.rankearOpcoesProfundidade = this.rankearOpcoesProfundidade;
        return sub;
    }

    void dumpResultadoSeInteressante() {
        dumpResultadoSeInteressante(this);
    }

    void dumpResultadoSeInteressante(Processamento processamento) {
        long currentTimeMillis = System.currentTimeMillis();
        if (processamento.dumpResultadoPeriodicamente
                && currentTimeMillis - processamento.lastime > UtilProccess.ALERT_HOUR) {

            System.out.println("Alert hour");
            UtilProccess.dumpStringIdentified(processamento.getEstrategiaString());
            UtilProccess.dumpString(String.format("rbcount[%d,%d,%d,%d]=%d ",
                    processamento.rbcount[0], processamento.rbcount[1],
                    processamento.rbcount[2], processamento.rbcount[3],
                    (processamento.rbcount[0] + processamento.rbcount[1] + processamento.rbcount[2] + processamento.rbcount[3])));

            String printGraphCount = processamento.toStringGraphCount();
            String estimatedTime = processamento.toStringEstimatedTime(processamento.insumo.getEdgeCount(), currentTimeMillis);

            processamento.rbcount[0] = processamento.rbcount[1] = processamento.rbcount[2] = processamento.rbcount[3] = 0;
            processamento.lastime = currentTimeMillis;
            processamento.lastresult = processamento.insumo.getEdgeCount();
            String lastAdd = String.format("last+[%5d](%4d,%4d) \n", insumo.getEdgeCount(), processamento.trabalhoAtual, melhorOpcaoLocal);

            UtilProccess.dumpString(lastAdd + "\n" + printGraphCount + "\nestimado: " + estimatedTime);
            UtilProccess.printCurrentItme();

            if (processamento.longestresult < processamento.insumo.getEdgeCount()
                    || currentTimeMillis - processamento.lastime2 > UtilProccess.ALERT_HOUR_12) {
                processamento.lastime2 = currentTimeMillis;
                if (processamento.longestresult < processamento.insumo.getEdgeCount()) {
                    System.out.print("new longest  result: ");
                    processamento.longestresult = processamento.insumo.getEdgeCount();
                    System.out.println(processamento.longestresult);
                }
                UtilProccess.dumpVertAddArray(processamento.insumo,
                        processamento.numArestasIniciais,
                        processamento.caminhoPercorrido);
                if (processamento.k > 7) {
                    UtilProccess.dumpOverrideString(processamento.insumo.getEdgeString(), ".graph.g9." + processamento.getEstrategiaString());
                }
            }
        }
    }

    void printGraphCaminhoPercorrido() {
        try {
            System.out.print("vert-adds: ");
            for (Integer i : (Collection<Integer>) insumo.getEdges()) {
                if (i > numArestasIniciais) {
                    Collection<Integer> opcoesTestadas = caminhoPercorrido.get(i);
                    String str = String.format("{%d}(%d,%d)",
                            i, insumo.getEndpoints(i).getFirst(),
                            insumo.getEndpoints(i).getSecond());
                    System.out.printf(str);
                    System.out.print("[");

                    for (Integer j : opcoesTestadas) {
                        String jstr = j.toString();
                        System.out.print(jstr);
                        System.out.print(",");
                    }

                    System.out.print("] ");
                }
            }
            System.out.println();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void ordenarTrabalhoPorFazerNatual() {
        Collections.sort(trabalhoPorFazer);
    }

    void ordenarTrabalhoInicialPorCaminhosPossiveis() {
        Collections.sort(trabalhoPorFazer, new ComparatorTrabalhoPorFazer(this.caminhosPossiveis, false));
    }

    void ordenarTrabalhoPorCaminhosPossiveis() {
        Collections.sort(trabalhoPorFazer, new ComparatorTrabalhoPorFazer(this.caminhosPossiveis));
    }

    List<Integer> getOpcoesPossiveisAtuais() {
        return caminhosPossiveis.get(trabalhoAtual);
    }

    private Integer getPosicaoAtualAbsoluta(Integer e1) {
        return getPosicaoAtualRelativa(e1) + numAretasFinais;

    }

    public Integer getPosicaoAtualAbsoluta() {
//        return insumo.getEdgeCount();
        return getPosicaoAtualRelativa() + numAretasFinais;
    }

    public Integer getPosicaoAtualRelativa() {
        return getPosicaoAtualRelativa(trabalhoAtual);
    }

    public Integer getPosicaoAtualRelativa(Integer v) {
        return v * k + insumo.degree(v);
    }

    void mergeProcessamentos(List<Processamento> processamentos) {
        System.out.println("Merge current processamento");
        printGraphCount();
        System.out.println("With anothers " + processamentos.size() + " processamentos");
        int count = 0;
        int added = 0;
        for (Processamento p : processamentos) {
            System.out.println("Merging process " + count++);
            added = 0;
            Set<Map.Entry<Integer, Collection<Integer>>> entrySet = p.caminhoPercorrido.entrySet();
            for (Map.Entry<Integer, Collection<Integer>> e : entrySet) {
                Pair endpoints = p.insumo.getEndpoints(e.getKey());
                Integer first = (Integer) endpoints.getFirst();
                Integer second = (Integer) endpoints.getSecond();
                if (addEdgeIfConsistent(first, second, e.getValue())) {
                    added++;
                }
            }
            System.out.println("Added " + added);
        }
        removerTrabalhoPorFazerVerticesCompletos();
        printGraphCount();
        printGraphCaminhoPercorrido();
    }

    private boolean addEdgeIfConsistent(Integer first, Integer second, Collection<Integer> value) {
        int posicaoAtual = getPosicaoAtualAbsoluta(first);
        boolean ret = addEdgeIfConsistent(first, second);
        if (ret) {
            caminhoPercorrido.put(posicaoAtual, new ArrayList<>(value));
        }
        return ret;
    }

    boolean addEdgeIfConsistent(Integer first, Integer second) {
        boolean ret = false;
        bfsRankingSegundaOpcao.bfs(insumo, first);
        if (bfsRankingSegundaOpcao.getDistance(insumo, second) == 4) {
            addEdge(first, second);
            ret = true;
        }
        return ret;
    }

    private void removerTrabalhoPorFazerVerticesCompletos() {
        Set<Integer> removeList = new HashSet<>();
        for (Integer v : trabalhoPorFazer) {
            if (this.verticeComplete(v)) {
                removeList.add(v);
            }
        }
        trabalhoPorFazer.removeAll(removeList);
    }

    public void marcoInicial() {
        this.marcoInicial = insumo.getEdgeCount();
    }

    public boolean deuPassoFrente() {
        return insumo.getEdgeCount() >= this.marcoInicial || !caminhoPercorrido.isEmpty();
    }

    public Collection<Integer> getCaminhoPercorridoPosicaoAtual() {
        Integer posicaoAtual = getPosicaoAtualAbsoluta();
        Collection<Integer> caminho = caminhoPercorrido.getOrDefault(posicaoAtual, new ArrayList<>());
        caminhoPercorrido.putIfAbsent(posicaoAtual, caminho);
        return caminho;
    }

    Integer addEge() {
//        Integer edge = getPosicaoAtualAbsoluta();
//        if (insumo.addEdge(edge, trabalhoAtual, melhorOpcaoLocal)) {
//            return edge;
//        }
        return addEdge(trabalhoAtual, melhorOpcaoLocal);
    }

    private Integer addEdge(Integer e1, Integer e2) {
        Integer edge = getPosicaoAtualAbsoluta(e1);
        if (insumo.addEdge(edge, e1, e2)) {
            return edge;
        }
        return null;
    }

    int getDvTrabalhoAtual() {
        return (k - insumo.degree(trabalhoAtual));
    }

    Pair<Integer> desfazerUltimoTrabalho() {
        if (falhaInRollBack) {
            if (falhaRollbackCount-- <= 0) {
                throw new IllegalStateException("Interrução forçada");
            }
        }
        Integer arestaAtual = getPosicaoAtualAbsoluta();
        caminhoPercorrido.get(arestaAtual).clear();//limpar caminho
        //limparranking
        historicoRanking.get(arestaAtual).clear();
        Integer arestaAnterior = arestaAtual - 1;
        Pair<Integer> desfazer = null;
        while ((desfazer = insumo.getEndpoints(arestaAnterior)) == null) {
            arestaAnterior = arestaAnterior - 1;
        }
        if (desfazer == null) {
            throw new IllegalStateException("Vertice falhou na primeira posição " + trabalhoAtual + " " + melhorOpcaoLocal + " " + arestaAtual);
        }
        desfazerAresta(desfazer, arestaAnterior);

        //Zerar as opções posteriores
        if (verbose) {
            System.out.printf("-[%5d](%4d,%4d) ", arestaAnterior, desfazer.getFirst(), desfazer.getSecond());
        }
        return desfazer;
    }

    void desfazerAresta(Pair<Integer> desfazer, Integer aresta) throws IllegalStateException {
        //caminhoPercorrido.get(ultimoPasso).add(desfazer.getSecond());
        insumo.removeEdge(aresta);
        if (!trabalhoPorFazer.contains(desfazer.getSecond())) {
            trabalhoPorFazer.add(desfazer.getSecond());
        }
        if (!trabalhoAtual.equals(desfazer.getFirst())
                && !trabalhoPorFazer.contains(desfazer.getFirst())) {
            trabalhoPorFazer.add(desfazer.getFirst());
        }
    }

    void bfsRankingTotal(Integer val) {
        bfsRanking.clearRanking();
        bfsRanking.labelDistances(insumo, trabalhoAtual);
//        bfsRanking.bfsRanking(insumo, trabalhoAtual, val);
        Object edge = insumo.addEdge(trabalhoAtual, val);
        if (edge != null) {
            for (Integer v : trabalhoPorFazer) {
                if (bfsRanking.bfs[v] == 4) {
                    BFSUtil bfstmp = rankingTmp.get(v);
                    if (bfstmp == null) {
                        rankingTmp.put(v, new BFSUtil(numVertices));
                    }
                }
            }
            trabalhoPorFazer.parallelStream().filter(v -> bfsRanking.bfs[v] == 4).forEach(v -> rankingTmp.get(v).bfsRanking(insumo, v));
            for (Integer v : trabalhoPorFazer) {
                if (bfsRanking.bfs[v] == 4) {
                    bfsRanking.incDepthcount(rankingTmp.get(v).depthcount);
                }
            }
            insumo.removeEdge(edge);
        }
    }

    void bfsRanking(Integer val) {
        bfsRanking.bfsRanking(insumo, trabalhoAtual, val);
    }

    boolean atingiuObjetivo() {
        return insumo.getEdgeCount() == numAretasFinais;
    }

    int countEdges() {
        return insumo.getEdgeCount();
    }

    private void addPendencia(Integer v, Pair endpoints, Collection<Integer> percorrido) {
        TreeMap<Integer, Collection<Integer>> pend = this.pendencia.getOrDefault(v, new TreeMap<>());
        this.pendencia.putIfAbsent(v, pend);
        pend.put((Integer) endpoints.getSecond(), percorrido);
    }

    void dumpCaminho() {
        UtilProccess.dumpVertAddArray(insumo,
                numArestasIniciais,
                caminhoPercorrido);
    }

    void mergeContinues(String... strmerge) {
        if (strmerge == null || strmerge.length == 0) {
            return;
        }
        Processamento base = this.fork();
        List<Processamento> processamentos = new ArrayList<>();
        for (String str : strmerge) {
            Processamento fork = base.fork();
            fork.loadCaminho(str);
            processamentos.add(fork);
        }
        this.mergeProcessamentos(processamentos);
    }

    Pair<Integer> getEdgePosicao(int i) {
        Pair<Integer> endpoints = insumo.getEndpoints(i);
        return endpoints;
    }

    Integer getLastAdd() {
        Integer lastAdd = null;
        Pair<Integer> edgePosicao = getEdgePosicao(getPosicaoAtualAbsoluta() - 1);
        if (edgePosicao != null && edgePosicao.getFirst().equals(trabalhoAtual)) {
            lastAdd = edgePosicao.getSecond();
        }
        return lastAdd;
    }

    Integer getRankingHistorico(int posicao, Integer melhorOpcao) {
        Integer ranking = null;
        List<Integer> get = historicoRanking.get(posicao).get(melhorOpcao);
        if (get != null) {
            ranking = get.get(0);
        }
        return ranking;
    }

}
