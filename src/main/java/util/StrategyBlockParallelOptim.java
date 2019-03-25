package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class StrategyBlockParallelOptim
        extends StrategyBlock
        implements IGenStrategy {

    public String getName() {
        return "Estagnar em Bloco Paralelamente Optimin";
    }

    @Override
    public void processarBlocos(TreeMap<Integer, LinkedList<Integer>> blocos,
            Processamento processamento) throws IllegalStateException {
        Map<Integer, List<Integer>> blocksBySize = new HashMap<>();
        Set<Integer> setmax = new HashSet<>();
        Integer great = 0;
        for (Map.Entry<Integer, LinkedList<Integer>> es : blocos.entrySet()) {
            Integer size = processamento.caminhosPossiveis.get(es.getValue().get(0)).size();
            List<Integer> list = blocksBySize.getOrDefault(size, new ArrayList<Integer>());
            list.add(es.getKey());
            if (size > great) {
                great = size;
                setmax.clear();
            }
            setmax.addAll(es.getValue());
            blocksBySize.putIfAbsent(size, list);
        }

        System.out.println(" " + blocos.size() + " Blocos ");
        System.out.println(blocksBySize.get(great).size() + " blocos de tamanho " + great + " serão processados");

        UtilProccess.printCurrentItme();
        /* */
        Integer numThreads = blocksBySize.get(great).size();
        List<Integer> blocosPraProcessar = blocksBySize.get(great);
//        Processamento base = processamento.fork();

        Set<Integer> blockset = new HashSet<>();
        Set<Integer> tmpblockset = new HashSet<>();
        Map<Integer, List<Integer>> concorrencia = new HashMap<>();
        for (Integer i = 0; i < numThreads; i++) {
            Integer idxBloco = blocosPraProcessar.get(i);
            LinkedList<Integer> bloco = blocos.get(idxBloco);

            blockset.clear();
            concorrencia.put(idxBloco, new ArrayList<>());
            for (Integer v : bloco) {
                blockset.addAll(processamento.caminhosPossiveis.get(v));
            }
//            System.out.print("Similaridade " + bloco + ": ");
            for (Integer j = 0; j < numThreads; j++) {
                Integer idxBlocoj = blocosPraProcessar.get(j);
                LinkedList<Integer> blocoj = blocos.get(idxBlocoj);
                tmpblockset.clear();
                for (Integer v : blocoj) {
                    tmpblockset.addAll(processamento.caminhosPossiveis.get(v));
                }
                if (!tmpblockset.isEmpty()) {
                    tmpblockset.retainAll(blockset);
//                    System.out.print(" ");
//                    System.out.print(tmpblockset.size());
                    if (tmpblockset.size() == 0) {
                        concorrencia.get(idxBloco).add(idxBlocoj);
                    }
                }
            }
//            System.out.println();
        }
        int maxthreads = (int) (Runtime.getRuntime().availableProcessors() * 1.5);

        List<TrabalhoProcessamento> processos = new ArrayList<>();
        for (Integer i = 0; i < numThreads; i++) {
            Integer idxBloco = blocosPraProcessar.get(i);
            LinkedList<Integer> bloco = blocos.get(idxBloco);
            List<Integer> paralelismo = concorrencia.get(idxBloco);
            System.out.print("Processando blocos: ");
            System.out.println(bloco);

            processos.add(new TrabalhoProcessamento(bloco));
            for (Integer ip : paralelismo) {
                LinkedList<Integer> veticesbloco = blocos.get(ip);
                if (setmax.contains(veticesbloco.get(0))) {
                    System.out.print("Paralelizando: ");
                    System.out.println(veticesbloco);
                    processos.add(new TrabalhoProcessamento(veticesbloco));
                }
            }
            processos.parallelStream().forEach(p -> p.processarBlocoTotal(processamento.fork()));
//                processos.parallelStream().forEach(p -> p.processarProximo());
            List<Processamento> processamentos = new ArrayList<>();
            for (TrabalhoProcessamento processo : processos) {
                Processamento last = processo.last;
//                last.dumpCaminho();
                processamentos.add(last);
            }
            System.out.println("Barreira atingida... merge");
            processamento.mergeProcessamentos(processamentos);
            processamento.dumpResultadoSeInteressante();
            UtilProccess.printCurrentItme();
            processos.clear();
            processamento.sanitizeGraphPossibility();
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
