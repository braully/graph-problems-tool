package util;

/**
 *
 * @author braully
 */
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author strike
 */
public class ComparatorMap implements Comparator<Integer> {

    Map<Integer, ? extends Number> mapRanking = null;
    Integer[] ranking = null;
    Map<Integer, List<Integer>> mapListRanking = null;
    private int rankearOpcoesProfundidade;

    public ComparatorMap(int rankearOpcoesProfundidade) {
        this.rankearOpcoesProfundidade = rankearOpcoesProfundidade;
    }

    public Comparator<Integer> setMap(Map<Integer, ? extends Number> map) {
        this.mapRanking = map;
        this.ranking = null;
        this.mapListRanking = null;
        return (Comparator<Integer>) this;
    }

    public Comparator<Integer> setBfs(Integer[] bfs) {
        this.mapRanking = null;
        this.mapListRanking = null;
        this.ranking = bfs;
        return (Comparator<Integer>) this;
    }

    @Override
    public int compare(Integer o1, Integer o2) {
        int ret = 0;
        if (mapRanking != null) {
            ret = Integer.compare(this.mapRanking.get(o2).intValue(), this.mapRanking.get(o1).intValue());
        }
        if (ranking != null) {
            ret = Integer.compare(ranking[o2], ranking[o1]);
        }
        if (mapListRanking != null) {
            int cont = 0;

            if (mapListRanking.get(o1) == mapListRanking.get(o2)) {
                ret = 0;
            } else if (mapListRanking.get(o1) == null && mapListRanking.get(o2) != null) {
                ret = 1;
            } else if (mapListRanking.get(o1) != null && mapListRanking.get(o2) == null) {
                ret = -1;
            } else {
                while (ret == 0 && cont < rankearOpcoesProfundidade && cont < mapListRanking.get(o1).size()) {
                    ret = Integer.compare(mapListRanking.get(o2).get(cont), mapListRanking.get(o1).get(cont));
                    cont++;
                }
            }
        }
        if (ret == 0) {
            ret = Integer.compare(o1, o2);
        }
        return ret;
    }

    Comparator<? super Integer> setMapList(Map<Integer, List<Integer>> rankingAtual) {
        this.mapRanking = null;
        this.ranking = null;
        this.mapListRanking = rankingAtual;
        return (Comparator<Integer>) this;
    }
}
