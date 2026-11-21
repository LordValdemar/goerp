package br.com.goerp.financeiro.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class ContaFinanceiraInativaException extends ExcecaoDeDominio {

    public ContaFinanceiraInativaException(String conta) {
        super("A conta " + conta + " está inativa");
    }
}
