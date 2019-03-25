package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StrategyBlockParallel
        extends StrategyBlock
        implements IGenStrategy {

    public String getName() {
        return "Estagnar em Bloco Paralelamente";
    }

    @Override
    public void processarBlocos(TreeMap<Integer, LinkedList<Integer>> blocos,
            Processamento processamento) throws IllegalStateException {
        Map<Integer, List<Integer>> blocksBySize = new HashMap<>();
        Integer great = 0;
        for (Map.Entry<Integer, LinkedList<Integer>> es : blocos.entrySet()) {
            Integer size = processamento.caminhosPossiveis.get(es.getValue().get(0)).size();
            List<Integer> list = blocksBySize.getOrDefault(size, new ArrayList<Integer>());
            list.add(es.getKey());
            if (size > great) {
                great = size;
            }
            blocksBySize.putIfAbsent(size, list);
        }

        System.out.println(" " + blocos.size() + " Blocos ");
        System.out.println(blocksBySize.get(great).size()
                + " blocos de tamanho " + great
                + " serão processados");

        UtilProccess.printCurrentItme();
        /* */
        Integer numThreads = blocksBySize.get(great).size();
        List<Integer> blocosPraProcessar = blocksBySize.get(great);
        Processamento base = processamento.fork();

        List<TrabalhoProcessamento> processos = new ArrayList<>();
        int maxthreads = (int) (Runtime.getRuntime().availableProcessors() * 1.5);

        for (Integer i = 0; i < numThreads; i++) {
            LinkedList<Integer> bloco = blocos.get(blocosPraProcessar.get(i));
            processos.add(new TrabalhoProcessamento(bloco));
            if (processos.size() >= maxthreads) {
                processos.parallelStream().forEach(p -> p.processarBlocoTotal(base.fork()));
//                processos.parallelStream().forEach(p -> p.processarProximo());
                List<Processamento> processamentos = new ArrayList<>();
                for (TrabalhoProcessamento processo : processos) {
                    Processamento last = processo.last;
                    last.dumpCaminho();
                    processamentos.add(last);
                }
                System.out.println("Barreira atingida... merge");
                processamento.mergeProcessamentos(processamentos);
                processamento.dumpResultadoSeInteressante();
                UtilProccess.printCurrentItme();
                processos.clear();
            }
        }

//        for (Integer i = 0; i < numThreads; i++) {
//            LinkedList<Integer> bloco = blocos.get(blocosPraProcessar.get(i));
//            processos.add(new TrabalhoProcessamento(bloco));
//        }
//        for (TrabalhoProcessamento processo : processos) {
//            processamentos.add(processo.last);
//        }
//        processos.parallelStream().forEach(p -> p.processarProximo());
//        System.out.println("Merge");
    }
}
