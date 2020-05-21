package util;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StrategyBlock
        extends StrategyEstagnacaoCrescente
        implements IGenStrategy {

    public String getName() {
        return "Estangar em Bloco";
    }

    public void generateGraph(Processamento processamento) {
        verboseInicioGeracao(processamento);
        ordenacaoFimEtapa(processamento);
        UtilProccess.printCurrentItme();

        TreeMap<Integer, LinkedList<Integer>> blocos = new TreeMap<>();
        Integer count = 0;
        List<Integer> ant = processamento.caminhosPossiveis.get(processamento.trabalhoPorFazer.get(0));
        blocos.put(count, new LinkedList<>());

        for (Integer e : processamento.trabalhoPorFazer) {
            List<Integer> at = processamento.caminhosPossiveis.get(e);
            if (at != null && !at.isEmpty()) {
                if (!at.equals(ant)) {
                    count++;
                    blocos.put(count, new LinkedList<>());
                    if (processamento.vebosePossibilidadesIniciais) {
                        System.out.println("----------------------------------------------------------------------------------------------");
                    }
                }
                blocos.get(count).add(e);
                if (processamento.vebosePossibilidadesIniciais) {
                    System.out.printf("%d|%d|=%s\n", e, at.size(), at.toString());
                }
                ant = at;
            }
        }
//        blocos.pollLastEntry();
        processarBlocos(blocos, processamento);
        verboseResultadoFinal(processamento);
    }

    public void processarBlocos(TreeMap<Integer, LinkedList<Integer>> blocos, Processamento processamento) throws IllegalStateException {
        TreeMap<Integer, LinkedList<Integer>> blocosConcluidos = new TreeMap<>();
        while (!blocos.isEmpty()) {
            Map.Entry<Integer, LinkedList<Integer>> firstEntry = blocos.firstEntry();
            LinkedList<Integer> bloco = firstEntry.getValue();
            System.out.printf("Processando bloco %d vertices %s\n",
                    firstEntry.getKey(), firstEntry.getValue().toString());
            processamento.marcoInicial();
            estagnarBloco(processamento, bloco);

            if (!processamento.deuPassoFrente()) {
                processamento.printGraphCaminhoPercorrido();
                throw new IllegalStateException("Grafo inviavel no bloco: " + bloco);
            }

            blocosConcluidos.put(firstEntry.getKey(), firstEntry.getValue());
            blocos.remove(firstEntry.getKey());
            System.out.printf("Concluido bloco %d vertices %s\n", firstEntry.getKey(), firstEntry.getValue().toString());
            verboseFimEtapa(processamento);
        }
    }

    public void estagnarBloco(Processamento processamento, LinkedList<Integer> bloco) {
        while (temTrabalhoNoBloco(processamento, bloco)) {
            processamento.trabalhoAtual = UtilProccess.getOverflow(bloco, processamento.countEdges());
            verboseInicioEtapa(processamento);
            if (trabalhoNaoAcabou(processamento)
                    && processamento.deuPassoFrente()) {
                processamento.melhorOpcaoLocal = avaliarMelhorOpcao(processamento);
                adicionarMellhorOpcao(processamento);
            }
        }
    }

    boolean temTrabalhoNoBloco(Processamento processamento, LinkedList<Integer> bloco) {
        boolean ret = true;
        for (Integer i : bloco) {
            ret = ret && processamento.verticeComplete(i);
            if (!ret) {
                break;
            }
        }
        return !ret;
    }
}
