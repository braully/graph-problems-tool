/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.LinkedList;

/**
 *
 * @author braully
 */
public class TrabalhoProcessamento extends StrategyEstagnacao {

    Integer indiceAtual;
    Processamento last;
    LinkedList<Integer> bloco;

    public String getName() {
        return "Executar Trabalho Processamento";
    }

    TrabalhoProcessamento(LinkedList<Integer> bloco) {
        this.bloco = bloco;
        this.indiceAtual = 0;
    }

    public TrabalhoProcessamento(Integer indiceAtual) {
        this.indiceAtual = indiceAtual;
    }

    public void processarBlocoTotal(Processamento p) {
        while (indiceAtual < bloco.size()) {
            generateGraph(p);
            indiceAtual++;
        }
        p.printGraphCaminhoPercorrido();
    }

    @Override
    public void generateGraph(Processamento processamento) {
        last = processamento;
        if (bloco == null) {
            processamento.trabalhoAtual = processamento.trabalhoPorFazer.get(indiceAtual);
        } else {
            processamento.trabalhoAtual = bloco.get(indiceAtual);
        }
        System.out.printf("Trabalho atual %d do indice %d \n", processamento.trabalhoAtual, indiceAtual);
        estagnarVertice(processamento);
        System.out.printf("Concluido trabalho %d do indice %d \n", processamento.trabalhoAtual, indiceAtual);
    }

    public void processarProximo() {
        indiceAtual++;
        generateGraph(last);
    }
}
