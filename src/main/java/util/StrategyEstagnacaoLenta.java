package util;

import java.util.List;

public class StrategyEstagnacaoLenta
        extends StrategyEstagnacao
        implements IGenStrategy {

    public String getName() {
        return "Estagnação lenta";
    }

    public void rankearOpcao(Processamento processamento, Integer posicaoAtual, Integer val) {
        processamento.bfsRankingTotal(val);
        List<Integer> listRankingVal = processamento.historicoRanking.get(posicaoAtual).get(val);
        listRankingVal.clear();        
        listRankingVal.add(processamento.bfsRanking.depthcount[4]);
        listRankingVal.add(-processamento.bfsRanking.depthcount[3]);
        listRankingVal.add(processamento.bfsRanking.depthcount[2]);
    }
}
