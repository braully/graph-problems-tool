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
package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import com.github.braully.graph.operation.AbstractHeuristic;
import com.github.braully.graph.operation.GraphBigHNVOptm;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV1;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV2;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV3;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV4;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV5;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV5Tmp;
import com.github.braully.graph.operation.GraphHullNumberHeuristicV5Tmp3;
import com.github.braully.graph.operation.GraphTSSCordasco;
import com.github.braully.graph.operation.GraphTSSGreedy;
import com.github.braully.graph.operation.IGraphOperation;
import static com.github.braully.graph.operation.IGraphOperation.DEFAULT_PARAM_NAME_SET;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import util.UtilProccess;

/**
 *
 * @author strike
 */
public class ExecBigDataSets {

    public static final Map<String, int[]> resultadoArquivado = new HashMap<>();

    static {
        resultadoArquivado.put("TSS-Greedy-k2-BlogCatalog", new int[]{75, 60199});
        resultadoArquivado.put("TSS-Cordasco-k1-BlogCatalog", new int[]{1, 79415});
        resultadoArquivado.put("TSS-Greedy-k1-BlogCatalog", new int[]{1, 45039});
        resultadoArquivado.put("TSS-Cordasco-k1-BlogCatalog2", new int[]{1, 136434});
        resultadoArquivado.put("TSS-Greedy-k1-BlogCatalog2", new int[]{1, 69592});
        resultadoArquivado.put("TSS-Cordasco-k1-BlogCatalog3", new int[]{1, 137299});
        resultadoArquivado.put("TSS-Greedy-k1-BlogCatalog3", new int[]{1, 70063});
        resultadoArquivado.put("TSS-Cordasco-k1-BuzzNet", new int[]{1, 278575});
        resultadoArquivado.put("TSS-Greedy-k1-BuzzNet", new int[]{1, 147535});
        resultadoArquivado.put("TSS-Cordasco-k1-Delicious", new int[]{65, 296271});
        resultadoArquivado.put("TSS-Greedy-k1-Delicious", new int[]{58, 207280});
        resultadoArquivado.put("TSS-Cordasco-k1-Douban", new int[]{1, 689520});
        resultadoArquivado.put("TSS-Greedy-k1-Douban", new int[]{1, 355642});
        resultadoArquivado.put("TSS-Cordasco-k1-Last.fm", new int[]{55, 890668});
        resultadoArquivado.put("TSS-Greedy-k1-Last.fm", new int[]{55, 475961});
        resultadoArquivado.put("TSS-Cordasco-k1-Livemocha", new int[]{1, 1056541});
        resultadoArquivado.put("TSS-Greedy-k1-Livemocha", new int[]{1, 577846});
        resultadoArquivado.put("TSS-Cordasco-k1-ca-AstroPh", new int[]{297, 1065121});
        resultadoArquivado.put("TSS-Greedy-k1-ca-AstroPh", new int[]{289, 583740});
        resultadoArquivado.put("TSS-Cordasco-k1-ca-CondMat", new int[]{567, 1075946});
        resultadoArquivado.put("TSS-Greedy-k1-ca-CondMat", new int[]{567, 591514});
        resultadoArquivado.put("TSS-Cordasco-k1-ca-GrQc", new int[]{379, 1076494});
        resultadoArquivado.put("TSS-Greedy-k1-ca-GrQc", new int[]{354, 591970});
        resultadoArquivado.put("TSS-Cordasco-k1-ca-HepPh", new int[]{289, 1080797});
        resultadoArquivado.put("TSS-Greedy-k1-ca-HepPh", new int[]{276, 594971});
        resultadoArquivado.put("TSS-Cordasco-k1-ca-HepTh", new int[]{451, 1082918});
        resultadoArquivado.put("TSS-Greedy-k1-ca-HepTh", new int[]{427, 596293});
        resultadoArquivado.put("TSS-Cordasco-k2-BlogCatalog", new int[]{73, 1140581});
        resultadoArquivado.put("TSS-Greedy-k2-BlogCatalog", new int[]{75, 681223});
        resultadoArquivado.put("TSS-Cordasco-k2-BlogCatalog2", new int[]{2, 1223056});
        resultadoArquivado.put("TSS-Greedy-k2-BlogCatalog2", new int[]{2, 711886});
        resultadoArquivado.put("TSS-Cordasco-k2-BlogCatalog3", new int[]{2, 1223920});
        resultadoArquivado.put("TSS-Greedy-k2-BlogCatalog3", new int[]{2, 712357});
        resultadoArquivado.put("TSS-Cordasco-k2-BuzzNet", new int[]{2, 1375164});
        resultadoArquivado.put("TSS-Greedy-k2-BuzzNet", new int[]{2, 808907});
        resultadoArquivado.put("TSS-Cordasco-k2-Delicious", new int[]{650, 1392129});
        resultadoArquivado.put("TSS-Greedy-k2-Delicious", new int[]{671, 906010});
        resultadoArquivado.put("TSS-Cordasco-k2-Douban", new int[]{241, 1400198});
        resultadoArquivado.put("TSS-Greedy-k2-Douban", new int[]{245, 1014016});
        resultadoArquivado.put("TSS-Cordasco-k2-Last.fm", new int[]{330, 1467438});
        resultadoArquivado.put("TSS-Greedy-k2-Last.fm", new int[]{338, 1103060});
        resultadoArquivado.put("TSS-Cordasco-k2-Livemocha", new int[]{17, 1495413});
        resultadoArquivado.put("TSS-Greedy-k2-Livemocha", new int[]{19, 1219832});
        resultadoArquivado.put("TSS-Cordasco-k2-ca-AstroPh", new int[]{807, 1515578});
        resultadoArquivado.put("TSS-Greedy-k2-ca-AstroPh", new int[]{844, 1236747});
        resultadoArquivado.put("TSS-Cordasco-k2-ca-CondMat", new int[]{1704, 1536091});
        resultadoArquivado.put("TSS-Greedy-k2-ca-CondMat", new int[]{1817, 1258673});
        resultadoArquivado.put("TSS-Cordasco-k2-ca-GrQc", new int[]{810, 1536937});
        resultadoArquivado.put("TSS-Greedy-k2-ca-GrQc", new int[]{885, 1259642});
        resultadoArquivado.put("TSS-Cordasco-k2-ca-HepPh", new int[]{778, 1547128});
        resultadoArquivado.put("TSS-Greedy-k2-ca-HepPh", new int[]{819, 1270256});
        resultadoArquivado.put("TSS-Cordasco-k2-ca-HepTh", new int[]{1104, 1550500});
        resultadoArquivado.put("TSS-Greedy-k2-ca-HepTh", new int[]{1201, 1272756});
        resultadoArquivado.put("TSS-Cordasco-k3-BlogCatalog", new int[]{168, 1626270});
        resultadoArquivado.put("TSS-Greedy-k3-BlogCatalog", new int[]{171, 1349137});
        resultadoArquivado.put("TSS-Cordasco-k3-BlogCatalog2", new int[]{3, 1776008});
        resultadoArquivado.put("TSS-Greedy-k3-BlogCatalog2", new int[]{3, 1445158});
        resultadoArquivado.put("TSS-Cordasco-k3-BlogCatalog3", new int[]{4, 1776895});
        resultadoArquivado.put("TSS-Greedy-k3-BlogCatalog3", new int[]{4, 1445675});
        resultadoArquivado.put("TSS-Cordasco-k3-BuzzNet", new int[]{10, 1821609});
        resultadoArquivado.put("TSS-Greedy-k3-BuzzNet", new int[]{13, 1524561});
        resultadoArquivado.put("TSS-Cordasco-k3-Delicious", new int[]{1602, 1841147});
        resultadoArquivado.put("TSS-Greedy-k3-Delicious", new int[]{1694, 1635151});
        resultadoArquivado.put("TSS-Cordasco-k3-Douban", new int[]{640, 1997250});
        resultadoArquivado.put("TSS-Greedy-k3-Douban", new int[]{651, 1762434});
        resultadoArquivado.put("TSS-Cordasco-k3-Last.fm", new int[]{1065, 2154272});
        resultadoArquivado.put("TSS-Greedy-k3-Last.fm", new int[]{1095, 1979442});
        resultadoArquivado.put("TSS-Cordasco-k3-Livemocha", new int[]{87, 2209958});
        resultadoArquivado.put("TSS-Greedy-k3-Livemocha", new int[]{90, 2087984});
        resultadoArquivado.put("TSS-Cordasco-k3-ca-AstroPh", new int[]{1370, 2234810});
        resultadoArquivado.put("TSS-Greedy-k3-ca-AstroPh", new int[]{1469, 2109642});
        resultadoArquivado.put("TSS-Cordasco-k3-ca-CondMat", new int[]{3078, 2262949});
        resultadoArquivado.put("TSS-Greedy-k3-ca-CondMat", new int[]{3445, 2140744});
        resultadoArquivado.put("TSS-Cordasco-k3-ca-GrQc", new int[]{1246, 2263786});
        resultadoArquivado.put("TSS-Greedy-k3-ca-GrQc", new int[]{1394, 2141783});
        resultadoArquivado.put("TSS-Cordasco-k3-ca-HepPh", new int[]{1348, 2274448});
        resultadoArquivado.put("TSS-Greedy-k3-ca-HepPh", new int[]{1487, 2153652});
        resultadoArquivado.put("TSS-Cordasco-k3-ca-HepTh", new int[]{1797, 2278232});
        resultadoArquivado.put("TSS-Greedy-k3-ca-HepTh", new int[]{2045, 2156984});
        resultadoArquivado.put("TSS-Cordasco-k4-BlogCatalog", new int[]{288, 2347136});
        resultadoArquivado.put("TSS-Greedy-k4-BlogCatalog", new int[]{295, 2245174});
        resultadoArquivado.put("TSS-Cordasco-k4-BlogCatalog2", new int[]{4, 2492761});
        resultadoArquivado.put("TSS-Greedy-k4-BlogCatalog2", new int[]{4, 2339122});
        resultadoArquivado.put("TSS-Cordasco-k4-BlogCatalog3", new int[]{8, 2493791});
        resultadoArquivado.put("TSS-Greedy-k4-BlogCatalog3", new int[]{9, 2340111});
        resultadoArquivado.put("TSS-Cordasco-k4-BuzzNet", new int[]{37, 2562040});
        resultadoArquivado.put("TSS-Greedy-k4-BuzzNet", new int[]{41, 2450855});
        resultadoArquivado.put("TSS-Cordasco-k4-Delicious", new int[]{2638, 2583018});
        resultadoArquivado.put("TSS-Greedy-k4-Delicious", new int[]{2847, 2573054});
        resultadoArquivado.put("TSS-Cordasco-k4-Douban", new int[]{1072, 2743493});
        resultadoArquivado.put("TSS-Greedy-k4-Douban", new int[]{1116, 2695942});
        resultadoArquivado.put("TSS-Cordasco-k4-Last.fm", new int[]{2197, 2971567});
        resultadoArquivado.put("TSS-Greedy-k4-Last.fm", new int[]{2304, 2953684});
        resultadoArquivado.put("TSS-Cordasco-k4-Livemocha", new int[]{238, 3034940});
        resultadoArquivado.put("TSS-Greedy-k4-Livemocha", new int[]{242, 3100050});
        resultadoArquivado.put("TSS-Cordasco-k4-ca-AstroPh", new int[]{1904, 3059475});
        resultadoArquivado.put("TSS-Greedy-k4-ca-AstroPh", new int[]{2077, 3125097});
        resultadoArquivado.put("TSS-Cordasco-k4-ca-CondMat", new int[]{4474, 3090629});
        resultadoArquivado.put("TSS-Greedy-k4-ca-CondMat", new int[]{5191, 3164337});
        resultadoArquivado.put("TSS-Cordasco-k4-ca-GrQc", new int[]{1556, 3091292});
        resultadoArquivado.put("TSS-Greedy-k4-ca-GrQc", new int[]{1866, 3165296});
        resultadoArquivado.put("TSS-Cordasco-k4-ca-HepPh", new int[]{1854, 3101156});
        resultadoArquivado.put("TSS-Greedy-k4-ca-HepPh", new int[]{2093, 3177823});
        resultadoArquivado.put("TSS-Cordasco-k4-ca-HepTh", new int[]{2364, 3104278});
        resultadoArquivado.put("TSS-Greedy-k4-ca-HepTh", new int[]{2785, 3180952});
        resultadoArquivado.put("TSS-Cordasco-k5-BlogCatalog", new int[]{439, 3159903});
        resultadoArquivado.put("TSS-Greedy-k5-BlogCatalog", new int[]{452, 3259657});
        resultadoArquivado.put("TSS-Cordasco-k5-BlogCatalog2", new int[]{5, 3288737});
        resultadoArquivado.put("TSS-Greedy-k5-BlogCatalog2", new int[]{6, 3332179});
        resultadoArquivado.put("TSS-Cordasco-k5-BlogCatalog3", new int[]{15, 3289951});
        resultadoArquivado.put("TSS-Greedy-k5-BlogCatalog3", new int[]{17, 3333163});
        resultadoArquivado.put("TSS-Cordasco-k5-BuzzNet", new int[]{78, 3364048});
        resultadoArquivado.put("TSS-Greedy-k5-BuzzNet", new int[]{83, 3436951});
        resultadoArquivado.put("TSS-Cordasco-k5-Delicious", new int[]{3506, 3394200});
        resultadoArquivado.put("TSS-Greedy-k5-Delicious", new int[]{3839, 3538250});
        resultadoArquivado.put("TSS-Cordasco-k5-Douban", new int[]{1463, 3576176});
        resultadoArquivado.put("TSS-Greedy-k5-Douban", new int[]{1577, 3700621});
        resultadoArquivado.put("TSS-Cordasco-k5-Last.fm", new int[]{3658, 3857304});
        resultadoArquivado.put("TSS-Greedy-k5-Last.fm", new int[]{3876, 4057798});
        resultadoArquivado.put("TSS-Cordasco-k5-Livemocha", new int[]{482, 3953941});
        resultadoArquivado.put("TSS-Greedy-k5-Livemocha", new int[]{489, 4216414});
        resultadoArquivado.put("TSS-Cordasco-k5-ca-AstroPh", new int[]{2407, 3969314});
        resultadoArquivado.put("TSS-Greedy-k5-ca-AstroPh", new int[]{2676, 4237358});
        resultadoArquivado.put("TSS-Cordasco-k5-ca-CondMat", new int[]{5672, 3999053});
        resultadoArquivado.put("TSS-Greedy-k5-ca-CondMat", new int[]{6744, 4279948});
        resultadoArquivado.put("TSS-Cordasco-k5-ca-GrQc", new int[]{1809, 3999662});
        resultadoArquivado.put("TSS-Greedy-k5-ca-GrQc", new int[]{2186, 4280988});
        resultadoArquivado.put("TSS-Cordasco-k5-ca-HepPh", new int[]{2344, 4009832});
        resultadoArquivado.put("TSS-Greedy-k5-ca-HepPh", new int[]{2711, 4295596});
        resultadoArquivado.put("TSS-Cordasco-k5-ca-HepTh", new int[]{2850, 4012774});
        resultadoArquivado.put("TSS-Greedy-k5-ca-HepTh", new int[]{3467, 4299061});
        resultadoArquivado.put("TSS-Cordasco-k6-BlogCatalog", new int[]{603, 4077132});
        resultadoArquivado.put("TSS-Greedy-k6-BlogCatalog", new int[]{622, 4379436});
        resultadoArquivado.put("TSS-Cordasco-k6-BlogCatalog2", new int[]{10, 4206655});
        resultadoArquivado.put("TSS-Greedy-k6-BlogCatalog2", new int[]{12, 4454561});
        resultadoArquivado.put("TSS-Cordasco-k6-BlogCatalog3", new int[]{23, 4207764});
        resultadoArquivado.put("TSS-Greedy-k6-BlogCatalog3", new int[]{27, 4455596});
        resultadoArquivado.put("TSS-Cordasco-k6-BuzzNet", new int[]{198, 4296343});
        resultadoArquivado.put("TSS-Greedy-k6-BuzzNet", new int[]{203, 4574316});
        resultadoArquivado.put("TSS-Cordasco-k6-Delicious", new int[]{4263, 4333239});
        resultadoArquivado.put("TSS-Greedy-k6-Delicious", new int[]{4746, 4674615});
        resultadoArquivado.put("TSS-Cordasco-k6-Douban", new int[]{1941, 4536667});
        resultadoArquivado.put("TSS-Greedy-k6-Douban", new int[]{2129, 4843324});
        resultadoArquivado.put("TSS-Cordasco-k6-Last.fm", new int[]{5405, 4891118});
        resultadoArquivado.put("TSS-Greedy-k6-Last.fm", new int[]{5767, 5337998});
        resultadoArquivado.put("TSS-Cordasco-k6-Livemocha", new int[]{829, 4998858});
        resultadoArquivado.put("TSS-Greedy-k6-Livemocha", new int[]{840, 5521104});
        resultadoArquivado.put("TSS-Cordasco-k6-ca-AstroPh", new int[]{2887, 5016678});
        resultadoArquivado.put("TSS-Greedy-k6-ca-AstroPh", new int[]{3226, 5546019});
        resultadoArquivado.put("TSS-Cordasco-k6-ca-CondMat", new int[]{6737, 5045188});
        resultadoArquivado.put("TSS-Greedy-k6-ca-CondMat", new int[]{8193, 5592465});
        resultadoArquivado.put("TSS-Cordasco-k6-ca-GrQc", new int[]{1985, 5045741});
        resultadoArquivado.put("TSS-Greedy-k6-ca-GrQc", new int[]{2364, 5593466});
        resultadoArquivado.put("TSS-Cordasco-k6-ca-HepPh", new int[]{2764, 5055840});
        resultadoArquivado.put("TSS-Greedy-k6-ca-HepPh", new int[]{3247, 5609563});
        resultadoArquivado.put("TSS-Cordasco-k6-ca-HepTh", new int[]{3227, 5057884});
        resultadoArquivado.put("TSS-Greedy-k6-ca-HepTh", new int[]{3965, 5612870});
        resultadoArquivado.put("TSS-Cordasco-k7-BlogCatalog", new int[]{810, 5127170});
        resultadoArquivado.put("TSS-Greedy-k7-BlogCatalog", new int[]{828, 5714805});
        resultadoArquivado.put("TSS-Cordasco-k7-BlogCatalog2", new int[]{14, 5277279});
        resultadoArquivado.put("TSS-Greedy-k7-BlogCatalog2", new int[]{17, 5791833});
        resultadoArquivado.put("TSS-Cordasco-k7-BlogCatalog3", new int[]{30, 5278494});
        resultadoArquivado.put("TSS-Greedy-k7-BlogCatalog3", new int[]{34, 5792925});
        resultadoArquivado.put("TSS-Cordasco-k7-BuzzNet", new int[]{411, 5407754});
        resultadoArquivado.put("TSS-Greedy-k7-BuzzNet", new int[]{424, 5962311});
        resultadoArquivado.put("TSS-Cordasco-k7-Delicious", new int[]{4935, 5446874});
        resultadoArquivado.put("TSS-Greedy-k7-Delicious", new int[]{5488, 6063683});
        resultadoArquivado.put("TSS-Cordasco-k7-Douban", new int[]{2365, 5764842});
        resultadoArquivado.put("TSS-Greedy-k7-Douban", new int[]{2650, 6229355});
        resultadoArquivado.put("TSS-Cordasco-k7-Last.fm", new int[]{7280, 6157012});
        resultadoArquivado.put("TSS-Greedy-k7-Last.fm", new int[]{7897, 6777593});
        resultadoArquivado.put("TSS-Cordasco-k7-Livemocha", new int[]{1272, 6290435});
        resultadoArquivado.put("TSS-Greedy-k7-Livemocha", new int[]{1288, 6942465});
        resultadoArquivado.put("TSS-Cordasco-k7-ca-AstroPh", new int[]{3311, 6314778});
        resultadoArquivado.put("TSS-Greedy-k7-ca-AstroPh", new int[]{3763, 6973518});
        resultadoArquivado.put("TSS-Cordasco-k7-ca-CondMat", new int[]{7660, 6342906});
        resultadoArquivado.put("TSS-Greedy-k7-ca-CondMat", new int[]{9420, 7019296});
        resultadoArquivado.put("TSS-Cordasco-k7-ca-GrQc", new int[]{2121, 6343402});
        resultadoArquivado.put("TSS-Greedy-k7-ca-GrQc", new int[]{2483, 7020225});
        resultadoArquivado.put("TSS-Cordasco-k7-ca-HepPh", new int[]{3104, 6354860});
        resultadoArquivado.put("TSS-Greedy-k7-ca-HepPh", new int[]{3687, 7040474});
        resultadoArquivado.put("TSS-Cordasco-k7-ca-HepTh", new int[]{3530, 6357435});
        resultadoArquivado.put("TSS-Greedy-k7-ca-HepTh", new int[]{4307, 7045573});
        resultadoArquivado.put("TSS-Cordasco-k8-BlogCatalog", new int[]{999, 6420862});
        resultadoArquivado.put("TSS-Greedy-k8-BlogCatalog", new int[]{1024, 7143661});
        resultadoArquivado.put("TSS-Cordasco-k8-BlogCatalog2", new int[]{20, 6557269});
        resultadoArquivado.put("TSS-Greedy-k8-BlogCatalog2", new int[]{25, 7199036});
        resultadoArquivado.put("TSS-Cordasco-k8-BlogCatalog3", new int[]{45, 6558925});
        resultadoArquivado.put("TSS-Greedy-k8-BlogCatalog3", new int[]{50, 7200419});
        resultadoArquivado.put("TSS-Cordasco-k8-BuzzNet", new int[]{678, 6722429});
        resultadoArquivado.put("TSS-Greedy-k8-BuzzNet", new int[]{724, 7426887});
        resultadoArquivado.put("TSS-Cordasco-k8-Delicious", new int[]{5523, 6759022});
        resultadoArquivado.put("TSS-Greedy-k8-Delicious", new int[]{6208, 7505878});
        resultadoArquivado.put("TSS-Cordasco-k8-Douban", new int[]{2834, 7081632});
        resultadoArquivado.put("TSS-Greedy-k8-Douban", new int[]{3122, 7709246});
        resultadoArquivado.put("TSS-Cordasco-k8-Last.fm", new int[]{9189, 7508652});
        resultadoArquivado.put("TSS-Greedy-k8-Last.fm", new int[]{10126, 8364422});
        resultadoArquivado.put("TSS-Cordasco-k8-Livemocha", new int[]{1793, 7680890});
        resultadoArquivado.put("TSS-Greedy-k8-Livemocha", new int[]{1816, 8615604});
        resultadoArquivado.put("TSS-Cordasco-k8-ca-AstroPh", new int[]{3716, 7704914});
        resultadoArquivado.put("TSS-Greedy-k8-ca-AstroPh", new int[]{4247, 8648693});
        resultadoArquivado.put("TSS-Cordasco-k8-ca-CondMat", new int[]{8430, 7729986});
        resultadoArquivado.put("TSS-Greedy-k8-ca-CondMat", new int[]{10404, 8694866});
        resultadoArquivado.put("TSS-Cordasco-k8-ca-GrQc", new int[]{2229, 7730478});
        resultadoArquivado.put("TSS-Greedy-k8-ca-GrQc", new int[]{2548, 8695773});
        resultadoArquivado.put("TSS-Cordasco-k8-ca-HepPh", new int[]{3437, 7741305});
        resultadoArquivado.put("TSS-Greedy-k8-ca-HepPh", new int[]{4089, 8712873});
        resultadoArquivado.put("TSS-Cordasco-k8-ca-HepTh", new int[]{3778, 7743636});
        resultadoArquivado.put("TSS-Greedy-k8-ca-HepTh", new int[]{4548, 8717764});
        resultadoArquivado.put("TSS-Cordasco-k9-BlogCatalog", new int[]{1197, 7803788});
        resultadoArquivado.put("TSS-Greedy-k9-BlogCatalog", new int[]{1226, 8812609});
        resultadoArquivado.put("TSS-Cordasco-k9-BlogCatalog2", new int[]{28, 7935102});
        resultadoArquivado.put("TSS-Greedy-k9-BlogCatalog2", new int[]{34, 8889900});
        resultadoArquivado.put("TSS-Cordasco-k9-BlogCatalog3", new int[]{68, 7936635});
        resultadoArquivado.put("TSS-Greedy-k9-BlogCatalog3", new int[]{75, 8891520});
        resultadoArquivado.put("TSS-Cordasco-k9-BuzzNet", new int[]{956, 8119012});
        resultadoArquivado.put("TSS-Greedy-k9-BuzzNet", new int[]{1031, 9149405});
        resultadoArquivado.put("TSS-Cordasco-k9-Delicious", new int[]{6011, 8159957});
        resultadoArquivado.put("TSS-Greedy-k9-Delicious", new int[]{6775, 9251228});
        resultadoArquivado.put("TSS-Cordasco-k9-Douban", new int[]{3230, 8495243});
        resultadoArquivado.put("TSS-Greedy-k9-Douban", new int[]{3564, 9436574});
        resultadoArquivado.put("TSS-Cordasco-k9-Last.fm", new int[]{11239, 8951931});
        resultadoArquivado.put("TSS-Greedy-k9-Last.fm", new int[]{12607, 10204897});
        resultadoArquivado.put("TSS-Cordasco-k9-Livemocha", new int[]{2425, 9145074});
        resultadoArquivado.put("TSS-Greedy-k9-Livemocha", new int[]{2467, 10496046});
        resultadoArquivado.put("TSS-Cordasco-k9-ca-AstroPh", new int[]{4112, 9169811});
        resultadoArquivado.put("TSS-Greedy-k9-ca-AstroPh", new int[]{4718, 10529224});
        resultadoArquivado.put("TSS-Cordasco-k9-ca-CondMat", new int[]{9067, 9191774});
        resultadoArquivado.put("TSS-Greedy-k9-ca-CondMat", new int[]{11166, 10574461});
        resultadoArquivado.put("TSS-Cordasco-k9-ca-GrQc", new int[]{2312, 9192188});
        resultadoArquivado.put("TSS-Greedy-k9-ca-GrQc", new int[]{2590, 10575223});
        resultadoArquivado.put("TSS-Cordasco-k9-ca-HepPh", new int[]{3675, 9200811});
        resultadoArquivado.put("TSS-Greedy-k9-ca-HepPh", new int[]{4381, 10592708});
        resultadoArquivado.put("TSS-Cordasco-k9-ca-HepTh", new int[]{3970, 9202949});
        resultadoArquivado.put("TSS-Greedy-k9-ca-HepTh", new int[]{4710, 10596891});
        resultadoArquivado.put("TSS-Cordasco-k10-BlogCatalog", new int[]{1421, 9263480});
        resultadoArquivado.put("TSS-Greedy-k10-BlogCatalog", new int[]{1445, 10707113});
        resultadoArquivado.put("TSS-Cordasco-k10-BlogCatalog2", new int[]{37, 9393311});
        resultadoArquivado.put("TSS-Greedy-k10-BlogCatalog2", new int[]{45, 10784900});
        resultadoArquivado.put("TSS-Cordasco-k10-BlogCatalog3", new int[]{82, 9394579});
        resultadoArquivado.put("TSS-Greedy-k10-BlogCatalog3", new int[]{91, 10785963});
        resultadoArquivado.put("TSS-Cordasco-k10-BuzzNet", new int[]{1199, 9543444});
        resultadoArquivado.put("TSS-Greedy-k10-BuzzNet", new int[]{1302, 11028366});
        resultadoArquivado.put("TSS-Cordasco-k10-Delicious", new int[]{6432, 9583728});
        resultadoArquivado.put("TSS-Greedy-k10-Delicious", new int[]{7282, 11135049});
        resultadoArquivado.put("TSS-Cordasco-k10-Douban", new int[]{3591, 9951753});
        resultadoArquivado.put("TSS-Greedy-k10-Douban", new int[]{3999, 11332677});
        resultadoArquivado.put("TSS-Cordasco-k10-Last.fm", new int[]{13261, 10414111});
        resultadoArquivado.put("TSS-Greedy-k10-Last.fm", new int[]{15037, 12189433});
        resultadoArquivado.put("TSS-Cordasco-k10-Livemocha", new int[]{3097, 10596719});
        resultadoArquivado.put("TSS-Greedy-k10-Livemocha", new int[]{3175, 12469934});
        resultadoArquivado.put("TSS-Cordasco-k10-ca-AstroPh", new int[]{4476, 10621879});
        resultadoArquivado.put("TSS-Greedy-k10-ca-AstroPh", new int[]{5151, 12508716});
        resultadoArquivado.put("TSS-Cordasco-k10-ca-CondMat", new int[]{9598, 10642258});
        resultadoArquivado.put("TSS-Greedy-k10-ca-CondMat", new int[]{11741, 12551881});
        resultadoArquivado.put("TSS-Cordasco-k10-ca-GrQc", new int[]{2376, 10642652});
        resultadoArquivado.put("TSS-Greedy-k10-ca-GrQc", new int[]{2617, 12552583});
        resultadoArquivado.put("TSS-Cordasco-k10-ca-HepPh", new int[]{3895, 10651092});
        resultadoArquivado.put("TSS-Greedy-k10-ca-HepPh", new int[]{4629, 12570163});
        resultadoArquivado.put("TSS-Cordasco-k10-ca-HepTh", new int[]{4130, 10653049});
        resultadoArquivado.put("TSS-Greedy-k10-ca-HepTh", new int[]{4800, 12574004});
        resultadoArquivado.put("TSS-Cordasco-k2-BlogCatalog", new int[]{73, 43229});
        resultadoArquivado.put("TSS-Cordasco-k2-BlogCatalog", new int[]{73, 43567});
        resultadoArquivado.put("TSS-Cordasco-k2-BlogCatalog2", new int[]{2, 200004});
        resultadoArquivado.put("TSS-Cordasco-k2-BlogCatalog3", new int[]{2, 201241});
        resultadoArquivado.put("TSS-Cordasco-k2-BuzzNet", new int[]{2, 347545});
        resultadoArquivado.put("TSS-Cordasco-k2-Delicious", new int[]{650, 379078});
        resultadoArquivado.put("TSS-Cordasco-k2-Douban", new int[]{241, 412210});
        resultadoArquivado.put("TSS-Cordasco-k2-Last.fm", new int[]{330, 502092});

    }

    public static void main(String... args) throws FileNotFoundException, IOException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String[] dataSets = new String[]{
            "ca-GrQc", "ca-HepTh",
            "ca-CondMat", "ca-HepPh",
            "ca-AstroPh",
            "Douban",
            "Delicious",
            "BlogCatalog3",
            "BlogCatalog2",
            "Livemocha",
            "BlogCatalog",
            "BuzzNet",
            "Last.fm", //             "YouTube2"
        };
//        GraphHullNumberHeuristicV5Tmp heur = new GraphHullNumberHeuristicV5Tmp();

        GraphHullNumberHeuristicV4 heur4 = new GraphHullNumberHeuristicV4();
        heur4.setVerbose(false);
        GraphHullNumberHeuristicV3 heur3 = new GraphHullNumberHeuristicV3();
        heur3.setVerbose(false);
        GraphHullNumberHeuristicV2 heur2 = new GraphHullNumberHeuristicV2();
        heur2.setVerbose(false);
        GraphHullNumberHeuristicV1 heur1 = new GraphHullNumberHeuristicV1();
//        heur1.fatorLimite = 2;
        heur1.setVerbose(true);
        GraphHullNumberHeuristicV5 heur5 = new GraphHullNumberHeuristicV5();
        heur5.setVerbose(false);
        GraphHullNumberHeuristicV5Tmp heur5t = new GraphHullNumberHeuristicV5Tmp();
        heur5t.setVerbose(false);
//        GraphHullNumberHeuristicV5Tmp2 heur5t2 = new GraphHullNumberHeuristicV5Tmp2();
        GraphHullNumberHeuristicV5Tmp3 heur5t2 = new GraphHullNumberHeuristicV5Tmp3();
        GraphBigHNVOptm optm = new GraphBigHNVOptm();

        heur5t2.setVerbose(false);
        heur5t2.startVertice = false;

        optm.pularAvaliacaoOffset = true;
        optm.resetParameters();
        optm.setParameter(GraphBigHNVOptm.pdificuldadeTotal, true);

//        optm.setParameter(GraphBigHNVOptm.pdeltaHsi, true);
//        optm.setParameter(GraphBigHNVOptm.pgrau, true);
//        optm.setParameter(GraphBigHNVOptm.pbonusTotal, true);
//        optm.setParameter(GraphBigHNVOptm.pdificuldadeParcial, true);
//        optm.setParameter(GraphBigHNVOptm.pbonusParcialNormalizado, true);
//        optm.setParameter(GraphBigHNVOptm.pdificuldadeTotal, true);
//        optm.setParameter(GraphBigHNVOptm.pdificuldadeParcial, false);
//        optm.setParameter(GraphBigHNVOptm.pbonusTotal, false);
        GraphTSSCordasco tss = new GraphTSSCordasco();
        GraphTSSGreedy tssg = new GraphTSSGreedy();

        AbstractHeuristic[] operations = new AbstractHeuristic[]{
                        tss, //            heur1,
            //            heur2, 
            //            heur3, heur4,
            //            heur5,
            //            heur5t,
            //            tssg,
            //            heur5t2
            optm
        };
        long totalTime[] = new long[operations.length];
        Integer[] result = new Integer[operations.length];
        Integer[] delta = new Integer[operations.length];
        int[] contMelhor = new int[operations.length];
        int[] contPior = new int[operations.length];
        int[] contIgual = new int[operations.length];
        for (int i = 0; i < operations.length; i++) {
            contMelhor[i] = contPior[0] = contIgual[0] = 0;
        }

        Arrays.sort(dataSets);

        String strResultFile = "resultado-" + ExecBigDataSets.class.getSimpleName() + ".txt";
        File resultFile = new File(strResultFile);
        BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile, true));

        for (int k = 1; k <= 10; k++) {
//            heur1.K = heur2.K = heur3.K
//                    = heur4.K = heur5.K = heur5t.K = heur5t2.K = tss.K = tssg.K = k;
//            tss.setR(k);
            heur5t2.setR(k);
            tssg.setR(k);
            optm.setR(k);
            System.out.println("-------------\n\nK: " + k);

            for (String s : dataSets) {
                System.out.println("\n-DATASET: " + s);

                UndirectedSparseGraphTO<Integer, Integer> graphES
                        = null;
                try {
                    graphES = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/" + s + "/nodes.csv"),
                            new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/" + s + "/edges.csv"));
                } catch (FileNotFoundException e) {
                    graphES = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/" + s + "/" + s + ".txt"));
                }
                if (graphES == null) {
                    System.out.println("Fail to Load GRAPH: " + s);
                }
                System.out.println("Loaded Graph: " + s + " " + graphES.getVertexCount() + " " + graphES.getEdgeCount());

                for (int i = 0; i < operations.length; i++) {
                    String arquivadoStr = operations[i].getName() + "-k" + k + "-" + s;
                    Map<String, Object> doOperation = null;
//                    BeanUtils.setProperty(operations[i], "K", k);
//                    PropertyUtils.setSimpleProperty(operations[i], "K", k);
                    System.out.println("*************");
                    System.out.print(" - EXEC: " + operations[i].getName() + "-k: " + k + " g:" + s + " " + graphES.getVertexCount() + " ");
                    int[] get = resultadoArquivado.get(arquivadoStr);
                    if (get != null) {
                        result[i] = get[0];
                        totalTime[i] = get[1];
                    } else {
                        UtilProccess.printStartTime();
                        doOperation = operations[i].doOperation(graphES);
                        result[i] = (Integer) doOperation.get(IGraphOperation.DEFAULT_PARAM_NAME_RESULT);
                        totalTime[i] += UtilProccess.printEndTime();
                        System.out.println(" - arquivar: resultadoArquivado.put(\"" + arquivadoStr + "\", new int[]{" + result[i] + ", " + totalTime[i] + "});");
                    }
                    System.out.println(" - Result: " + result[i]);

                    String out = "Big\t" + s + "\t" + graphES.getVertexCount() + "\t"
                            + graphES.getEdgeCount()
                            + "\t" + k + "\t" + operations[i].getName()
                            + "\t" + result[i] + "\t" + totalTime[i] + "\n";

                    System.out.print("xls: " + out);

                    writer.write(out);
//                        writer.write(resultProcess);
                    writer.flush();

                    if (doOperation != null) {
                        boolean checkIfHullSet = operations[i].checkIfHullSet(graphES, ((Set<Integer>) doOperation.get(DEFAULT_PARAM_NAME_SET)));
                        if (!checkIfHullSet) {
                            System.out.println("ALERT: ----- RESULTADO ANTERIOR IS NOT HULL SET");
                            System.err.println("ALERT: ----- RESULTADO ANTERIOR IS NOT HULL SET");
//                            throw new IllegalStateException("CORDASSO IS NOT HULL SET");
                        }
                    }
                    if (i == 0) {
                        if (get == null) {
                            delta[i] = 0;
                        }
                    } else {
                        delta[i] = result[0] - result[i];

                        long deltaTempo = totalTime[0] - totalTime[i];
                        System.out.print(" - Tempo: ");

                        if (deltaTempo >= 0) {
                            System.out.println(" +RAPIDO " + deltaTempo);
                        } else {
                            System.out.println(" +LENTO " + deltaTempo);
                        }
                        System.out.print(" - Delta: " + delta[i] + " ");
                        if (delta[i] == 0) {
                            System.out.println(" = igual");
                            contIgual[i]++;
                        } else if (delta[i] > 0) {
                            System.out.println(" + MELHOR ");
                            contMelhor[i]++;
                        } else {
                            System.out.println(" - PIOR ");
                            contPior[i]++;
                        }
                        System.out.println(delta[i]);
                    }
                    System.out.println();
                }
            }
        }
        writer.flush();
        writer.close();
        System.out.println("Resumo ");
        for (int i = 1; i < operations.length; i++) {
            System.out.println("Operacao: " + operations[i].getName());
            System.out.println("Melhor: " + contMelhor[i]);
            System.out.println("Pior: " + contPior[i]);
            System.out.println("Igual: " + contIgual[i]);
        }
    }
}
