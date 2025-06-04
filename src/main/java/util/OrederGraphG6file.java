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
package util;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author strike
 */
public class OrederGraphG6file {

    public static void main(String... args) throws Exception {
        String dir = "/home/strike/";
        String unsortedFile = "diameter-2-list.g6";
        String sortedFile = "diameter-2-list-ordered.g6";
        BufferedReader inputFile = new BufferedReader(new FileReader(new File(dir + unsortedFile)));

        String linha = null;
        TreeMap<Integer, List<String>> map = new TreeMap<>();
        while ((linha = inputFile.readLine()) != null) {
            UndirectedSparseGraphTO<Integer, Integer> loadGraphG6 = UtilGraph.loadGraphG6(linha);
            if (loadGraphG6 != null) {
                int k = loadGraphG6.getVertexCount();
                map.computeIfAbsent(k, key -> new ArrayList<>()).add(linha);
            }
        }
        FileWriter filew = new FileWriter(new File(dir, sortedFile));
        for (List<String> strs : map.values()) {
            for (String s : strs) {
                filew.write(s);
                filew.write("\n");
            }
        }
        filew.close();
    }
}
