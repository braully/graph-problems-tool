/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import static java.util.Map.Entry;
import junit.framework.TestCase;

/**
 *
 * @author Braully Rocha da Silva
 */
public class BatchExecuteHeuristicTest extends TestCase {

    public void testProcessFile() throws Exception {
//        System.out.println("processFile");
//        File file = new File("graph" + File.separator + "mft", "MTF_17-order-17-9999019890.mat");
//        File file = new File("graph" + File.separator + "mft", "MTF_15-order-15-9720.mat");
//        File file = new File("graph" + File.separator + "mft", "MTF_17-order-17-9999043451.mat");
//        File file = new File("graph" + File.separator + "mft", "MTF_14-order-14-21.mat");
//        File file = new File("graph" + File.separator + "mft", "MTF_7-order-7-04.mat");
//        File file = new File("graph" + File.separator + "almhypo", "almhypo20_g5-order-20-08.mat");
//        BatchExecuteHeuristic bach = new BatchExecuteHeuristic();
//        bach.processFileMat(file, dirname);

    }

    public void testReverseParentheses() {
//        String s = "a(bc)de";
//        String inicio, meio, fim;
//        int ix = s.indexOf("(");
//        int fx = s.indexOf(")");
//        System.out.println("i:" + ix + " f:" + fx);
//        inicio = s.substring(0, ix);
//        meio = s.substring(ix+1, fx);
//        fim = s.substring(fx+1);
//        System.out.println(inicio + meio + fim);
    }

    public HashMap<String, Float> calculcaValores(HashMap<String, Float> leiturasAtuais,
            HashMap<String, Float> leiturasAnteriores, Float fator) {
        HashMap<String, Float> resultado = new HashMap<>();

        for (Entry<String, Float> valorAtual : leiturasAtuais.entrySet()) {
            //
            String codigoCliente = valorAtual.getKey();
            Float valor = valorAtual.getValue();
            Float valorAnterior = leiturasAnteriores.get(codigoCliente);
            if (valorAnterior != null) {
                Float valorFinal = (valor - valorAnterior) * fator;
                resultado.put(codigoCliente, valorFinal);
            }
        }
        return resultado;
    }
}
