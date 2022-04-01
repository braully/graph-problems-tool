/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author strike
 */
public class PairShort implements Comparable<PairShort>, Serializable {

    /**
     * Key.
     */
    public final Short key;
    /**
     * Value.
     */
    public final Short value;

    private transient static final Map<Integer, PairShort> cache = new HashMap<>();

    public static final Integer MAX_I = 10000;

    public static PairShort get(Integer i, Integer j) {
        PairShort pair = null;
        Integer aux = null;
        if (i > j) {
            aux = i;
            i = j;
            j = aux;
        }
        aux = MAX_I * i + j;
        pair = cache.get(aux);

        if (pair == null) {
            pair = new PairShort(i, j);
        }
        return pair;
    }

    public static PairShort get(Short i, Short j) {
        PairShort pair = null;
        Integer aux = null;
        if (i > j) {
            aux = i.intValue();
            i = j;
            j = aux.shortValue();
        }

        aux = MAX_I * i + j;
        pair = cache.get(aux);

        if (pair == null) {
            pair = new PairShort(i, j);
        }
        return pair;
    }

    public static synchronized PairShort cached(Short i, Short j) {
        PairShort pair = null;
        Integer aux = null;
        if (i > j) {
            aux = i.intValue();
            i = j;
            j = aux.shortValue();
        }

        synchronized (cache) {
            aux = MAX_I * i + j;
            pair = cache.get(aux);

            if (pair == null) {
                pair = new PairShort(i, j);
                cache.put(aux, pair);
            }
        }
        //check consistence
//        if (!(pair.key.equals(i) && pair.value.equals(j))) {
//            throw new IllegalStateException(i + " " + j + " " + pair);
//        }
        return pair;
    }

    /**
     * Create an entry representing a mapping from the specified key to the
     * specified value.
     *
     * @param k Key (first element of the pair).
     * @param v Value (second element of the pair).
     */
    public PairShort(Integer k, Integer v) {
        this.key = k.shortValue();
        this.value = v.shortValue();
    }

    public PairShort(Short k, Short v) {
        this.key = k;
        this.value = v;
    }

    /**
     * Get the key.
     *
     * @return the key (first element of the pair).
     */
    public Short getKey() {
        return key;
    }

    /**
     * Get the value.
     *
     * @return the value (second element of the pair).
     */
    public Short getValue() {
        return value;
    }

    /**
     * Get the first element of the pair.
     *
     * @return the first element of the pair.
     * @since 3.1
     */
    public Short getFirst() {
        return key;
    }

    public Integer getFirstInt() {
        return key.intValue();
    }

    /**
     * Get the second element of the pair.
     *
     * @return the second element of the pair.
     * @since 3.1
     */
    public Short getSecond() {
        return value;
    }

    public Integer getSecondInt() {
        return value.intValue();
    }

    /**
     * Compare the specified object with this entry for equality.
     *
     * @param o Object.
     * @return {@code true} if the given object is also a map entry and the two
     * entries represent the same mapping.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PairShort)) {
            return false;
        } else {
            PairShort oP = (PairShort) o;
            return (key == null
                    ? oP.key == null
                    : key.equals(oP.key))
                    && (value == null
                            ? oP.value == null
                            : value.equals(oP.value));
        }
    }

    /**
     * Compute a hash code.
     *
     * @return the hash code value.
     */
    @Override
    public int hashCode() {
        int result = key == null ? 0 : key.hashCode();
        final int h = value == null ? 0 : value.hashCode();
        result = MAX_I * result + h;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getKey() + "," + getValue();
    }

    @Override
    public int compareTo(PairShort o) {
        int compare = this.key.compareTo(o.key);
        if (compare == 0) {
            compare = this.value.compareTo(o.value);
        }
        return compare;
    }

    public Integer[] toIntegerArray() {
        return new Integer[]{key.intValue(), value.intValue()};
    }

    public boolean equals(Integer i, Integer i0) {
        return key.equals(i.shortValue()) && value.equals(i0.shortValue());
    }

    public boolean contains(Short v) {
        return key.equals(v) || value.equals(v);
    }

    public boolean containsInt(int v) {
        return firstEquals(v) || secondEquals(v);
    }

    public boolean firstEquals(int i) {
        return key == i;
    }

    public boolean secondEquals(int i) {
        return value == i;
    }
}
