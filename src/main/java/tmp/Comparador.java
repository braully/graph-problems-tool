/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tmp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author strike
 */
public class Comparador {

    public static void main(String... args) throws FileNotFoundException, IOException {
        String[] strFiles = new String[]{
            "resultado-TSS-Cordasco.hog-graphs-ge20-le50-ordered.txt",
            //            "resultado-Hull_Number_Heuristic.hog-graphs-ge20-le50-ordered.txt", //            "resultado-Hull_Number_Heuristic_V6.hog-graphs-ge20-le50-ordered.txt"
            //            "resultado-Hull_Number_Heuristic_V5.hog-graphs-ge20-le50-ordered.txt",
            "resultado-Hull_Number_Heuristic_V5-tmp.hog-graphs-ge20-le50-ordered.txt"
//            "resultado-Hull_Number_Heuristic_V5-tmp2.hog-graphs-ge20-le50-ordered.txt"
        };
        BufferedReader[] files = new BufferedReader[strFiles.length];
        for (int i = 0; i < strFiles.length; i++) {
            files[i] = new BufferedReader(new FileReader(strFiles[i]));
        }
        String line = null;
        String line2 = null;
        int igual = 0;
        int melhor = 0;
        int pior = 0;
        double total = 0;
        while (null != (line = files[0].readLine())) {
            line2 = files[1].readLine();
            String[] split = line.split("\t");
            String[] split2 = line2.split("\t");
            int r1 = Integer.parseInt(split[2]);
            int r2 = Integer.parseInt(split2[2]);
            if (r1 == r2) {
                igual++;
            } else {
                if (r1 < r2) {
                    pior++;
                } else {
                    melhor++;
                }
            }
            total++;
        }
        System.out.println("Resultado ");
        System.out.println("total: " + total);
        System.out.println("Melhor: " + melhor);
        System.out.println("Pior: " + pior);
        System.out.println("Pior-perc: " + (pior / total) * 100);
    }
}
