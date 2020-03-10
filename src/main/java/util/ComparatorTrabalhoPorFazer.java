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
public class ComparatorTrabalhoPorFazer implements Comparator<Integer> {

    Map<Integer, List<Integer>> caminhosPossiveis;
    boolean aprofundar = true;

    public ComparatorTrabalhoPorFazer(Map<Integer, List<Integer>> caminhosPossiveis) {
        this.caminhosPossiveis = caminhosPossiveis;
    }

    public ComparatorTrabalhoPorFazer(Map<Integer, List<Integer>> caminhosPossiveis, boolean aprofundar) {
        this.caminhosPossiveis = caminhosPossiveis;
        this.aprofundar = aprofundar;
    }

    @Override
    public int compare(Integer t, Integer t1) {
        int ret = 0;
        int sizeT1 = caminhosPossiveis.get(t1).size();
        int sizeT = caminhosPossiveis.get(t).size();
        ret = Integer.compare(sizeT1, sizeT);
        int cont = 0;
        
        /*while (aprofundar && ret == 0 && cont < sizeT) {
            ret = Integer.compare(caminhosPossiveis.get(t).get(cont), caminhosPossiveis.get(t1).get(cont));
            cont++;
        }*/

        if (ret == 0) {
            ret = Integer.compare(t, t1);
        }
        return ret;
    }

}
