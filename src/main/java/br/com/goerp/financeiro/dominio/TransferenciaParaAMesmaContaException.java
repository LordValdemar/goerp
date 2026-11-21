package br.com.goerp.financeiro.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class TransferenciaParaAMesmaContaException extends ExcecaoDeDominio {

    public TransferenciaParaAMesmaContaException() {
        super("A conta de origem e a de destino precisam ser diferentes");
    }
}
