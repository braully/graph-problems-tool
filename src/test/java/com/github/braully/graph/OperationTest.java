/*
 * The MIT License
 *
 * Copyright 2022 strike.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.braully.graph;

import com.github.braully.graph.operation.GraphSubgraph;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

/**
 *
 * @author strike
 */
public class OperationTest {

    @Test
    public void testSubgraph57() throws FileNotFoundException, IOException {
        GraphSubgraph subgraph = new GraphSubgraph();
        UndirectedSparseGraphTO<Integer, Integer> graphES = UtilGraph.loadGraphES(new FileInputStream("./esqueleto-ultimo-grafo-moore.es"));
        //String viz = "1,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100,101,102,103,104,105,106,107,108,109,110,111,113,114,116,117,118,119,120,3192,121,3193,122,3194,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155,156,157,158,159,160,161,162,163,164,165,166,167,168,169,170,171,172,173,174,175,176,177,3249,178,179,180,181,182,183,184,185,186,187,188,189,190,191,192,193,194,195,196,197,198,199,200,201,202,203,204,205,206,207,208,209,210,211,212,213,214,215,216,217,218,219,220,221,225,227,229,231,233,235,237,239,241,243,245,247,249,251,253,255,257,259,261,263,265,267,269,271,273,275,277,279,281,283,285,287,289,291,293,295,297,299,301,303,305,307,309,311,313,315,317,319,321,323,325,327,329,330,332,334,336,338,340,342,344,346,348,350,352,354,356,358,360,362,364,366,368,370,372,374,376,378,380,382,384,386,388,390,392,394,396,398,400,402,404,406,408,410,412,414,416,418,420,422,424,426,428,430,432,434";
        String viz = "0, 56, 1, 3247, 57, 3248, 3191, 55, 110, 113, 112, 3249, 3192, 329, 221, 111, 3224, 54, 326, 218, 3119, 102, 2808, 35, 3193, 3246, 3118, 46, 328, 220, 282, 88, 283, 32, 176, 89, 177, 33, 3190, 327, 219, 2809, 91, 3245, 2588, 30, 2589, 86, 114, 58, 115, 2, 434, 435, 224, 59, 225, 3, 2180, 23, 916, 7, 3117, 310, 202, 2792, 1912, 19, 289, 2095, 78, 181, 2774, 93, 1511, 70, 2810, 2181, 79, 917, 63, 3116, 311, 203, 2793, 1913, 75, 174, 2643, 34, 2645, 2647, 36, 1252, 11, 2667, 2671, 48, 2683, 2685, 175, 2642, 90, 2646, 92, 1253, 67, 2666, 2670, 104, 2682, 2684, 2689, 2705, 43, 2711, 794, 6, 284, 2721, 51, 2727, 2729, 696, 5, 2640, 2704, 99, 2710, 795, 62, 285, 2720, 107, 2726, 2728, 697, 61, 2641, 2688, 288, 2094, 22, 180, 2644, 2775, 37, 1510, 14, 2811, 3198, 3238, 3237, 3213, 3226, 3215, 2572, 2580, 106, 279, 2590, 171, 591, 60, 979, 64, 2019, 77, 2544, 2546, 2550, 2573, 2581, 50, 278, 2591, 170, 590, 4, 978, 8, 2018, 21, 2545, 2547, 2551, 385, 389, 391, 393, 395, 417, 433, 337, 349, 12, 222, 384, 388, 390, 392, 394, 416, 432, 336, 348, 68, 223, 3223, 3204, 3206, 3225, 3232, 3212, 3242, 3210, 3219, 3203, 521, 529, 537, 539, 330, 467, 489, 493, 495, 499, 116, 505, 38, 507, 39, 520, 528, 536, 538, 331, 466, 488, 492, 494, 498, 117, 504, 94, 506, 95, 265, 2192, 108, 2196, 2198, 2071, 157, 677, 475, 2148, 2152, 2154, 2158, 371, 2174, 920, 932, 934, 443, 339, 854, 71, 858, 73, 862, 233, 884, 888, 890, 125, 894, 2696, 2697, 577, 464, 74, 2000, 20, 465, 18, 2001, 76, 340, 341, 1504, 1505, 230, 231, 124, 398, 399, 2848, 2849, 204, 103, 205, 47, 600, 601, 864, 2149, 2024, 2025, 2674, 2675, 1160, 10, 1161, 66, 928, 2730, 2731, 1856, 1857, 594, 595, 984, 985, 2140, 82, 350, 69, 351, 13, 484, 84, 485, 28, 240, 241, 3188, 53, 3189, 109, 257, 1928, 1930, 149, 1049, 65, 1876, 1878, 85, 1880, 1884, 1886, 863, 1890, 1896, 363, 1900, 96, 386, 87, 387, 31, 524, 525, 154, 155, 1567, 2210, 24, 2978, 40, 2211, 80, 2979, 596, 597, 982, 983, 860, 1888, 3170, 3171, 3062, 44, 3063, 100, 1792, 772, 262, 2073, 2075, 25, 2085, 2089, 2091, 2117, 2119, 1864, 2131, 2133, 2135, 472, 868, 368, 2442, 2443, 402, 2194, 403, 286, 287, 2084, 1076, 9, 1077, 1366, 1367, 3160, 49, 3161, 105, 872, 3182, 52, 3183, 1397, 510, 97, 511, 41, 898, 2692, 802, 2851, 292, 2853, 2342, 26, 2855, 2857, 2602, 2867, 184, 2883, 2885, 2504, 29, 2648, 1894, 2162, 502, 2554, 1537, 1543, 138, 1549, 1038, 1551, 558, 1471, 15, 1473, 16, 1479, 456, 1481, 1487, 852, 1501, 352, 1507, 1513, 246, 1533, 1922, 1296, 1297, 158, 159, 2093, 2494, 2495, 1734, 17, 1735, 1493, 354, 355, 2150, 3194, 3222, 3202, 3221, 3234, 3228, 3240, 3220, 3236, 3195, 264, 2193, 2195, 2197, 2070, 2199, 156, 1566, 676, 576, 1866, 474, 2141, 870, 2151, 2153, 2155, 2159, 370, 2175, 899, 921, 929, 933, 935, 442, 338, 855, 859, 861, 865, 871, 232, 873, 885, 889, 891, 895, 256, 1923, 1929, 1931, 148, 1048, 1867, 1877, 1879, 1881, 1885, 1887, 1889, 1891, 1897, 362, 1901, 1396, 1668, 1669, 72, 3080, 3081, 268, 81, 269, 2066, 2067, 2224, 1478, 2123, 850, 1234, 851, 1235, 2932, 2933, 2447, 1959, 2732, 2734, 179, 2742, 2752, 2762, 2768, 2770, 1509, 2279, 2157, 497, 2549, 893, 2687, 900, 901, 518, 101, 519, 45, 136, 137, 2318, 2574, 2319, 2575, 1564, 2594, 2595, 2738, 356, 357, 1904, 98, 1905, 42, 242, 243, 2163, 1784, 1785, 1532, 897, 2691, 1671, 2826, 397, 2830, 2832, 1173, 2840, 2842, 2846, 291, 1963, 2733, 2097, 183, 2773, 603, 1893, 2031, 2161, 501, 2553, 2812, 1280, 133, 1286, 1288, 1298, 941, 1216, 1218, 451, 1226, 847, 1232, 1236, 347, 1248, 1254, 1258, 1262, 751, 1266, 1268, 1272, 1794, 1795, 140, 908, 141, 909, 1174, 1708, 1709, 1079, 440, 441, 962, 1747, 2390, 27";
        Set<Integer> set = new LinkedHashSet<>();
        String[] split = viz.split(",");
        for (String str : split) {
            int parseInt = Integer.parseInt(str.trim());
            set.add(parseInt);
        }
        graphES.setSet(set);

        UndirectedSparseGraphTO sub = subgraph.subGraphInduced(graphES, set);

        int cont = 0;
        while (removeVerticesDegreeTwo(sub)) {
            cont++;
            System.out.println("Remove round " + cont);
        }

        System.out.println("Subgraph: N" + sub.getVertexCount() + ",M" + sub.getEdgeCount());
        System.out.println("ES-Subgraph: ");
        System.out.println(sub.getEdgeString());
        System.out.println();

//        Map<String, Object> doOperation = subgraph.doOperation(graphES);
//        System.out.println(doOperation);
    }

    @Test
    public void testSubgraph7() throws FileNotFoundException, IOException {
        GraphSubgraph subgraph = new GraphSubgraph();
        UndirectedSparseGraphTO<Integer, Integer> graphES = UtilGraph.loadGraphES(new FileInputStream("./esqueleto-grafo-moore-50.es"));
        String viz = "0, 6, 1, 47, 7, 48, 41, 5, 10, 13, 12, 49, 42, 21, 11, 29, 44, 18, 4, 26, 32, 2, 39, 9, 46, 20, 38, 3";
        Set<Integer> set = new LinkedHashSet<>();
        String[] split = viz.split(",");
        for (String str : split) {
            int parseInt = Integer.parseInt(str.trim());
            set.add(parseInt);
        }
        graphES.setSet(set);

        UndirectedSparseGraphTO sub = subgraph.subGraphInduced(graphES, set);

        System.out.println("Subgraph: N" + sub.getVertexCount() + ",M" + sub.getEdgeCount());
        System.out.println("ES-Subgraph-7-Before: ");
        System.out.println(sub.getEdgeString());
        
        int cont = 0;
        while (removeVerticesDegreeTwo(sub)) {
            cont++;
            System.out.println("Remove round " + cont);
        }

        System.out.println("Subgraph: N" + sub.getVertexCount() + ",M" + sub.getEdgeCount());
        System.out.println("ES-Subgraph-7: ");
        System.out.println(sub.getEdgeString());
        System.out.println();

//        Map<String, Object> doOperation = subgraph.doOperation(graphES);
//        System.out.println(doOperation);
    }

    private boolean removeVerticesDegreeTwo(UndirectedSparseGraphTO sub) {
        Set<Integer> removeList = new LinkedHashSet<>();
        System.out.print("Remove: ");

        for (Integer v : (Collection<Integer>) sub.getVertices()) {
            if (sub.degree(v) <= 2) {
                removeList.add(v);
            }
        }
        for (Integer v : removeList) {
            sub.removeVertex(v);
            System.out.print("-" + v + ",");
        }
        System.out.println();
        return !removeList.isEmpty();
    }
}
