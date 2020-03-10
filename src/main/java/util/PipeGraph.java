package util;

import com.github.braully.graph.operation.GraphStatistics;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author braully
 */
public class PipeGraph {

    static String WORK_DIRECTORY = "";

    static final IGenStrategy[] operations = new IGenStrategy[]{
        new StrategyEstagnacao(),
        new StrategyAvoidCollid(),
        new StrategyBlock(),
        new StrategyBlockSeq(),
        new StrategyBlockParallel(),
        new StrategyBlockParallelOptim(),
        new StrategyEstagnacaoLenta(),
        new StrategyEstagnacaoCrescente()
    };

    public static void main(String... args) {
        Processamento processamento = new Processamento();

        Option input = new Option("i", "input", true, "input file graph");
        Options options = new Options();
        options.addOption(input);

        Option loadprocess = new Option("c", "continue", true, "continue process from comb state");
        loadprocess.setRequired(false);
        options.addOption(loadprocess);

        Option compresss = new Option("uc", "uncompress-possibility", false, "compress possiblity list");
        compresss.setRequired(false);
        options.addOption(compresss);

        Option mergecontinue = new Option("mc", "merge-continue", true, "continue process from comb state");
        mergecontinue.setRequired(false);
        options.addOption(mergecontinue);

        Option loadstart = new Option("l", "load-start", false, "load start information");
        loadstart.setRequired(false);
        options.addOption(loadstart);

        Option stat = new Option("st", "stat", false, "statitics from graph");
        stat.setRequired(false);
        options.addOption(stat);

        Option orderInit = new Option("oi", "order-init", false, "Ordenação pre processamento");
        //orderInit.setRequired(false);
        options.addOption(orderInit);

        Option verb = new Option("v", "verbose", false, "verbose processing");
        options.addOption(verb);

        Option verbrank = new Option("vr", "verbose-ranking", false, "verbose ranking");
        options.addOption(verbrank);

        Option verbinit = new Option("vi", "verbose-init", false, "verbose initial possibilities");
        options.addOption(verbinit);

        Option poss = new Option("cp", "check-possibility", false, "check possiblities");
        options.addOption(poss);

        Option resume = new Option("rs", "resume", false, "resume last");
        options.addOption(resume);

        Option sanitize = new Option("s", "sanitize", false, "sanitizar grafo");
        options.addOption(sanitize);

        Option strip = new Option("si", "strip-incomplete", false, "Strip incomplete vertices");
        options.addOption(strip);

        Option output = new Option("o", "output", true, "output file");
        output.setRequired(false);
        options.addOption(output);

        Option commitfail = new Option("cf", "commit-fail", true, "Commit count fail");
        commitfail.setRequired(false);
        options.addOption(commitfail);

        Option ranking = new Option("r", "ranking", true, "Ranking deepth");
        ranking.setRequired(false);
        options.addOption(ranking);

        Option rollbackfail = new Option("rf", "rollback-fail", true, "Rollback count fail");
        rollbackfail.setRequired(false);
        options.addOption(rollbackfail);

        Option notfailinv = new Option("nfi", "not-fail-inviable", false, "Not fail on inviable graph");
        notfailinv.setRequired(false);
        options.addOption(notfailinv);

        Option parallel = new Option("np", "nparallel", true, "Parallel process");
        parallel.setRequired(false);
        options.addOption(parallel);

        Option vparallel = new Option("vp", "vparallel", true, "Parallel process");
        vparallel.setRequired(false);
        options.addOption(vparallel);

        Option iparallel = new Option("iparallel", "iparallel", true, "Parallel process");
        iparallel.setRequired(false);
        options.addOption(iparallel);

        Option merge = new Option("mc", "merge-continue", true, "merge multiple continue process from comb state");
        loadprocess.setRequired(false);
        options.addOption(merge);

        OptionGroup exec = new OptionGroup();
        exec.setRequired(false);
        IGenStrategy[] opers = operations;
        Option[] execs = new Option[opers.length];
        for (int i = 0; i < opers.length; i++) {
            IGenStrategy oper = opers[i];
            execs[i] = new Option("" + i, false, oper.getName());
            options.addOption(execs[i]);
        }

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("PipeGraph", options);
            return;
        }

        if (cmd.hasOption("verbose")) {
            processamento.verbose = true;
        }
        if (cmd.hasOption("verbose-ranking")) {
            processamento.verboseRankingOption = true;
        }
        if (cmd.hasOption("verbose-init")) {
            processamento.vebosePossibilidadesIniciais = true;
        }
        if (cmd.hasOption("verbose-fim")) {
            processamento.veboseFimEtapa = true;
        }
        if (cmd.hasOption("not-fail-inviable")) {
            processamento.failInviable = false;
        }
        if (cmd.hasOption("uncompress-possibility")) {
            processamento.compressPossiblidades = false;
        }
        if (cmd.hasOption("verbrank")) {
            processamento.verboseRankingOption = true;
        }

        String strfailcom = cmd.getOptionValue("commit-fail");
        if (strfailcom != null && !strfailcom.isEmpty()) {
            processamento.falhaInCommitCount = true;
            processamento.falhaCommitCount = Integer.parseInt(strfailcom.trim());
        }

        String strrollbackfail = cmd.getOptionValue("rollback-fail");
        if (strrollbackfail != null && !strrollbackfail.isEmpty()) {
            processamento.falhaInRollBack = true;
            processamento.falhaRollbackCount = Integer.parseInt(strrollbackfail.trim());
        }

        String inputFilePath = cmd.getOptionValue("input");
        if (inputFilePath == null) {
            System.out.println("No input graph, loading default: ");
            inputFilePath = com.github.braully.graph.DatabaseFacade.DATABASE_DIRECTORY + "/ultimo-grafo-de-moore.es";
            try {
                File file = new File(inputFilePath);
                if (!file.exists()) {
                    System.out.println("File not exists... copying previous version");
                    InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("esqueleto-ultimo-grafo-moore.es");
                    OutputStream outputStream = new FileOutputStream(file);
                    IOUtils.copy(in, outputStream);
                    System.out.println("Success");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (inputFilePath == null || inputFilePath.isEmpty()) {
            formatter.printHelp("PipeGraph", options);
            System.out.println("input is requeried");
            return;
        }

        String stranking = cmd.getOptionValue("ranking");
        if (stranking != null && !stranking.isEmpty()) {
            processamento.rankearOpcoes = true;
            processamento.rankearOpcoesProfundidade = Integer.parseInt(stranking.replaceAll("\\D", "").trim());
        }

        processamento.loadGraph(inputFilePath);
        if (cmd.hasOption("load-start")) {
            System.out.print("Loading from cache");
            processamento.loadStartFromCache();
            System.out.print("...Ok");
        }

        if (cmd.hasOption("stat") && !cmd.hasOption("continue")) {
            GraphStatistics gs = new GraphStatistics();
            Map result = gs.doOperation(processamento.insumo);
            System.out.println("Statitics");
            System.out.println(result);
        }

        processamento.prepareStart();

        if (cmd.hasOption("continue")) {
            System.out.println("Continue combination");
            String loadProcess = cmd.getOptionValue("continue");
            if (loadProcess != null && loadProcess.trim().isEmpty()) {
                processamento.loadCaminho(loadProcess);
                System.out.println("...Ok");
            } else {
                processamento.loadLastCaminho();
            }
        }
        String mergec = cmd.getOptionValue("merge-continue");
        if (mergec != null) {
            String[] strmerge = mergec.split(",");
            if (strmerge == null || strmerge.length == 0) {
                System.out.println("Invalid merge-continue");
            } else {
                for (int i = 0; i < strmerge.length; i++) {
                    strmerge[i] = strmerge[i].trim();
                }
                processamento.mergeContinues(strmerge);
            }
        }

        if (cmd.hasOption("check-possibility")) {
            processamento.recheckPossibilities();
            System.out.println("Graph...Ok");
        }

        if (cmd.hasOption("sanitize")) {
            System.out.println("Sanitize Graph ");
            processamento.sanitizeGraphPossibility();
            System.out.println("Recheck possibility ");
            processamento.recheckPossibilities();
            System.out.println("Graph...Ok");
        }

        if (cmd.hasOption("strip-incomplete")) {
            System.out.println("Strip incomplete vertices");
            processamento.stripIncompleteVertices();
            System.out.println("Graph...Ok");
        }

        if (cmd.hasOption("stat") && (cmd.hasOption("continue") || cmd.hasOption("merge-continue"))) {
            GraphStatistics gs = new GraphStatistics();
            Map result = gs.doOperation(processamento.insumo);
            System.out.println("Statitics");
            System.out.println(result);
        }

        List<IGenStrategy> operationsToExecute = new ArrayList<IGenStrategy>();
        for (int i = 0; i < opers.length; i++) {
            IGenStrategy oper = opers[i];
            String value = execs[i].getOpt();
            if (cmd.hasOption(value)) {
                operationsToExecute.add(oper);
            }
        }

        if (cmd.hasOption("nparallel")
                || cmd.hasOption("vparallel")
                || cmd.hasOption("iparallel")
                || cmd.hasOption("order-init")) {
            processamento.ordenarTrabalhoPorCaminhosPossiveis();
            System.out.println("Reordenando trabalho por fazer: ");
            System.out.println(processamento.trabalhoPorFazer);
        }

        List<TrabalhoProcessamento> processos = new ArrayList<>();
        String strparallel = cmd.getOptionValue("nparallel");
        if (strparallel != null && !strparallel.isEmpty()) {
            Integer numThreads = Integer.parseInt(strparallel.trim().replaceAll("\\D", ""));
            int size = processamento.trabalhoPorFazer.size();
            numThreads = Integer.min(size, numThreads);
            for (Integer i = 0; i < numThreads; i++) {
                processos.add(new TrabalhoProcessamento(i));
            }
        }

        strparallel = cmd.getOptionValue("iparallel");
        if (strparallel != null && !strparallel.isEmpty()) {
            String[] strs = strparallel.trim().split(",");
            for (String str : strs) {
                Integer idx = Integer.parseInt(str.trim().replaceAll("\\D", ""));
                processos.add(new TrabalhoProcessamento(idx));
            }
        }

        strparallel = cmd.getOptionValue("vparallel");
        if (strparallel != null && !strparallel.isEmpty()) {
            String[] strs = strparallel.trim().split(",");
            for (String str : strs) {
                Integer vert = Integer.parseInt(str.trim().replaceAll("\\D", ""));
                Integer idx = processamento.trabalhoPorFazer.indexOf(vert);
                if (idx != null && idx >= 0) {
                    processos.add(new TrabalhoProcessamento(idx));
                } else {
                    System.out.println("Index not found for vet: " + str);
                }
            }
        }

        if (!processos.isEmpty()) {
            UtilProccess.printCurrentItme();
            processos.parallelStream().forEach(p -> p.generateGraph(processamento.fork()));
            List<Processamento> processamentos = new ArrayList<>();
            for (TrabalhoProcessamento processo : processos) {
                processamentos.add(processo.last);
            }
            UtilProccess.printCurrentItme();
            processamento.mergeProcessamentos(processamentos);
        }

        String mc = cmd.getOptionValue("merge-continue");
        if (mc != null) {
            Processamento subprocessamento = processamento.fork();
        }

//        if (operationsToExecute.isEmpty()) {
//            operationsToExecute.add(opers[0]);
//        }
        for (IGenStrategy operation : operationsToExecute) {
            operation.generateGraph(processamento);
        }
    }
}
