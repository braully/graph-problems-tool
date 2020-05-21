package util;

import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author strike
 */
public class StrategyEstagnacao implements IGenStrategy {

    Comparator<Integer> comparatorTrabalhoPorFazer;
    ComparatorMap comparatorProfundidade;

    public ComparatorMap getComparatorProfundidade(Processamento processamento) {
        if (comparatorProfundidade == null) {
            comparatorProfundidade = new ComparatorMap(processamento.rankearOpcoesProfundidade);
        }
        return comparatorProfundidade;
    }

    public Comparator<Integer> getComparatorTrabalhoPorFazer(Processamento processamento) {
        if (comparatorTrabalhoPorFazer == null) {
            comparatorTrabalhoPorFazer = new ComparatorTrabalhoPorFazer(processamento.caminhosPossiveis);
        }
        return comparatorTrabalhoPorFazer;
    }

    public String getName() {
        return "Estagnação de Vertice";
    }

    public void generateGraph(Processamento processamento) {
        ordenacaoFimEtapa(processamento);
        verboseInicioGeracao(processamento);
        UtilProccess.printCurrentItme();
        processamento.marcoInicial();
        while (!processamento.trabalhoPorFazer.isEmpty() && processamento.deuPassoFrente()) {
            processamento.trabalhoAtual = processamento.trabalhoPorFazer.get(0);
            estagnarVertice(processamento);
            verboseFimEtapa(processamento);
            ordenacaoFimEtapa(processamento);
        }
//        printMapOpcoes(trabalhPorFazerOriginal, insumo, caminhosPossiveis);
        processamento.printGraphCount();
        verboseResultadoFinal(processamento);
    }

    public void estagnarVertice(Processamento processamento) throws IllegalStateException {
        verboseInicioEtapa(processamento);
        System.out.println("Estagnando vertice: " + processamento.trabalhoAtual);
//            printMapOpcoes(trabalhPorFazerOriginal, insumo, caminhosPossiveis);
        while (trabalhoNaoAcabou(processamento) && processamento.deuPassoFrente()) {
            processamento.getCaminhoPercorridoPosicaoAtual();
            processamento.melhorOpcaoLocal = avaliarMelhorOpcao(processamento);
            adicionarMellhorOpcao(processamento);
        }
        if (trabalhoAcabou(processamento, processamento.trabalhoAtual)) {
            processamento.trabalhoPorFazer.remove(processamento.trabalhoAtual);
        }
    }

    public void ordenacaoFimEtapa(Processamento processamento) {
        if (processamento.ordenarTrabalhoPorFazerPorPrimeiraOpcao) {
            Collections.sort(processamento.trabalhoPorFazer, getComparatorTrabalhoPorFazer(processamento));
        } else {
            Collections.sort(processamento.trabalhoPorFazer);
        }
    }

    void verboseInicioGeracao(Processamento processamento) {
        System.out.println(this.getClass().getSimpleName());
        if (processamento.vebosePossibilidadesIniciais) {
            System.out.print("Caminhos possiveis: \n");
            List<Integer> ant = processamento.caminhosPossiveis.get(processamento.trabalhoPorFazer.get(0));
            for (Integer e : processamento.trabalhoPorFazer) {
                List<Integer> at = processamento.caminhosPossiveis.get(e);
                if (!at.equals(ant)) {
                    System.out.println("----------------------------------------------------------------------------------------------");
                }
                System.out.printf("%d|%d|=%s\n", e, at.size(), at.toString());
                ant = at;
            }
        }
        System.out.println();
    }

    void adicionarMellhorOpcao(Processamento processamento) {
        //boolean fakeProblem = trabalhoAtual.equals(13) && insumo.degree(13) == K - 1;
        //if (opcaoViavel(insumo, melhorOpcaoLocal) && !fakeProblem) {
        Integer posicaoAtual = processamento.getPosicaoAtualAbsoluta();
        Collection<Integer> subcaminho = processamento.caminhoPercorrido.getOrDefault(posicaoAtual, new ArrayList<>());
        processamento.caminhoPercorrido.putIfAbsent(posicaoAtual, subcaminho);
        if (processamento.melhorOpcaoLocal != null) {
            subcaminho.add(processamento.melhorOpcaoLocal);
        }
        if (opcaoValida(processamento)) {
            if (processamento.verticeComplete(processamento.melhorOpcaoLocal)) {
                throw new IllegalStateException("vertice statured " + posicaoAtual + " " + processamento.trabalhoAtual + " " + processamento.melhorOpcaoLocal);
            }
            Integer aresta = processamento.addEge();
            if (!aresta.equals(posicaoAtual)) {
                throw new IllegalStateException("Edge not added: " + posicaoAtual + " " + processamento.trabalhoAtual + " " + processamento.melhorOpcaoLocal);
            }
            if (trabalhoAcabou(processamento, processamento.melhorOpcaoLocal)) {
                processamento.trabalhoPorFazer.remove(processamento.melhorOpcaoLocal);
            }
            observadorDeEtapa(aresta, processamento.melhorOpcaoLocal, processamento);
        } else {
            desfazerUltimoTrabalho(processamento);
        }
    }

    /* */
    void verboseInicioEtapa(Processamento processamento) {
//        System.out.print("Start trabalho: ");
//        System.out.println(processamento.trabalhoAtual);
//        if (falhaInCommitCount) {
//            bfsalg.labelDistances(insumo, trabalhoAtual);
//            System.out.printf("\nbfs-map-ini[%4d]: [", trabalhoAtual);
//            for (Integer o : opcoesPossiveis) {
//                if (bfsalg.getDistance(insumo, o) == 1) {
//                    System.out.print('x');
//                } else if (bfsalg.getDistance(insumo, o) == 4) {
//                    System.out.print('4');
//                } else {
//                    System.out.print(' ');
//                }
//            }
//            System.out.print("]\n");
//        }
    }

    boolean opcaoValida(Processamento processamento) {
        Integer melhorOpcao = processamento.melhorOpcaoLocal;

        if (!processamento.bfsalg.getDistance(processamento.insumo, processamento.trabalhoAtual).equals(0)) {
            throw new IllegalStateException("Etado do bfs incorreto para" + processamento.trabalhoAtual + " " + processamento.getPosicaoAtualAbsoluta());
        }

        if (melhorOpcao == null) {
            processamento.rbcount[0]++;
            if (processamento.verbose) {
                System.out.println("melhor opçao é nula");
            }
            return false;
        }

        int posicao = processamento.getPosicaoAtualAbsoluta();
        int distanciaMelhorOpcao = processamento.bfsalg.getDistance(processamento.insumo, melhorOpcao);
        if (distanciaMelhorOpcao < 4) {
            processamento.rbcount[1]++;
            if (processamento.verbose) {
                System.out.printf("g[%d](%d,%d) ", posicao, processamento.trabalhoAtual, melhorOpcao);
            }
            return false;
        }

//        if (trabalhoAtual.equals(113)) {
//            return false;
//        }
        if (processamento.anteciparVazio && processamento.bfsalg.getDistance(processamento.insumo, processamento.trabalhoAtual) == 0) {
            boolean condicao1 = true;
            int dv = processamento.getDvTrabalhoAtual();
            condicao1 = dv <= processamento.bfsalg.depthcount[4];
            if (!condicao1 && processamento.verbose) {
                System.out.printf("*[%d](%d,%d -> rdv=%d 4c=%d) ", posicao, processamento.trabalhoAtual, melhorOpcao, dv, processamento.bfsalg.depthcount[4]);
            }
            if (!condicao1) {
                processamento.rbcount[2]++;
                return false;
            }
        }
        if (processamento.descartarOpcoesNaoOptimais && !processamento.caminhoPercorrido.get(posicao).isEmpty()) {
            Integer escolhaAnterior = ((List<Integer>) processamento.caminhoPercorrido.get(posicao)).get(0);
            List<Integer> rankingAnterior = processamento.historicoRanking.get(posicao).get(escolhaAnterior);
            if (rankingAnterior != null) {
                Integer rankingEscolhaAnterior = rankingAnterior.get(0);
                Integer rankingOpcaoAtual = processamento.getRankingHistorico(posicao, melhorOpcao);
                if (rankingEscolhaAnterior != null && rankingOpcaoAtual != null && rankingOpcaoAtual < rankingEscolhaAnterior) {
                    processamento.rbcount[3]++;
                    if (processamento.verbose) {
                        System.out.printf("o[%d](%d,%d) ", posicao, processamento.trabalhoAtual, melhorOpcao);
                    }
                    return false;
                }
            }
        }
        return true;
    }

    boolean trabalhoAcabou(Processamento processamento, Integer vertice) {
        return processamento.verticeComplete(vertice);
    }

    boolean trabalhoNaoAcabou(Processamento processamento) {
        return !trabalhoAcabou(processamento, processamento.trabalhoAtual);
    }

    boolean temFuturo(Integer trabalhoAtual) {
        return true;
    }

    void observadorDeEtapa(Integer aresta,
            Integer melhorOpcaoLocal, Processamento processamento) {
        if (processamento.verbose) {
            System.out.printf("+[%5d](%4d,%4d) ", aresta, processamento.trabalhoAtual, melhorOpcaoLocal);
        }
        processamento.dumpResultadoSeInteressante();
    }

    void verboseResultadoFinal(Processamento processamento) {
        System.out.println();
        if (!processamento.atingiuObjetivo()) {
            System.out.println("Busca pelo grafo Falhou ***");
        } else {
            System.out.println("Grafo Encontrado");
        }

        try {
            System.out.print("Added-Edges: ");
            for (Integer e : processamento.caminhoPercorrido.navigableKeySet()) {
                Pair endpoints = processamento.insumo.getEndpoints(e);
                if (endpoints != null) {
                    System.out.print(endpoints);
                    System.out.print(", ");
                }
            }
        } catch (Exception e) {
        } finally {
            System.out.println();
        }
        System.out.println("Final Graph: ");
        String edgeString = processamento.insumo.getEdgeString();
        System.out.println(edgeString);
    }

    void verboseFimEtapa(Processamento processamento) throws IllegalStateException {
        if (trabalhoAcabou(processamento, processamento.trabalhoAtual)) {
            System.out.printf(".. %d [%d] \n", processamento.trabalhoAtual, processamento.countEdges());
        } else {
            System.out.printf("!! %d \n", processamento.trabalhoAtual);
        }
        System.out.printf("rbcount[%d,%d,%d,%d]=%d ", processamento.rbcount[0], processamento.rbcount[1],
                processamento.rbcount[2], processamento.rbcount[3],
                (processamento.rbcount[0] + processamento.rbcount[1]
                + processamento.rbcount[2] + processamento.rbcount[3]));
        System.out.println(processamento.getEstrategiaString());
        UtilProccess.printCurrentItme();

        if (processamento.veboseFimEtapa) {
            verboseFimEtapa(processamento.caminhoPercorrido, processamento.insumo);
        }
        if (processamento.falhaInCommitCount) {
//            verboseFimEtapa(caminhoPercorrido);
//            bfsalg.labelDistances(insumo, trabalhoAtual);
//            System.out.printf("\nbfs-map-opf[%4d]: [", trabalhoAtual);
//            TreeSet<Integer> tmp = new TreeSet<>(opcoesPossiveis);
//            for (Integer o : tmp) {
//                int distance = bfsalg.getDistance(insumo, o);
//                if (distance == 1) {
//                    System.out.print('x');
//                } else if (distance == 4) {
//                    System.out.print('4');
//                } else {
//                    System.out.print(' ');
//                }
//            }
//            System.out.print("]\n");
            if (processamento.falhaCommitCount-- <= 0) {
                throw new IllegalStateException("Interrução forçada -- commit");
            }
        }
    }

    void verboseFimEtapa(TreeMap<Integer, Collection<Integer>> caminhoPercorrido, UndirectedSparseGraphTO insumo) {
        System.out.println("------------------------------------------------------------------------------------------------");
        System.out.print("Caminhos percorrido: ");
        caminhoPercorrido.entrySet().forEach(e -> System.out.printf("%d(%s)=%s\n", e.getKey(), insumo.getEndpoints(e.getKey()), e.getValue().toString()));
        System.out.println();
    }

    Integer avaliarMelhorOpcao(Processamento processsamento) {
        processsamento.bfsalg.labelDistances(processsamento.insumo, processsamento.trabalhoAtual);
//        sort(opcoesPossiveis, bfsalg.getDistanceDecorator());
        processsamento.getCaminhoPercorridoPosicaoAtual();
        sortAndRanking(processsamento);
        Collection<Integer> jaSelecionados = processsamento.getCaminhoPercorridoPosicaoAtual();
        Integer melhorOpcao = getOpcao(processsamento.getOpcoesPossiveisAtuais(), jaSelecionados);
        return melhorOpcao;
    }

    void sortAndRanking(Processamento processamento) {
        if (processamento.rankearOpcoes) {
            rankearOpcoes(processamento);
        }
    }

    public void rankearOpcoes(Processamento processamento) throws RuntimeException {
        Integer[] bfs = processamento.bfsalg.bfs;
        int posicaoAtual = processamento.getPosicaoAtualAbsoluta();
        Collection<Integer> opcoesPassadas = processamento.getCaminhoPercorridoPosicaoAtual();
        Map<Integer, List<Integer>> rankingAtual = processamento.historicoRanking.getOrDefault(posicaoAtual, new HashMap<>());
        processamento.historicoRanking.putIfAbsent(posicaoAtual, rankingAtual);
        if (opcoesPassadas.isEmpty() || rankingAtual.isEmpty()) {
            processamento.getOpcoesPossiveisAtuais().sort(getComparatorProfundidade(processamento).setBfs(bfs));
            rankingAtual.clear();
            int i = 0;
//                for (i = 0; i < processamento.ranking.length; i++) {
//                    processamento.ranking[i] = 0;
//                }
            for (i = 0; i < processamento.getOpcoesPossiveisAtuais().size(); i++) {
                Integer val = processamento.getOpcoesPossiveisAtuais().get(i);
                if (bfs[val] == 4) {
                    List<Integer> listRankingVal = rankingAtual.get(val);
                    if (listRankingVal == null) {
                        listRankingVal = new ArrayList<>(4);
                        rankingAtual.put(val, listRankingVal);
                    }
                    rankearOpcao(processamento, posicaoAtual, val);
                } else {
                    break;
                }
//                if (trabalhoAtual.equals(18) && (val.equals(22) || val.equals(23))) {
//                    if (trabalhoAtual.equals(18)) {
//                    if (verboseRankingOption || posicaoAtual == 9505) {
                if (processamento.verboseRankingOption) {
                    System.out.printf("Ranking (%4d,%4d): ", val, processamento.trabalhoAtual);
                    UtilProccess.printArray(processamento.bfsRanking.depthcount);
//                        if (rankearSegundaOpcoes) {
//                            int f = processamento.bfsRankingSegundaOpcao.depthcount[3];
//                            UtilTmp.printArray(processamento.bfsRankingSegundaOpcao.depthcount);
//                            processamento.bfsRankingSegundaOpcao.bfsRanking(processamento.insumo, val);
//                            UtilTmp.printArray(processamento.bfsRankingSegundaOpcao.depthcount);
//                            listRankingVal.add(processamento.bfsRankingSegundaOpcao.depthcount[3]-f);
//                        }
                }
//                    }
            }
//                opcoesPossiveis.subList(0, i).sort(comparatorProfundidade.setBfs(ranking));
            processamento.getOpcoesPossiveisAtuais().subList(0, i).sort(getComparatorProfundidade(processamento).setMapList(rankingAtual));
        } else {
            List<Integer> subList = null;
            try {
//                opcoesPossiveis.subList(0, rankingAtual.size()).sort(comparatorProfundidade.setMap(rankingAtual));
//                processamento.getOpcoesPossiveisAtuais().sort(getComparatorProfundidade(processamento).setBfs(bfs));
                processamento.getOpcoesPossiveisAtuais().sort(getComparatorProfundidade(processamento).setBfs(bfs));
                subList = processamento.getOpcoesPossiveisAtuais().subList(0, rankingAtual.size());
                subList.sort(getComparatorProfundidade(processamento).setMapList(rankingAtual));
//Reaproveintando ranking anteriormente calculado
            } catch (RuntimeException e) {
                throw e;
            }
        }
    }

    public void rankearOpcao(Processamento processamento, Integer posicaoAtual, Integer val) {
        processamento.bfsRanking(val);
        List<Integer> listRankingVal = processamento.historicoRanking.get(posicaoAtual).get(val);
//       
        listRankingVal.clear();
        listRankingVal.add(processamento.bfsRanking.depthcount[4]);
        listRankingVal.add(-processamento.bfsRanking.depthcount[3]);
        listRankingVal.add(processamento.bfsRanking.depthcount[2]);
        if (processamento.rankearSegundaOpcoes) {
            processamento.bfsRankingSegundaOpcao.bfsRanking(processamento.insumo, val, processamento.trabalhoAtual);
            int f = processamento.bfsRankingSegundaOpcao.depthcount[3];
            processamento.bfsRankingSegundaOpcao.bfsRanking(processamento.insumo, val);
            listRankingVal.add(processamento.bfsRankingSegundaOpcao.depthcount[3] - f);
        }
    }

    Integer getOpcao(List<Integer> opcoesPossiveis,
            Collection<Integer> excludentes) {
        Integer opcao = null;
        for (int i = 0; i < opcoesPossiveis.size(); i++) {
            Integer val = opcoesPossiveis.get(i);
            if (!excludentes.contains(val)) {
                opcao = val;
                break;
            }
        }
        return opcao;
    }

    Pair<Integer> desfazerUltimoTrabalho(Processamento processamento) {
        return processamento.desfazerUltimoTrabalho();
    }

}
