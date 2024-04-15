package util;

import java.util.ArrayList;

/**
 *
 * @author strike
 */
public class MapCountOpt extends ArrayList<Integer> {

    int[] inc;

    public MapCountOpt(int sizeInc) {
        inc = new int[sizeInc];
    }

    public Integer inc(Integer i) {
        inc[i]++;
        if (inc[i] == 1) {
            super.add(i);
        }
        return inc[i];
    }

    public Integer dec(Integer i) {
        inc[i]--;
        if (inc[i] == 0) {
            super.remove(i);
        }
        return inc[i];
    }

    public Integer setVal(Integer i, int val) {
        if (inc[i] == 0) {
            super.add(i);
        }
        inc[i] = val;
        return inc[i];
    }

    @Override
    public void clear() {
        clearArrayAux();
        super.clear();
    }

    public void clearArrayAux() {
        for (Integer v : this) {
            inc[v] = 0;
        }
    }

    public Iterable<Integer> keySet() {
        return this;
    }

    public int getCount(Integer v) {
        return inc[v];
    }
}
