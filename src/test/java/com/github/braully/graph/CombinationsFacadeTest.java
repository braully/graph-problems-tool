package com.github.braully.graph;

import org.junit.Test;

/**
 *
 * @author Braully Rocha da Silva
 */
public class CombinationsFacadeTest {

    @Test
    public void testCombination5_3() {
        int n = 10;
        int k = 3;
        int[] comb = new int[k];
        long maxCombinations = CombinationsFacade.maxCombinations(n, k);
        CombinationsFacade.initialCombination(n, k, comb);
        for (int i = 0; i < maxCombinations; i++) {
            System.out.printf("%3d-", i);
            CombinationsFacade.printCombination(comb);
            System.out.print(" - ");
            System.out.print(CombinationsFacade.lexicographicIndex(n, k, comb));
            System.out.println();
            CombinationsFacade.nextCombination(n, k, comb);
        }
    }
}
