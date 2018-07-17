package com.github.braully.graph;

import java.math.BigInteger;
import java.util.Iterator;
import org.apache.commons.math3.util.CombinatoricsUtils;

/**
 *
 * @author braully
 */
public class CombinationsFacade {

    public static synchronized int[] getCombinationNKByLexicographIndex(int n, int k, long index) {
        int[] comb = null;
        if (n <= 0 || k <= 0 || index < 0) {
            return comb;
        }
        long maxCombinations = CombinationsFacade.maxCombinations(n, k);
        if (index > maxCombinations) {
            return comb;
        }
        comb = new int[k];
        CombinationsFacade.initialCombination(n, k, comb, index);
        return comb;
    }

    public static synchronized long lexicographicIndex(int n, int k, int[] combination) {
        long index = 0;
        int j = 0;
        for (int i = 0; i < k; ++i) {
            for (++j; j != combination[i] + 1; ++j) {
                int nj = n - j;
                int kj = k - i - 1;
                long maxComb = maxCombinations(nj, kj);
                index = index + maxComb;
                if (maxComb == 0) {
                    index++;
                }
            }
        }
        return index;
    }

    public static synchronized void printCombination(int[] currentCombination) {
        System.out.printf("S = {");
        for (int i = 0; i < currentCombination.length; i++) {
            System.out.printf("%2d", currentCombination[i]);
            if (i < currentCombination.length - 1) {
                System.out.printf(", ");
            }
        }
        System.out.printf(" }");
    }

    public static synchronized long maxCombinations(int n, int k) {
        if (n == 0 || k == 0) {
            return 0;
        }
        if (n < k) {
            return 0;
        }
        if (n == k) {
            return 1;
        }
        long delta, idxMax;
        if (k < n - k) {
            delta = n - k;
            idxMax = k;
        } else {
            delta = k;
            idxMax = n - k;
        }

        long ans = delta + 1;
        for (int i = 2; i <= idxMax; ++i) {
            ans = (ans * (delta + i)) / i;
        }
        return ans;
    }

    public static synchronized BigInteger maxCombinationsBig(int n, int k) {
        if (n == 0 || k == 0) {
            return BigInteger.ZERO;
        }
        if (n < k) {
            return BigInteger.ZERO;
        }
        if (n == k) {
            return BigInteger.ONE;
        }
        long delta, idxMax;
        if (k < n - k) {
            delta = n - k;
            idxMax = k;
        } else {
            delta = k;
            idxMax = n - k;
        }

        long ans = delta + 1;
        BigInteger ansbig = new BigInteger("" + ans);
        for (int i = 2; i <= idxMax; ++i) {
            ans = (ans * (delta + i)) / i;
            BigInteger bigi = new BigInteger("" + i);
            BigInteger deltai = new BigInteger("" + delta);
            deltai = deltai.add(bigi);
            ansbig = ansbig.multiply(deltai);
            ansbig = ansbig.divide(bigi);
        }
        return ansbig;
    }

    public static synchronized void initialCombination(int n, int k, int[] combinationArray, long idx) {
        int a = n;
        int b = k;
        long x = (maxCombinations(n, k) - 1) - idx;
        for (int i = 0; i < k; ++i) {
            combinationArray[i] = a - 1;
            while (maxCombinations(combinationArray[i], b) > x) {
                --combinationArray[i];
            }
            x = x - maxCombinations(combinationArray[i], b);
            a = combinationArray[i];
            b = b - 1;
        }

        for (int i = 0; i < k; ++i) {
            combinationArray[i] = (n - 1) - combinationArray[i];
        }
    }

    public static synchronized void initialCombination(int n, int k, int[] combinationArray) {
        for (int i = 0; i < k; i++) {
            combinationArray[i] = i;
        }
    }

    public static synchronized void nextCombination(int n,
            int k,
            int[] currentCombination) {
        if (currentCombination[0] == n - k) {
            return;
        }
        int i;
        for (i = k - 1; i > 0 && currentCombination[i] == n - k + i; --i);
        ++currentCombination[i];
        for (int j = i; j < k - 1; ++j) {
            currentCombination[j + 1] = currentCombination[j] + 1;
        }
    }

}
